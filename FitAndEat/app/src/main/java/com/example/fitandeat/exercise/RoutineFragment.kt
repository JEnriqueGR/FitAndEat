// app/src/main/java/com/example/fitandeat/exercise/RoutineFragment.kt

package com.example.fitandeat.exercise

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitandeat.R
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.exercise.adapter.SavedRoutineAdapter
import com.example.fitandeat.exercise.adapter.SavedTrainAdapter
import com.example.fitandeat.exercise.adapter.SuggestedRoutineAdapter
import com.example.fitandeat.exercise.model.SavedRoutine
import com.example.fitandeat.exercise.model.SavedTrain
import com.example.fitandeat.exercise.repository.SuggestedRoutineRepository
import kotlinx.coroutines.launch

class RoutineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos layout
        val view = inflater.inflate(R.layout.fragment_routine, container, false)

        // ─────────── Navegación a Calendario ───────────
        val btnCalendario = view.findViewById<Button>(R.id.btnCalendario)
        btnCalendario.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, CalendarRoutineFragment())
                .addToBackStack(null)
                .commit()
        }

        // ─────────── Entrenamientos guardados ───────────
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerSavedTrainings)
        val dao = AppDatabase.getDatabase(requireContext()).savedTrainDao()
        val adapter = SavedTrainAdapter(dao) { loadSavedTrainings() }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        val tvEmptyListMessage = view.findViewById<TextView>(R.id.tvEmptyListMessage)
        parentFragmentManager.setFragmentResultListener("saved_training", viewLifecycleOwner) { _, _ ->
            loadSavedTrainings()
        }

        // ─────────── Rutinas guardadas ───────────
        val recyclerRoutines = view.findViewById<RecyclerView>(R.id.recyclerSavedRoutines)
        val tvEmptyListMessageRoutine = view.findViewById<TextView>(R.id.tvEmptyListMessageRoutine)
        val routineDao = AppDatabase.getDatabase(requireContext()).savedRoutineDao()
        val routineAdapter = SavedRoutineAdapter(routineDao) { loadSavedRoutines() }
        recyclerRoutines.layoutManager = LinearLayoutManager(requireContext())
        recyclerRoutines.adapter = routineAdapter
        recyclerRoutines.setHasFixedSize(true)

        parentFragmentManager.setFragmentResultListener("saved_routine", viewLifecycleOwner) { _, _ ->
            loadSavedRoutines()
        }

        // ─────────── Rutinas sugeridas ───────────
        val recyclerSuggested = view.findViewById<RecyclerView>(R.id.recyclerSuggestedRoutines)
        recyclerSuggested.layoutManager = LinearLayoutManager(requireContext())
        recyclerSuggested.setHasFixedSize(true)

        val prefs = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val emailUsuario = prefs.getString("correo", "") ?: ""
        if (emailUsuario.isNotBlank()) {
            val db = AppDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                val user = db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
                val objetivo = user?.objetivo ?: ""
                val suggestedAdapter = SuggestedRoutineAdapter(mutableListOf(), objetivo)
                recyclerSuggested.adapter = suggestedAdapter
                val sugeridas = SuggestedRoutineRepository.obtenerRutinasPorObjetivo(objetivo)
                suggestedAdapter.updateList(sugeridas)
            }
        }

        // ─────────── Botones BottomSheets ───────────
        view.findViewById<Button>(R.id.btnStartEmptyWorkout).setOnClickListener {
            EntrenamientoBottomSheet().show(parentFragmentManager, "EntrenamientoBottomSheet")
        }
        view.findViewById<Button>(R.id.btnNuevaRutina).setOnClickListener {
            RutinaBottomSheet().show(parentFragmentManager, "RutinaBottomSheet")
        }

        // ─────────── Carga inicial ───────────
        loadSavedTrainings()
        loadSavedRoutines()

        return view
    }

    private fun loadSavedTrainings() { /* tu impl existente */ }
    private fun displaySavedTrainings(savedTrains: List<SavedTrain>) { /* tu impl existente */ }
    private fun loadSavedRoutines() { /* tu impl existente */ }
    private fun displaySavedRoutines(savedRoutines: List<SavedRoutine>) { /* tu impl existente */ }
}
