package com.example.fitandeat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_food) // Asegúrate de tener activity_food.xml
    }
}
