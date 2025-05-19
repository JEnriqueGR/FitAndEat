package com.example.fitandeat.exercise.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fitandeat.R
import com.example.fitandeat.exercise.model.Exercise

class ExerciseSelectionAdapterRoutine(

    private val context: Context,
    private var ejercicios: List<Exercise>,
    private val onExerciseSelected: (List<Exercise>) -> Unit // ⬅ devuelve lista

) : RecyclerView.Adapter<ExerciseSelectionAdapterRoutine.ExerciseViewHolder>() {

    private val selectedExercises = mutableSetOf<Exercise>() // ⬅ selección múltiple

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEjercicio)
        val ivImagen: ImageView = view.findViewById(R.id.ivEjercicio)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val ejercicio = ejercicios[position]

                if (selectedExercises.contains(ejercicio)) {
                    selectedExercises.remove(ejercicio)
                } else {
                    selectedExercises.add(ejercicio)
                }

                onExerciseSelected(selectedExercises.toList())
                notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.tvNombre.text = ejercicio.nombre
        holder.ivImagen.setImageResource(ejercicio.imagenResId)

        // Colorear fondo si está seleccionado
        if (selectedExercises.contains(ejercicio)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    override fun getItemCount(): Int = ejercicios.size

    fun updateData(newEjercicios: List<Exercise>) {
        ejercicios = newEjercicios
        selectedExercises.retainAll(newEjercicios.toSet())
        notifyDataSetChanged()
    }

    fun getSelectedExercises(): List<Exercise> = selectedExercises.toList()
}





