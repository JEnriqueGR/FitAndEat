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

class ExerciseSelectionAdapter(

    private val context: Context,
    private var ejercicios: List<Exercise>,
    private val onExerciseSelected: (Exercise?) -> Unit

) : RecyclerView.Adapter<ExerciseSelectionAdapter.ExerciseViewHolder>() {

    var selectedExercise: Exercise? = null // Guardar el ejercicio seleccionado por su nombre

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEjercicio)
        val ivImagen: ImageView = view.findViewById(R.id.ivEjercicio)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                val ejercicio = ejercicios[position]

                if (selectedExercise == ejercicio) {
                    // Si ya está seleccionado, lo deseleccionamos
                    selectedExercise = null
                    onExerciseSelected(null) // Desmarcar el ejercicio
                } else {
                    // Si no está seleccionado, lo seleccionamos
                    selectedExercise = ejercicio
                    onExerciseSelected(ejercicio) // Seleccionar el ejercicio
                }
                notifyDataSetChanged()
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

        // Cambiar el color de fondo si el ejercicio está seleccionado
        if (selectedExercise == ejercicio) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor)) // Azul claro
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white)) // Color blanco
        }
    }

    override fun getItemCount(): Int = ejercicios.size

    // Esta función ahora solo actualiza los datos sin notificar cambios de UI innecesarios.
    fun updateData(newEjercicios: List<Exercise>, selectedExercise: Exercise?) {
        this.ejercicios = newEjercicios
        this.selectedExercise = selectedExercise // Asegurarse de que el ejercicio seleccionado sigue siendo el mismo

        // Si el ejercicio seleccionado no está en la lista filtrada, desmarcarlo
        if (newEjercicios.find { it.nombre == selectedExercise?.nombre } == null) {
            this.selectedExercise = null
        }

        notifyDataSetChanged() // Notificar la actualización de la UI
    }
}




