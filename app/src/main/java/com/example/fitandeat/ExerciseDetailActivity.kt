package com.example.fitandeat

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ExerciseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_detail)

        val exercise = intent.getSerializableExtra("exercise") as? Exercise ?: return

        val ivDetalle = findViewById<ImageView>(R.id.ivDetalle)
        val tvNombre = findViewById<TextView>(R.id.tvNombreDetalle)
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcionDetalle)
        val layoutAlternativas = findViewById<LinearLayout>(R.id.layoutAlternativas)

        ivDetalle.setImageResource(exercise.imagenResId)
        tvNombre.text = exercise.nombre
        tvDescripcion.text = exercise.descripcion

        layoutAlternativas.removeAllViews()

        for (alt in exercise.alternativas) {
            val filaLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 32)
                }
                gravity = Gravity.CENTER_HORIZONTAL
            }

            val redirigir = {
                val destino = ExercisesRepository.buscarPorNombre(alt.nombre)
                destino?.let {
                    val intent = Intent(this@ExerciseDetailActivity, ExerciseDetailActivity::class.java)
                    intent.putExtra("exercise", it)
                    startActivity(intent)
                }
            }

            val altText = TextView(this).apply {
                text = alt.nombre
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 100, 80, 0)
                }
                gravity = Gravity.CENTER_VERTICAL
                setOnClickListener { redirigir() }
            }

            val altImage = ImageView(this).apply {
                setImageResource(alt.imagenResId)
                layoutParams = LinearLayout.LayoutParams(300, 300)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setOnClickListener { redirigir() }
            }

            filaLayout.addView(altText)
            filaLayout.addView(altImage)
            layoutAlternativas.addView(filaLayout)
        }
    }

}