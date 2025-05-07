package com.example.fitandeat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.fitandeat.databinding.ActivityExerciseListBinding


class ExerciseListDialogFragment : DialogFragment() {

    private lateinit var adapter: ExerciseSelectionAdapter
    private lateinit var allEjercicios: List<Exercise>
    private var filteredEjercicios: List<Exercise> = listOf()
    private var selectedExercise: Exercise? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivityExerciseListBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_modal)

        val recyclerView = binding.rvEjercicios
        val searchBar = binding.searchView
        val spinner = binding.spMuscleFilter
        val btnAddExercise = binding.btnAddExercise
        val btnClose = binding.btnClose

        allEjercicios = ExercisesRepository.obtenerEjercicios()

        filteredEjercicios = allEjercicios // Inicialmente mostramos todos los ejercicios

        // Inicializamos el adaptador
        adapter = ExerciseSelectionAdapter(requireContext(), filteredEjercicios) { selectedExercise ->
            this.selectedExercise = selectedExercise
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar el Spinner
        val muscleGroups = listOf("Todos", "Pecho", "Espalda", "Hombro", "Bíceps", "Tríceps", "Pierna", "Abdomen")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, muscleGroups)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // Filtrar por grupos musculares
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGroup = muscleGroups[position]
                filteredEjercicios = if (selectedGroup == "Todos") {
                    allEjercicios
                } else {
                    allEjercicios.filter { it.musculo == selectedGroup }
                }
                // Aquí solo actualizamos los datos
                adapter.updateData(filteredEjercicios, selectedExercise)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Filtrar por texto en el SearchView
        val searchEditText = searchBar.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()

                // Filtrar los ejercicios por nombre
                filteredEjercicios = allEjercicios.filter {
                    it.nombre.contains(searchText, ignoreCase = true)
                }

                // Filtrar también por grupo muscular
                val selectedGroup = spinner.selectedItem.toString()
                if (selectedGroup != "Todos") {
                    filteredEjercicios = filteredEjercicios.filter { it.musculo == selectedGroup }
                }

                // Actualizamos solo los datos sin cambiar la selección
                adapter.updateData(filteredEjercicios, selectedExercise)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Funcionalidad para el botón "Anadir"
        btnAddExercise.setOnClickListener {
            val exercise = selectedExercise
            if (exercise == null) {
                Toast.makeText(requireContext(), "Por favor selecciona un ejercicio", Toast.LENGTH_SHORT).show()
            } else {
                val exerciseName = exercise.nombre

                // Opcional: enviar globalmente (por si alguien más escucha)
                val bundle = Bundle()
                bundle.putString("selectedExerciseName", exerciseName)
                parentFragmentManager.setFragmentResult("exercise_selection", bundle)

                // Buscar el BottomSheet y llamar la función
                val bottomSheet = parentFragmentManager.findFragmentByTag("EntrenamientoBottomSheet") as? EntrenamientoBottomSheet
                bottomSheet?.showExerciseTraining(exerciseName)

                // Cerrar el modal
                dismiss()
            }
        }

        // Funcionalidad para el botón "X"
        btnClose.setOnClickListener {
            dismiss()  // Cierra el DialogFragment
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 90% del ancho
        val height = (resources.displayMetrics.heightPixels * 0.8).toInt() // 80% de la altura
        dialog?.window?.setLayout(width, height)
    }
}
