package com.example.fitandeat.exercise

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.R
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.databinding.FragmentTrainingModalBinding
import com.example.fitandeat.exercise.model.Entrenamiento
import com.example.fitandeat.exercise.model.SavedTrain
import com.example.fitandeat.exercise.model.Serie
import com.example.fitandeat.exercise.model.SesionTrain
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import com.google.gson.Gson

class EntrenamientoBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentTrainingModalBinding? = null
    private val binding get() = _binding!!

    // Variables para el cronómetro
    private var handler: Handler? = null
    private var startTime: Long = 0
    private var timeElapsed: Long = 0
    private var running: Boolean = false

    // Variables de series para guardar entrenamientos en bd
    private val seriesTemporales = mutableListOf<Serie>()

    // Variable para el dobleclick del titulo del entrenamiento
    private var lastClickTime = 0L

    private var newTraining = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingModalBinding.inflate(inflater, container, false)

        // OCULTAR LA SECCIÓN DE EJERCICIO AL PRINCIPIO
        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false

        // Inicializamos el cronómetro
        startChronometer()

        val entrenamientoJson = arguments?.getString("entrenamientoJson")

        if (entrenamientoJson != null) {
            // Carga un entrenamiento ya guardado
            newTraining = false
            val entrenamiento = Gson().fromJson(entrenamientoJson, Entrenamiento::class.java)
            binding.etTituloEntrenamiento.setText(entrenamiento.nombreEntrenamiento)
            binding.tvNombreEjercicio.text = entrenamiento.series.firstOrNull()?.nombreEjercicio ?: "Sin ejercicio"

            binding.layoutDetalleEjercicio.isVisible = true
            binding.swipeContainer.isVisible = true
            binding.btnAnEjercicio.isVisible = false

            entrenamiento.series.forEach { serie ->
                showSeries(serie)
                seriesTemporales.add(serie)
            }

        } else {
            // Se crea nuevo entrenamiento
            newTraining = true
            updateTrainingInfo() // ya tienes esta función
        }

        binding.root.post {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            dialog?.window?.setDimAmount(0f)

            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)

                val screenHeight = resources.displayMetrics.heightPixels
                val expandedHeight = (screenHeight * 0.95).toInt() // 90% de la pantalla
                val collapsedHeightDp = 120
                val density = resources.displayMetrics.density
                val collapsedHeightPx = (collapsedHeightDp * density).toInt()

                // Configuramos la altura inicial directamente al 90%
                sheet.layoutParams.height = expandedHeight

                // Establecer peekHeight (el tamaño del estado colapsado)
                behavior.peekHeight = collapsedHeightPx
                behavior.isHideable = false

                // Aseguramos que el BottomSheet inicie como expandido
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                // Agregar el callback para cambios en el estado
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {

                        if (!isAdded || view == null) return

                        val paramsTitulo = binding.etTituloEntrenamiento.layoutParams as ViewGroup.MarginLayoutParams
                        val paramsDuracion = binding.tvDuracion.layoutParams as ViewGroup.MarginLayoutParams

                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                            dialog?.window?.setDimAmount(0f)

                            // Ocultar elementos
                            binding.btnTerminar.isVisible = false
                            binding.btnAnEjercicio.isVisible = newTraining
                            binding.btnCancelarEntrenamiento.isVisible = false
                            binding.tvFecha.isVisible = false

                            // Centrar título y duración
                            binding.etTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.etTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            paramsTitulo.marginStart = 0
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginStart = 0
                            paramsDuracion.marginEnd = 0
                            binding.etTituloEntrenamiento.layoutParams = paramsTitulo
                            binding.tvDuracion.layoutParams = paramsDuracion

                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                            dialog?.window?.setDimAmount(0f)

                            // Mostrar elementos
                            binding.btnTerminar.isVisible = true
                            binding.btnAnEjercicio.isVisible = newTraining
                            binding.btnCancelarEntrenamiento.isVisible = true
                            binding.tvFecha.isVisible = true

                            // Alinear texto a la izquierda y restaurar márgenes
                            binding.etTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.etTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            paramsTitulo.marginStart = (16 * density).toInt()
                            paramsDuracion.marginStart = (16 * density).toInt()
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginEnd = 0
                            binding.etTituloEntrenamiento.layoutParams = paramsTitulo
                            binding.tvDuracion.layoutParams = paramsDuracion
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                        if (!isAdded || view == null) return

                        val paramsTitulo = binding.etTituloEntrenamiento.layoutParams as ViewGroup.MarginLayoutParams
                        val paramsDuracion = binding.tvDuracion.layoutParams as ViewGroup.MarginLayoutParams

                        // La opacidad base
                        val alpha = slideOffset  // Esto nos da un valor entre 0 (colapsado) y 1 (expandido)

                        // Ajuste de visibilidad de los botones y la fecha, de forma secuencial
                        if (slideOffset < 0.25f) {
                            // Primer botón: btnTerminar
                            binding.btnTerminar.alpha = alpha * 0.4f
                            binding.btnTerminar.isVisible = alpha > 0.2f  // Empieza a aparecer cuando slideOffset > 0.2

                            // Segundo botón: btnAnEjercicio
                            binding.btnAnEjercicio.alpha = alpha * 0.4f
                            binding.btnAnEjercicio.isVisible = newTraining

                            // Tercer botón: btnCancelarEntrenamiento
                            binding.btnCancelarEntrenamiento.alpha = alpha * 0.4f
                            binding.btnCancelarEntrenamiento.isVisible = alpha > 0.2f

                            // Cuarto elemento: tvFecha
                            binding.tvFecha.alpha = alpha * 0.4f
                            binding.tvFecha.isVisible = alpha > 0.2f
                        } else if (slideOffset < 0.5f) {
                            // Entre 0.25 y 0.5: Mostrar más elementos
                            // Primer botón: btnTerminar
                            binding.btnTerminar.alpha = alpha * 0.8f
                            binding.btnTerminar.isVisible = true

                            // Segundo botón: btnAnEjercicio
                            binding.btnAnEjercicio.alpha = alpha * 0.8f
                            binding.btnAnEjercicio.isVisible = newTraining

                            // Tercer botón: btnCancelarEntrenamiento
                            binding.btnCancelarEntrenamiento.alpha = alpha * 0.8f
                            binding.btnCancelarEntrenamiento.isVisible = true

                            // Cuarto elemento: tvFecha
                            binding.tvFecha.alpha = alpha * 0.8f
                            binding.tvFecha.isVisible = true
                        } else {
                            // Cuando el BottomSheet está completamente expandido
                            // Restauramos los valores de los botones
                            binding.btnTerminar.alpha = 1f
                            binding.btnTerminar.isVisible = true

                            binding.btnAnEjercicio.alpha = 1f
                            binding.btnAnEjercicio.isVisible = newTraining

                            binding.btnCancelarEntrenamiento.alpha = 1f
                            binding.btnCancelarEntrenamiento.isVisible = true

                            binding.tvFecha.alpha = 1f
                            binding.tvFecha.isVisible = true
                        }

                        // Ajustar los márgenes según el slideOffset
                        if (slideOffset < 0.15f) {
                            // Cuando está muy cerca de colapsar
                            paramsTitulo.marginStart = 0
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginStart = 0
                            paramsDuracion.marginEnd = 0
                        } else if (slideOffset < 0.25f) {
                            // Cuando se ha deslizado menos de la mitad, centramos los textos
                            binding.etTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.etTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            paramsTitulo.marginStart = 0
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginStart = 0
                            paramsDuracion.marginEnd = 0
                        } else {
                            // Restaurar márgenes originales cuando está más de la mitad
                            binding.etTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.etTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            paramsTitulo.marginStart = (16 * resources.displayMetrics.density).toInt()
                            paramsDuracion.marginStart = (16 * resources.displayMetrics.density).toInt()
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginEnd = 0
                        }

                        binding.etTituloEntrenamiento.layoutParams = paramsTitulo
                        binding.tvDuracion.layoutParams = paramsDuracion
                    }

                })
            }
        }

        dialog?.window?.setDimAmount(0.4f)

        binding.etTituloEntrenamiento.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 500) {
                enableTitleEdition()
            }
            lastClickTime = currentTime
        }

        binding.etTituloEntrenamiento.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                disableTitleEdition()
            }
        }

        binding.nestedScrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (binding.etTituloEntrenamiento.isFocused) {
                    binding.etTituloEntrenamiento.clearFocus()

                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etTituloEntrenamiento.windowToken, 0)
                }
            }
            false
        }

        binding.btnAnEjercicio.setOnClickListener {
            // Crear y mostrar el modal (DialogFragment) para la lista de ejercicios
            val modalFragment = ExerciseListDialogFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .add(modalFragment, "exerciseListModal")
                .commit()
        }

        binding.btnAddSerie.setOnClickListener {
            val numeroNuevaSerie = binding.contenedorSeries.childCount + 1
            addNewSerie(numeroNuevaSerie)
        }

        var xStart = 0f
        val swipeThreshold = 500f  // píxeles para considerar swipe completo

        binding.layoutDetalleEjercicio.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    xStart = event.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - xStart
                    if (deltaX < 0) {
                        binding.layoutDetalleEjercicio.translationX = deltaX * 0.6f
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaX = event.x - xStart

                    // Llama a performClick SIEMPRE en UP para accesibilidad
                    view.performClick()

                    if (abs(deltaX) > swipeThreshold) {
                        binding.layoutDetalleEjercicio.animate()
                            .translationX(-binding.layoutDetalleEjercicio.width.toFloat())
                            .setDuration(500)
                            .setInterpolator(DecelerateInterpolator())
                            .withEndAction {
                                hideExerciseDetail()

                                // Limpiar datos de la serie
                                binding.contenedorSeries.removeAllViews()
                                seriesTemporales.clear()

                                binding.btnAnEjercicio.text = getString(R.string.add_ejercicio)
                                binding.layoutDetalleEjercicio.translationX = 0f
                            }
                            .start()
                    } else {
                        binding.layoutDetalleEjercicio.animate()
                            .translationX(0f)
                            .setDuration(250)
                            .setInterpolator(OvershootInterpolator())
                            .start()
                    }
                    true
                }
                else -> false
            }
        }

        binding.btnCancelarEntrenamiento.setOnClickListener {
            cancelTrainingAndClose()
        }

        binding.btnTerminar.setOnClickListener {
            showFinishTrainingDialog()
        }


        return binding.root
    }

    // Función para actualizar el nombre del entrenamiento y la fecha
    private fun updateTrainingInfo() {
        // Obtener la hora actual
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Establecer el nombre del entrenamiento según la hora del día
        val trainingName = when {
            currentHour < 12 -> "Entrenamiento por la mañana"
            currentHour in 12..14 -> "Entrenamiento al medio día"
            currentHour in 15..18 -> "Entrenamiento por la tarde"
            else -> "Entrenamiento por la noche"
        }

        // Establecer el nombre en el TextView
        binding.etTituloEntrenamiento.setText(trainingName)

        // Obtener la fecha actual y establecerla
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.tvFecha.text = currentDate
    }

    private fun enableTitleEdition() {
        binding.etTituloEntrenamiento.apply {
            isFocusableInTouchMode = true
            isFocusable = true
            isCursorVisible = true
            requestFocus()
        }

        // Mostrar el teclado
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etTituloEntrenamiento, InputMethodManager.SHOW_IMPLICIT)

        // Manejar ENTER o focus perdido
        binding.etTituloEntrenamiento.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                disableTitleEdition()
                true
            } else false
        }

    }

    private fun disableTitleEdition() {
        binding.etTituloEntrenamiento.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isCursorVisible = false
            clearFocus()
            setTextIsSelectable(false)
        }

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etTituloEntrenamiento.windowToken, 0)
    }

    private fun startChronometer() {
        if (!running) {
            startTime = SystemClock.elapsedRealtime() - timeElapsed
            handler = Handler()
            running = true

            val updateRunnable = object : Runnable {
                @SuppressLint("DefaultLocale")
                override fun run() {
                    timeElapsed = SystemClock.elapsedRealtime() - startTime
                    val seconds = (timeElapsed / 1000).toInt() % 60
                    val minutes = (timeElapsed / 1000 / 60).toInt()
                    val timeString = String.format("%02d:%02d", minutes, seconds)
                    binding.tvDuracion.text = timeString
                    handler?.postDelayed(this, 1000) // Repetir cada segundo
                }
            }

            handler?.postDelayed(updateRunnable, 0)
        }
    }

    // Función para detener el cronómetro
    private fun stopChronometer() {
        if (running) {
            // Detener el cronómetro removiendo los callbacks del Handler
            handler?.removeCallbacksAndMessages(null) // Eliminar cualquier acción pendiente
            running = false
        }
    }

    // Funcion para mostrar el layout del exercise training en el modal
    fun showExerciseTraining(exerciseName: String) {
        binding.layoutDetalleEjercicio.isVisible = true
        binding.swipeContainer.isVisible = true

        binding.tvNombreEjercicio.text = exerciseName
        binding.btnAnEjercicio.text = getString(R.string.cambiar_ejercicio)
    }

    private fun hideExerciseDetail() {
        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false
        Toast.makeText(requireContext(), "Ejercicio borrado", Toast.LENGTH_SHORT).show()
    }

    // Funcion que muestra las series en el fragment del entrenamiento ya guardo
    private fun showSeries(serie: Serie) {
        val nuevaFila = LayoutInflater.from(requireContext())
            .inflate(R.layout.fila_serie, binding.contenedorSeries, false)

        val layoutFilaSerie = nuevaFila.findViewById<LinearLayout>(R.id.layoutFilaSerie)
        val tvSerieNumero = nuevaFila.findViewById<TextView>(R.id.tvSerieNumero)
        val etKg = nuevaFila.findViewById<EditText>(R.id.etKg)
        val etReps = nuevaFila.findViewById<EditText>(R.id.etReps)
        val ivCheck = nuevaFila.findViewById<ImageView>(R.id.ivCheck)

        tvSerieNumero.text = serie.numero.toString()
        etKg.setText(serie.kg.toString())
        etReps.setText(serie.repeticiones.toString())

        // Eliminar cualquier color de fondo previo
        ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        var estaCompletada = false
        ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))

        ivCheck.setOnClickListener {
            val kgText = etKg.text.toString()
            val repsText = etReps.text.toString()

            if (kgText.isBlank() || repsText.isBlank()) {
                Toast.makeText(requireContext(), "Llena kg y reps de la serie ${serie.numero}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kg = kgText.toFloatOrNull()
            val reps = repsText.toIntOrNull()

            if (kg == null || reps == null) {
                Toast.makeText(requireContext(), "Valores inválidos en serie ${serie.numero}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (estaCompletada) {
                // Desmarcar
                ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                estaCompletada = false
                Toast.makeText(requireContext(), "Serie ${serie.numero} desmarcada ❌", Toast.LENGTH_SHORT).show()
            } else {
                // Marcar
                ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greencheck))
                estaCompletada = true
                Toast.makeText(requireContext(), "Serie ${serie.numero} marcada como completada ✅", Toast.LENGTH_SHORT).show()
            }
        }

        // 👉 Agregamos swipe para eliminar
        var xStart = 0f
        val swipeThreshold = 200f  // distancia mínima

        nuevaFila.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    xStart = event.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - xStart
                    if (deltaX < 0) {
                        layoutFilaSerie.translationX = deltaX * 0.6f  // mueve solo el layout
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaX = event.x - xStart
                    view.performClick()

                    if (abs(deltaX) > swipeThreshold) {
                        layoutFilaSerie.animate()
                            .translationX(-layoutFilaSerie.width.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                binding.contenedorSeries.removeView(view)
                                Toast.makeText(requireContext(), "Serie ${serie.numero} eliminada", Toast.LENGTH_SHORT).show()
                            }
                            .start()
                    } else {
                        layoutFilaSerie.animate()
                            .translationX(0f)
                            .setDuration(200)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }

        binding.contenedorSeries.addView(nuevaFila)
    }

    // Funcion para agregar nueva serie en el boton de agregar serie
    private fun addNewSerie(numero: Int) {
        val nuevaFila = LayoutInflater.from(requireContext())
            .inflate(R.layout.fila_serie, binding.contenedorSeries, false)

        // Accedemos a las vistas internas
        val layoutFilaSerie = nuevaFila.findViewById<LinearLayout>(R.id.layoutFilaSerie)
        val tvSerieNumero = nuevaFila.findViewById<TextView>(R.id.tvSerieNumero)
        val etKg = nuevaFila.findViewById<EditText>(R.id.etKg)
        val etReps = nuevaFila.findViewById<EditText>(R.id.etReps)
        val ivCheck = nuevaFila.findViewById<ImageView>(R.id.ivCheck)

        tvSerieNumero.text = numero.toString()

        // Creamos la serie inicialmente con completada = false
        val nuevaSerie = Serie(
            numero = numero,
            nombreEjercicio = binding.tvNombreEjercicio.text.toString(),
            kg = 0f, // valores iniciales (los pondrá después el usuario)
            repeticiones = 0,
            completada = false
        )

        // Agregamos la serie a la lista temporal
        seriesTemporales.add(nuevaSerie)

        // 👉 Creamos una variable local para alternar
        var estaCompletada = false

        ivCheck.setOnClickListener {
            val kgText = etKg.text.toString()
            val repsText = etReps.text.toString()

            if (kgText.isBlank() || repsText.isBlank()) {
                Toast.makeText(requireContext(), "Llena kg y reps de la serie $numero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kg = kgText.toFloatOrNull()
            val reps = repsText.toIntOrNull()

            if (kg == null || reps == null) {
                Toast.makeText(requireContext(), "Valores inválidos en serie $numero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val index = seriesTemporales.indexOfFirst { it.numero == numero }
            if (index != -1) {
                if (estaCompletada) {
                    // Si ya estaba completada → desmarcamos
                    ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                    seriesTemporales[index] = seriesTemporales[index].copy(
                        kg = kg,
                        repeticiones = reps,
                        completada = false
                    )
                    estaCompletada = false
                    Toast.makeText(requireContext(), "Serie $numero desmarcada ❌", Toast.LENGTH_SHORT).show()
                } else {
                    // Si no estaba completada → marcamos
                    ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greencheck))
                    seriesTemporales[index] = seriesTemporales[index].copy(
                        kg = kg,
                        repeticiones = reps,
                        completada = true
                    )
                    estaCompletada = true
                    Toast.makeText(requireContext(), "Serie $numero marcada como completada ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // SWIPE para eliminar la fila
        var xStart = 0f
        val swipeThreshold = 200f  // distancia mínima

        nuevaFila.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    xStart = event.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - xStart
                    if (deltaX < 0) {
                        layoutFilaSerie.translationX = deltaX * 0.6f // mueve SOLO el layout, no toda la fila
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaX = event.x - xStart
                    view.performClick()

                    if (abs(deltaX) > swipeThreshold) {
                        layoutFilaSerie.animate()
                            .translationX(-layoutFilaSerie.width.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                binding.contenedorSeries.removeView(view)
                                seriesTemporales.removeIf { it.numero == numero }
                                Toast.makeText(requireContext(), "Serie $numero eliminada", Toast.LENGTH_SHORT).show()
                            }
                            .start()
                    } else {
                        layoutFilaSerie.animate()
                            .translationX(0f)
                            .setDuration(200)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }

        binding.contenedorSeries.addView(nuevaFila)
    }

    private fun cancelTrainingAndClose() {
        stopChronometer()
        handler?.removeCallbacksAndMessages(null)
        handler = null

        seriesTemporales.clear()
        binding.contenedorSeries.removeAllViews()

        timeElapsed = 0L
        startTime = 0L

        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false

        if (isAdded) dismiss()
    }


    private fun showFinishTrainingDialog() {
        if (seriesTemporales.isEmpty()) {
            Toast.makeText(requireContext(), "⚠ Aún no has agregado ninguna serie", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("🎉 ¡Felicitaciones!")
            .setMessage("¿Seguro que deseas terminar el entrenamiento?")
            .setCancelable(false)
            .setNegativeButton("Cancelar") { _, _ ->
                Toast.makeText(requireContext(), "Sigue dándole 💪", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Terminar") { _, _ ->
                showSaveTrainingDialog()
            }
            .create()

        dialog.show()

        // Personalizar botones después de mostrar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.greencheck))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }

    private fun showSaveTrainingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("💾 Guardar entrenamiento")
            .setMessage("¿Quieres guardar tu entrenamiento antes de salir?")
            .setCancelable(false)
            .setNegativeButton("No guardar") { _, _ ->
                cancelTrainingAndClose()
                Toast.makeText(requireContext(), "Entrenamiento Finalizado 🎉", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Guardar") { _, _ ->
                val prefs = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
                val emailUsuario = prefs.getString("correo", "") ?: ""

                if (emailUsuario.isBlank()) {
                    Toast.makeText(requireContext(), "No se detectó usuario activo", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val db = AppDatabase.getDatabase(requireContext())
                lifecycleScope.launch {
                    val user = db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
                    if (user != null) {
                        finishTrainingAndSave(user.email)
                        Toast.makeText(requireContext(), "Entrenamiento guardado 🎯", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error: usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.greencheck))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }

    private fun finishTrainingAndSave(email: String) {
        val nombreEntrenamiento = binding.etTituloEntrenamiento.text.toString()

        if (seriesTemporales.isEmpty()) {
            Toast.makeText(requireContext(), "No hay series para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val entrenamiento = Entrenamiento(
            nombreEntrenamiento = nombreEntrenamiento,
            series = seriesTemporales.toMutableList()
        )

        // Serializar a JSON usando Gson
        val gson = Gson()
        val entrenamientoJson = gson.toJson(entrenamiento)

        val savedTrain = SavedTrain(
            email = email,
            entrenamientoJson = entrenamientoJson,
        )

        val db = AppDatabase.getDatabase(requireContext())
        val savedTrainDao = db.savedTrainDao()
        val sessionDao = db.sessionTrainDao()

        lifecycleScope.launch {
            savedTrainDao.insert(savedTrain)

            // 👉 Insertar también en la tabla de sesiones
            seriesTemporales.forEach { serie ->
                val sesion = SesionTrain(
                    email = email,
                    ejercicio = serie.nombreEjercicio,
                    series = 1,
                    repeticiones = serie.repeticiones,
                    peso = serie.kg
                )
                sessionDao.insertarSesion(sesion)
            }

            // Notificar al fragmento padre
            parentFragmentManager.setFragmentResult("saved_training", Bundle())

            // Limpiar
            seriesTemporales.clear()

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopChronometer()
        _binding = null
    }
}