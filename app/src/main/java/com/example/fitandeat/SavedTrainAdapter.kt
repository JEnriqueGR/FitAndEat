package com.example.fitandeat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class SavedTrainAdapter(
        private val trainings: List<SavedTrain>
) : RecyclerView.Adapter<SavedTrainAdapter.TrainingViewHolder>() {

class TrainingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvName: TextView = view.findViewById(R.id.tvTrainingName)
    val tvExercises: TextView = view.findViewById(R.id.tvExerciseList)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_saved_training_card, parent, false)
    return TrainingViewHolder(view)
}

override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
    val gson = Gson()
    val entrenamiento = gson.fromJson(trainings[position].entrenamientoJson, Entrenamiento::class.java)

    holder.tvName.text = entrenamiento.nombreEntrenamiento
    val exerciseSummary = entrenamiento.series.joinToString("\n") { serie ->
            "${serie.nombreEjercicio} - ${serie.numero} series (${serie.kg}kg x ${serie.repeticiones} reps)"
    }
    holder.tvExercises.text = exerciseSummary
}

override fun getItemCount(): Int = trainings.size
}
