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
import com.example.fitandeat.exercise.RutinaBottomSheet
import com.example.fitandeat.exercise.dao.SavedRoutineDao
import com.example.fitandeat.exercise.model.Rutina
import com.example.fitandeat.exercise.model.SavedRoutine
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedRoutineAdapter(
    private val dao: SavedRoutineDao,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<SavedRoutineAdapter.RoutineViewHolder>() {

    private val routinesMutable = mutableListOf<SavedRoutine>()

    fun updateList(newList: List<SavedRoutine>) {
        routinesMutable.clear()
        routinesMutable.addAll(newList)
        notifyDataSetChanged()
    }

    class RoutineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRoutineName)
        val tvExercises: TextView = view.findViewById(R.id.tvExerciseList)
        val ivDelete: ImageView = view.findViewById(R.id.ivDeleteRoutine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_saved_routine_card, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val gson = Gson()
        val savedRoutine = routinesMutable[position]
        val rutina = gson.fromJson(savedRoutine.rutinaJson, Rutina::class.java)

        holder.tvName.text = rutina.nombre
        val exerciseSummary = buildString {
            rutina.ejercicios.forEach { ejercicio ->
                append("\n${ejercicio.nombreEjercicio}\n")
                ejercicio.series.forEachIndexed { index, serie ->
                    append("  • Serie ${index + 1}: ${serie.kg}kg x ${serie.repeticiones} reps\n")
                }
            }
        }
        holder.tvExercises.text = exerciseSummary

        holder.ivDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar rutina")
                .setMessage("¿Seguro que quieres eliminar esta rutina?")
                .setPositiveButton("Eliminar") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.deleteRoutineById(savedRoutine.id)
                        withContext(Dispatchers.Main) {
                            removeById(savedRoutine.id)
                            Toast.makeText(holder.itemView.context, "Rutina eliminada", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.itemView.setOnClickListener {
            val fragmentManager = (holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager
            val bottomSheet = RutinaBottomSheet()

            val bundle = Bundle().apply {
                putString("rutinaJson", savedRoutine.rutinaJson)
            }

            bottomSheet.arguments = bundle
            bottomSheet.show(fragmentManager, "RutinaBottomSheet")
        }
    }

    private fun removeById(id: Int) {
        routinesMutable.removeIf { it.id == id }
        notifyDataSetChanged()
        onDataChanged()
    }

    override fun getItemCount(): Int = routinesMutable.size
}
