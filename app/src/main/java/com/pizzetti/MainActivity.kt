package com.pizzetti

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.pizzetti.databinding.ActivityMainBinding
import com.pizzetti.databinding.ToppingElementBinding
import com.pizzetti.models.Topping

class MainActivity : AppCompatActivity() {
    // View Binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var sizes: Array<RelativeLayout>
    private var selectedSize: Int = -1
    private var selectedToppings: MutableList<Topping> = mutableListOf()
    private var price = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        // initiate the pizza size selection
        sizes = arrayOf(
            binding.smallSize.pizzaSizeSelect,
            binding.mediumSize.pizzaSizeSelect,
            binding.largeSize.pizzaSizeSelect
        )
        for (i in sizes.indices) {
            sizes[i].setOnClickListener {
                handleSelectSize(i)
            }
        }
        initializeToppings()
        binding.confirm.setOnClickListener {
            handlePlaceOrder()
        }
    }

    private fun handlePlaceOrder() {
        val firstName = binding.firstName.text.toString()
        val lastName = binding.lastName.text.toString()
        val address = binding.address.text.toString()
        if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
        } else if (selectedSize == -1) {
            Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
        } else {
            // Send sms
            intent = Intent(Intent.ACTION_VIEW)
            intent.putExtra(
                "sms_body",
                "Hello, I would like to order a pizza with the following toppings: ${selectedToppings.map { it.name }}. "
                    .replace("[", "").replace("]", "") +
                    "My name is $firstName $lastName and I live at $address. The total should price is $$price."
            )
            intent.type = "vnd.android-dir/mms-sms"
            startActivity(intent)
        }
    }

//    @SuppressLint("SetTextI18n")
//    private fun setPrice(price: Int) {
//        // value animator
//        val animator = ValueAnimator()
//        animator.setObjectValues(this.price, price)
//        animator.addUpdateListener {
//            val value = it.animatedValue as Int
//            binding.priceText.text = "$ $value"
//        }
//        animator.duration = 500
//        animator.start()
//
//        // animate the price change as with alpha variation
////        binding.priceText.animate().alpha(0f).setDuration(200).withEndAction {
////            binding.priceText.text = "$ $price"
////            binding.priceText.animate().alpha(1f).setDuration(200).start()
////        }.start()
//
//        this.price = price
//    }

    @SuppressLint("SetTextI18n")
    private fun handleSelectSize(size: Int) {
        val primaryColor = ContextCompat.getColor(
            this,
            com.google.android.material.R.color.cardview_dark_background
        )
        val secondaryColor = ContextCompat.getColor(this, R.color.white)
        if (selectedSize != -1) {
            modifySelectSize(selectedSize, secondaryColor, primaryColor)
        }
        modifySelectSize(size, primaryColor, secondaryColor, 1.2f)
        val oldPrice = when (selectedSize) {
            0 -> 10
            1 -> 17
            2 -> 25
            else -> 0
        }
        selectedSize = size

        // update the price
        val price = when (size) {
            0 -> 12
            1 -> 20
            else -> 30
        }
//        setPrice(this.price - oldPrice + price)
    }

    private fun modifySelectSize(size: Int, bg: Int, color: Int, scale: Float = 1.0f) {
        val sizeView = sizes[size]
        val roundedCorner = sizeView.background as GradientDrawable
        roundedCorner.setColor(bg)
        val text = sizeView.getChildAt(0) as TextView
        text.setTextColor(color)
        sizeView.animate().scaleX(scale).scaleY(scale).duration = 150
    }

    private fun initializeToppings() {
        val linearLayout = binding.toppingsLinearLayout
        val fileName = "toppings.json"
        val jsonString = application.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
        val gson = Gson()
        val toppings = gson.fromJson(jsonString, Array<Topping>::class.java)
        val disabledAlpha = 0.3f

        for (topping in toppings) {
            val toppingBinding = ToppingElementBinding.inflate(layoutInflater)
            toppingBinding.topping = topping
            val imageId = resources.getIdentifier(
                topping.imageSrc,
                "drawable",
                packageName
            )
            toppingBinding.toppingImage.setImageResource(imageId)
            linearLayout.addView(toppingBinding.root)

            // disable the remove button
            toppingBinding.remove.isEnabled = false
            toppingBinding.remove.alpha = disabledAlpha

            // add the click listener
            toppingBinding.add.setOnClickListener {
//                setPrice(price + topping.price)
                selectedToppings.add(topping)

                toppingBinding.add.isEnabled = false
                toppingBinding.add.alpha = disabledAlpha

                toppingBinding.remove.isEnabled = true
                toppingBinding.remove.alpha = 1.0f
            }
            toppingBinding.remove.setOnClickListener {
//                setPrice(price - topping.price)
                selectedToppings.remove(topping)

                toppingBinding.add.isEnabled = true
                toppingBinding.add.alpha = 1.0f

                toppingBinding.remove.isEnabled = false
                toppingBinding.remove.alpha = disabledAlpha
            }
        }
    }
}