package com.example.fitandeat

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val correo = intent.getStringExtra("correo")
        val textView = TextView(this)
        textView.text = "Bienvenido de nuevo: $correo\nAquí van tus estadísticas."
        setContentView(textView)
    }
}