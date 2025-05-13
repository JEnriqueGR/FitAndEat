package com.example.fitandeat

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
import kotlinx.coroutines.launch

class RoutineFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedTrainAdapter
    private lateinit var tvEmptyListMessage: TextView

    private lateinit var recyclerRoutines: RecyclerView
    private lateinit var routineAdapter: SavedRoutineAdapter
    private lateinit var tvEmptyListMessageRoutine: TextView

    private lateinit var recyclerSuggested: RecyclerView
    private lateinit var suggestedAdapter: SuggestedRoutineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_routine, container, false)

        recyclerView = view.findViewById(R.id.recyclerSavedTrainings)
        val dao = AppDatabase.getDatabase(requireContext()).savedTrainDao()
        adapter = SavedTrainAdapter(dao) { loadSavedTrainings() }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        tvEmptyListMessage = view.findViewById(R.id.tvEmptyListMessage)

        parentFragmentManager.setFragmentResultListener("saved_training", viewLifecycleOwner) { _, _ ->
            loadSavedTrainings()
        }

        recyclerRoutines = view.findViewById(R.id.recyclerSavedRoutines)
        tvEmptyListMessageRoutine = view.findViewById(R.id.tvEmptyListMessageRoutine)
        val routineDao = AppDatabase.getDatabase(requireContext()).savedRoutineDao()
        routineAdapter = SavedRoutineAdapter(routineDao) { loadSavedRoutines() }
        recyclerRoutines.layoutManager = LinearLayoutManager(requireContext())
        recyclerRoutines.adapter = routineAdapter
        recyclerRoutines.setHasFixedSize(true)

        recyclerSuggested = view.findViewById(R.id.recyclerSuggestedRoutines)
        recyclerSuggested.layoutManager = LinearLayoutManager(requireContext())
        recyclerSuggested.setHasFixedSize(true)

        val prefs = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val emailUsuario = prefs.getString("correo", "") ?: ""

        if (emailUsuario.isNotBlank()) {
            val db = AppDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                val user = db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
                val objetivo = user?.objetivo ?: ""

                // 💡 Creamos el adapter con el objetivo aquí
                suggestedAdapter = SuggestedRoutineAdapter(mutableListOf(), objetivo)
                recyclerSuggested.adapter = suggestedAdapter

                // Y luego cargamos las rutinas sugeridas
                val sugeridas = SuggestedRoutineRepository.obtenerRutinasPorObjetivo(objetivo)
                suggestedAdapter.updateList(sugeridas)
            }
        }

        parentFragmentManager.setFragmentResultListener("saved_routine", viewLifecycleOwner) { _, _ ->
            loadSavedRoutines()
        }

        val btnStartEmptyWorkout = view.findViewById<Button>(R.id.btnStartEmptyWorkout)
        btnStartEmptyWorkout.setOnClickListener {
            val bottomSheet = EntrenamientoBottomSheet()
            bottomSheet.show(parentFragmentManager, "EntrenamientoBottomSheet")
        }

        val btnNuevaRutina = view.findViewById<Button>(R.id.btnNuevaRutina)
        btnNuevaRutina.setOnClickListener {
            val modal = RutinaBottomSheet()
            modal.show(parentFragmentManager, "RutinaBottomSheet")
        }

        loadSavedTrainings()
        loadSavedRoutines()

        return view
    }

    private fun loadSavedTrainings() {
        val prefs = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val emailUsuario = prefs.getString("correo", "") ?: ""

        if (emailUsuario.isBlank()) {
            Toast.makeText(requireContext(), "No se detectó usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            val user = db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
            if (user != null) {
                val savedTrains = db.savedTrainDao().getTrainsByUser(user.email)
                displaySavedTrainings(savedTrains)
            } else {
                Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySavedTrainings(savedTrains: List<SavedTrain>) {
        adapter.updateList(savedTrains)

        if (savedTrains.isEmpty()) {
            tvEmptyListMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyListMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            val layoutParams = recyclerView.layoutParams
            layoutParams.height = if (savedTrains.size < 3) {
                (150 * resources.displayMetrics.density).toInt()
            } else {
                (285 * resources.displayMetrics.density).toInt()
            }
            recyclerView.layoutParams = layoutParams
        }
    }

    private fun loadSavedRoutines() {
        val prefs = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val emailUsuario = prefs.getString("correo", "") ?: ""

        if (emailUsuario.isBlank()) {
            Toast.makeText(requireContext(), "No se detectó usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            val user = db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
            if (user != null) {
                val savedRoutines = db.savedRoutineDao().getRoutinesByUser(user.email)
                displaySavedRoutines(savedRoutines)
            } else {
                Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySavedRoutines(savedRoutines: List<SavedRoutine>) {
        routineAdapter.updateList(savedRoutines)

        if (savedRoutines.isEmpty()) {
            tvEmptyListMessageRoutine.visibility = View.VISIBLE
            recyclerRoutines.visibility = View.GONE
        } else {
            tvEmptyListMessageRoutine.visibility = View.GONE
            recyclerRoutines.visibility = View.VISIBLE

            val layoutParams = recyclerRoutines.layoutParams
            layoutParams.height = if (savedRoutines.size < 3) {
                (150 * resources.displayMetrics.density).toInt()
            } else {
                (200 * resources.displayMetrics.density).toInt()
            }
            recyclerRoutines.layoutParams = layoutParams
        }
    }
}
