package com.example.fitandeat

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddFoodActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var emailUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        db = AppDatabase.getDatabase(this)

        // Recuperar el correo del usuario desde SharedPreferences
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        emailUsuario = prefs.getString("correo", "") ?: ""

        if (emailUsuario.isBlank()) {
            Toast.makeText(this, "No se detectó usuario activo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etNombre = findViewById<EditText>(R.id.etNombreComida)
        val etCalorias = findViewById<EditText>(R.id.etCalorias)
        val etProteinas = findViewById<EditText>(R.id.etProteinas)
        val etCarbohidratos = findViewById<EditText>(R.id.etCarbohidratos)
        val etGrasas = findViewById<EditText>(R.id.etGrasas)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarComida)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val calorias = etCalorias.text.toString().toIntOrNull()
            val proteinas = etProteinas.text.toString().toIntOrNull()
            val carbohidratos = etCarbohidratos.text.toString().toIntOrNull()
            val grasas = etGrasas.text.toString().toIntOrNull()

            if (nombre.isBlank() || calorias == null || proteinas == null || carbohidratos == null || grasas == null) {
                Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevaComida = Food(
                nombre = nombre,
                calorias = calorias,
                proteinas = proteinas,
                carbohidratos = carbohidratos,
                grasas = grasas,
                email = emailUsuario  // asociar al usuario
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.foodDao().insertarComida(nuevaComida)
                }

                Toast.makeText(this@AddFoodActivity, "Comida guardada", Toast.LENGTH_SHORT).show()
                finish() // Volver al fragmento anterior
            }
        }
    }
}
