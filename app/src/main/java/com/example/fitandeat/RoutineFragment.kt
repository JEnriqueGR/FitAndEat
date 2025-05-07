package com.example.fitandeat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RoutineFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedTrainAdapter
    private val savedTrainsList = mutableListOf<SavedTrain>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_routine, container, false)

        recyclerView = view.findViewById(R.id.recyclerSavedTrainings)
        adapter = SavedTrainAdapter(savedTrainsList)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        // Escucha primero para que se actualice si vuelve al fragment
        parentFragmentManager.setFragmentResultListener("saved_training", viewLifecycleOwner) { _, _ ->
            loadSavedTrainings()
        }

        val btnStartEmptyWorkout = view.findViewById<Button>(R.id.btnStartEmptyWorkout)
        btnStartEmptyWorkout.setOnClickListener {
            val bottomSheet = EntrenamientoBottomSheet()
            bottomSheet.show(parentFragmentManager, "EntrenamientoBottomSheet")
        }

        val btnNuevaRutina = view.findViewById<Button>(R.id.btnNuevaRutina)
        btnNuevaRutina.setOnClickListener {
            val intent = Intent(requireContext(), NewSessionTrainActivity::class.java)
            startActivity(intent)
        }

        loadSavedTrainings()

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
        savedTrainsList.clear()
        savedTrainsList.addAll(savedTrains)
        adapter.notifyDataSetChanged()

        if (savedTrainsList.isEmpty()) {
            Toast.makeText(requireContext(), "No tienes entrenamientos guardados aún", Toast.LENGTH_SHORT).show()
        }
    }
}
