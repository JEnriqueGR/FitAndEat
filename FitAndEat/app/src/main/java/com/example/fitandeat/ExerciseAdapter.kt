package com.example.fitandeat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(
    private val context: Context,
    private val ejercicios: List<Exercise>
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEjercicio)
        val ivImagen: ImageView = view.findViewById(R.id.ivEjercicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun getItemCount(): Int = ejercicios.size

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.tvNombre.text = ejercicio.nombre
        holder.ivImagen.setImageResource(ejercicio.imagenResId)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ExerciseDetailActivity::class.java)
            intent.putExtra("exercise", ejercicio) // ← usamos el objeto completo
            context.startActivity(intent)
        }
    }
}