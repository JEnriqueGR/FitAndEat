package com.example.fitandeat

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var etEmail: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        db = AppDatabase.getDatabase(this)

        etEmail = findViewById(R.id.etEmail)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Botón para iniciar sesión
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuario = withContext(Dispatchers.IO) {
                    db.userDao().obtenerUsuarioPorCorreo(email)
                }

                if (usuario == null) {
                    Toast.makeText(this@SignInActivity, "Usuario no registrado", Toast.LENGTH_SHORT).show()
                } else {
                    // Guardamos el correo
                    getSharedPreferences("usuario", MODE_PRIVATE).edit().putString("correo", email).apply()

                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Botón para registrarse
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuario = withContext(Dispatchers.IO) {
                    db.userDao().obtenerUsuarioPorCorreo(email)
                }

                if (usuario != null) {
                    Toast.makeText(this@SignInActivity, "El usuario ya está registrado", Toast.LENGTH_SHORT).show()
                } else {
                    // Guardamos el correo
                    getSharedPreferences("usuario", MODE_PRIVATE).edit().putString("correo", email).apply()

                    val intent = Intent(this@SignInActivity, CompleteProfileActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
