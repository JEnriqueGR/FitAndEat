package com.example.fitandeat

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.exercise.model.SesionTrain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewSessionTrainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var emailUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_train)

        db = AppDatabase.getDatabase(this)

        // Recuperamos el correo desde SharedPreferences
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        emailUsuario = prefs.getString("correo", "") ?: ""

        // Comprobador 🔍
        Toast.makeText(this, "Correo usado: $emailUsuario", Toast.LENGTH_LONG).show()
        Log.d("SESION_DEBUG", "Correo en SharedPreferences: $emailUsuario")

        val etEjercicio = findViewById<EditText>(R.id.etEjercicio)
        val etSeries = findViewById<EditText>(R.id.etSeries)
        val etRepeticiones = findViewById<EditText>(R.id.etRepeticiones)
        val etPeso = findViewById<EditText>(R.id.etPeso)
        val btnGuardar = findViewById<Button>(R.id.btnRegistrar)

        btnGuardar.setOnClickListener {
            val ejercicio = etEjercicio.text.toString()
            val series = etSeries.text.toString().toIntOrNull()
            val repeticiones = etRepeticiones.text.toString().toIntOrNull()
            val peso = etPeso.text.toString().toFloatOrNull()

            if (ejercicio.isBlank() || series == null || repeticiones == null || peso == null) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuario = withContext(Dispatchers.IO) {
                    db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
                }

                val usuarioExiste = withContext(Dispatchers.IO) {
                    db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
                }

                if (usuarioExiste == null) {
                    Log.e("SESION_DEBUG", "¡Usuario NO encontrado en Room con correo: $emailUsuario!")
                    runOnUiThread {
                        Toast.makeText(this@NewSessionTrainActivity, "Primero debes completar tu perfil.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                } else {
                    Log.d("SESION_DEBUG", "Usuario encontrado en Room: ${usuarioExiste.email}")
                }


                val sesion = SesionTrain(
                    email = emailUsuario,
                    ejercicio = ejercicio,
                    series = series,
                    repeticiones = repeticiones,
                    peso = peso
                )

                withContext(Dispatchers.IO) {
                    db.sessionTrainDao().insertarSesion(sesion)
                }

                Toast.makeText(this@NewSessionTrainActivity, "Sesión registrada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }
}