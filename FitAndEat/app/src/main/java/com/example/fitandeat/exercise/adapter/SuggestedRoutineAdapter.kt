package com.example.fitandeat.exercise.adapter

import com.example.fitandeat.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.example.fitandeat.exercise.model.Rutina
import com.google.gson.Gson
import com.example.fitandeat.exercise.RutinaBottomSheet

class SuggestedRoutineAdapter(
    private val routines: MutableList<Rutina>,
    private val objetivo: String
) : RecyclerView.Adapter<SuggestedRoutineAdapter.RoutineViewHolder>() {

    class RoutineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoutineName: TextView = view.findViewById(R.id.tvRoutineName)
        val tvExercises: TextView = view.findViewById(R.id.tvExerciseList)
        val ivInfo: ImageView = view.findViewById(R.id.ivInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_suggested_routine_card, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val rutina = routines[position]

        holder.tvRoutineName.text = rutina.nombre

        val resumen = rutina.ejercicios.joinToString("\n") { ejercicio ->
            val primeraSerie = ejercicio.series.firstOrNull()
            if (primeraSerie != null) {
                val kgText = if (primeraSerie.kg == 0f) "- kg" else "${primeraSerie.kg}kg"
                val repsText = if (primeraSerie.repeticiones == 0) "- reps" else "${primeraSerie.repeticiones} reps"
                "${ejercicio.nombreEjercicio} - ${ejercicio.series.size} series ($kgText x $repsText)"
            } else {
                "${ejercicio.nombreEjercicio} - sin series"
            }
        }

        holder.tvExercises.text = resumen

        // Usamos todo el card para abrir la rutina sugerida
        holder.itemView.setOnClickListener {
            val fragmentManager = (holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager
            val bottomSheet = RutinaBottomSheet()

            val rutinaJson = Gson().toJson(rutina)
            val bundle = Bundle().apply {
                putString("rutinaJson", rutinaJson)
            }

            bottomSheet.arguments = bundle
            bottomSheet.show(fragmentManager, "RutinaBottomSheet")
        }

        holder.ivInfo.setOnClickListener {
            val contexto = holder.itemView.context

            val mensaje = when (objetivo.lowercase()) {
                "perder grasa" -> """
            🔥 Objetivo: Perder Grasa
            
            • Peso: Moderado
            • Repeticiones: 12–20 por serie
            • Series: 3–4 por ejercicio
            
            Consejo: Combina fuerza con HIIT para mayor quema de grasa.
        """.trimIndent()
                "ganar músculo" -> """
            💪 Objetivo: Ganar Músculo
            
            • Peso: 70–85% de tu 1RM
            • Repeticiones: 6–12 por serie
            • Series: 3–5 por ejercicio
            
            Consejo: Apunta a sobrecarga progresiva y buena nutrición.
        """.trimIndent()
                "mantenerte" -> """
            ⚖️ Objetivo: Mantenerte
            • Peso: Moderado
            • Repeticiones: 10–15 por serie
            • Series: 2–3 por ejercicio
            
           Consejo: Equilibra cardio y fuerza para conservar condición.
        """.trimIndent()
                else -> "Personaliza tu peso y repeticiones según tus capacidades y objetivos."
            }

            val dialog = androidx.appcompat.app.AlertDialog.Builder(contexto)
                .setTitle("Recomendaciones")
                .setMessage(mensaje)
                .setPositiveButton("Entendido", null)
                .create()

            dialog.setOnShowListener {
                val boton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                boton.setTextColor(androidx.core.content.ContextCompat.getColor(contexto, R.color.greencheck))
            }

            dialog.show()
        }

    }

    override fun getItemCount(): Int = routines.size

    fun updateList(newRoutines: List<Rutina>) {
        routines.clear()
        routines.addAll(newRoutines)
        notifyDataSetChanged()
    }

}
