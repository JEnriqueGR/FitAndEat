package com.example.fitandeat.account

import com.example.fitandeat.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.user.model.User
import kotlinx.coroutines.launch

class CompleteProfileActivity : AppCompatActivity() {
    private lateinit var nombreEditText: EditText
    private lateinit var estaturaEditText: EditText
    private lateinit var pesoEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var sexoEditText: EditText
    private lateinit var guardarButton: Button
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        // Obtener email desde el intent
        val email = intent.getStringExtra("email") ?: return

        val opcionesSexo = listOf("Hombre", "Mujer", "Prefiero no decirlo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opcionesSexo)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.etSexo)
        autoCompleteTextView.setAdapter(adapter)

        // Opcional: para que se despliegue al hacer clic
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }


        nombreEditText = findViewById(R.id.etNombre)
        estaturaEditText = findViewById(R.id.etEstatura)
        pesoEditText = findViewById(R.id.etPeso)
        edadEditText = findViewById(R.id.etEdad)
        sexoEditText = findViewById(R.id.etSexo)
        guardarButton = findViewById(R.id.btnGuardarInfo)

        db = AppDatabase.getDatabase(this)

        guardarButton.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val estatura = estaturaEditText.text.toString().toFloatOrNull()
            val peso = pesoEditText.text.toString().toFloatOrNull()
            val edad = edadEditText.text.toString().toIntOrNull()
            val sexo = sexoEditText.text.toString()

            if (estatura == null || peso == null || edad == null || sexo.isBlank()) {
                Toast.makeText(this, "Por favor llena todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoUsuario = User(
                email = email,
                nombre = nombre,
                edad = edad,
                peso = peso,
                estatura = estatura,
                sexo = sexo,
                objetivo = ""
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.userDao().insertarUsuario(nuevoUsuario)
                }

                // Ir a UserGoalActivity con el mismo email
                val intent = Intent(this@CompleteProfileActivity, UserGoalActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
                finish()
            }
        }
    }
}