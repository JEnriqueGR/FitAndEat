package com.example.fitandeat.exercise.adapter

import com.example.fitandeat.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fitandeat.exercise.EntrenamientoBottomSheet
import com.example.fitandeat.exercise.dao.SavedTrainDao
import com.example.fitandeat.exercise.model.Entrenamiento
import com.example.fitandeat.exercise.model.SavedTrain
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedTrainAdapter(
    private val dao: SavedTrainDao,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<SavedTrainAdapter.TrainingViewHolder>() {

    private val trainingsMutable = mutableListOf<SavedTrain>()

    fun updateList(newList: List<SavedTrain>) {
        trainingsMutable.clear()
        trainingsMutable.addAll(newList)
        notifyDataSetChanged()
    }

    class TrainingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvTrainingName)
        val tvExercises: TextView = view.findViewById(R.id.tvExerciseList)
        val ivDelete: ImageView = view.findViewById(R.id.ivDeleteTraining)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_saved_training_card, parent, false)
        return TrainingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
        val gson = Gson()
        val savedTrain = trainingsMutable[position]
        val entrenamiento = gson.fromJson(savedTrain.entrenamientoJson, Entrenamiento::class.java)

        holder.tvName.text = entrenamiento.nombreEntrenamiento
        val exerciseSummary = entrenamiento.series.joinToString("\n") { serie ->
            "${serie.nombreEjercicio} - ${serie.numero} series (${serie.kg}kg x ${serie.repeticiones} reps)"
        }
        holder.tvExercises.text = exerciseSummary

        holder.ivDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar entrenamiento")
                .setMessage("¿Seguro que quieres eliminar este entrenamiento?")
                .setPositiveButton("Eliminar") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.deleteTrainById(savedTrain.id)

                        withContext(Dispatchers.Main) {
                            removeById(savedTrain.id)
                            Toast.makeText(holder.itemView.context, "Entrenamiento eliminado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.itemView.setOnClickListener {
            val fragmentManager = (holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager
            val bottomSheet = EntrenamientoBottomSheet()

            // Pasar datos en un Bundle
            val bundle = Bundle().apply {
                putString("nombreEntrenamiento", entrenamiento.nombreEntrenamiento)
                putString("entrenamientoJson", savedTrain.entrenamientoJson)
            }

            bottomSheet.arguments = bundle
            bottomSheet.show(fragmentManager, "EntrenamientoBottomSheet")
        }
    }

    private fun removeById(id: Int) {
        trainingsMutable.removeIf { it.id == id }
        notifyDataSetChanged()
        onDataChanged()
    }

    override fun getItemCount(): Int = trainingsMutable.size
}
