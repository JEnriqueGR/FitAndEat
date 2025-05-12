package com.example.fitandeat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RoutineFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedTrainAdapter
    private lateinit var tvEmptyListMessage: TextView


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

        Log.d("RoutineFragment", "Email usuario: $emailUsuario")

        if (emailUsuario.isBlank()) {
            Toast.makeText(requireContext(), "No se detectó usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            val user = db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
            Log.d("RoutineFragment", "Usuario encontrado: $user")

            if (user != null) {
                val savedTrains = db.savedTrainDao().getTrainsByUser(user.email)
                Log.d("RoutineFragment", "Entrenamientos encontrados: ${savedTrains.size}")

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

            // Si solo hay una fila, reducimos el height
            if (savedTrains.size < 3) {
                layoutParams.height = (150 * resources.displayMetrics.density).toInt() // 150dp a px
            } else {
                layoutParams.height = (285 * resources.displayMetrics.density).toInt() // 285dp a px
            }

            recyclerView.layoutParams = layoutParams
        }
    }

}
