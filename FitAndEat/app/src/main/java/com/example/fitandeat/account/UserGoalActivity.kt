package com.example.fitandeat.account

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.MainActivity
import com.example.fitandeat.R
import com.example.fitandeat.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserGoalActivity : AppCompatActivity() {

    private lateinit var btnPerderGrasa: Button
    private lateinit var btnGanarMusculo: Button
    private lateinit var btnMantenerte: Button
    private lateinit var email: String
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_goal)

        // Obtener el correo recibido
        email = intent.getStringExtra("email") ?: return

        db = AppDatabase.getDatabase(this)

        // Enlazar botones
        btnPerderGrasa = findViewById(R.id.btnPerderGrasa)
        btnGanarMusculo = findViewById(R.id.btnGanarMusculo)
        btnMantenerte = findViewById(R.id.btnMantenerte)

        // Escuchadores
        btnPerderGrasa.setOnClickListener {
            guardarObjetivoYContinuar("Perder grasa")
        }

        btnGanarMusculo.setOnClickListener {
            guardarObjetivoYContinuar("Ganar músculo")
        }

        btnMantenerte.setOnClickListener {
            guardarObjetivoYContinuar("Mantenerme")
        }
    }

    private fun guardarObjetivoYContinuar(objetivo: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.userDao().actualizarObjetivo(email, objetivo)
            }

            Toast.makeText(this@UserGoalActivity, "Objetivo guardado: $objetivo", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@UserGoalActivity, MainActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
            finish()
        }
    }
}