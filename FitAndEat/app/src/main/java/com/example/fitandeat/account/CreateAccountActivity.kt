package com.example.fitandeat.account

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.R
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.user.model.User
import kotlinx.coroutines.launch

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        db = AppDatabase.Companion.getDatabase(this)

        val nombreInput = findViewById<EditText>(R.id.etNombre)
        val estaturaInput = findViewById<EditText>(R.id.etEstatura)
        val pesoInput = findViewById<EditText>(R.id.etPeso)
        val edadInput = findViewById<EditText>(R.id.etEdad)
        val sexoInput = findViewById<EditText>(R.id.etSexo)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarInfo)

        val email = intent.getStringExtra("email") ?: ""

        btnGuardar.setOnClickListener {
            val nombre = nombreInput.text.toString()
            val estatura = estaturaInput.text.toString().toFloatOrNull()
            val peso = pesoInput.text.toString().toFloatOrNull()
            val edad = edadInput.text.toString().toIntOrNull()
            val sexo = sexoInput.text.toString()

            if (estatura != null && peso != null && edad != null && sexo.isNotBlank()) {
                val user = User(
                    email,
                    nombre,
                    edad,
                    peso,
                    estatura,
                    sexo,
                    objetivo = ""
                ) // el objetivo se llenará después

                lifecycleScope.launch {
                    db.userDao().insertarUsuario(user)

                    val intent = Intent(this@CreateAccountActivity, UserGoalActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}