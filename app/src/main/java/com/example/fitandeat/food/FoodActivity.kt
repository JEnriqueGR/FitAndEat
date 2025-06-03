package com.example.fitandeat.food
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitandeat.R

class FoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_food) // Asegúrate de tener activity_food.xml
    }
}
