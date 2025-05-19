package com.example.fitandeat.exercise

import com.example.fitandeat.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.databinding.FragmentTrainingModalRoutineBinding
import com.example.fitandeat.exercise.model.EjercicioRutina
import com.example.fitandeat.exercise.model.Exercise
import com.example.fitandeat.exercise.model.Rutina
import com.example.fitandeat.exercise.model.SavedRoutine
import com.example.fitandeat.exercise.model.SerieRutina
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.gson.Gson
import kotlin.math.abs

class RutinaBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentTrainingModalRoutineBinding? = null
    private val binding get() = _binding!!

    // Variables para el cronómetro
    private var handler: Handler? = null
    private var startTime: Long = 0
    private var timeElapsed: Long = 0
    private var running: Boolean = false

    // Variables de series para guardar entrenamientos en bd
    private val seriesTemporales = mutableListOf<SerieRutina>()

    // Variable para el dobleclick del titulo del entrenamiento
    private var lastClickTime = 0L

    private var newTraining = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingModalRoutineBinding.inflate(inflater, container, false)

        // OCULTAR LA SECCIÓN DE EJERCICIO AL PRINCIPIO
        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false

        // Inicializamos el cronómetro
        startChronometer()

        val rutinaJson = arguments?.getString("rutinaJson")

        if (rutinaJson != null) {
            val rutina = Gson().fromJson(rutinaJson, Rutina::class.java)
            binding.etTituloEntrenamiento.setText(rutina.nombre)
            showRoutine(rutina)

        } else {
            newTraining = true
            updateTrainingInfo()
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
            val modalFragment = ExerciseListActivityRoutine()
            modalFragment.show(parentFragmentManager, "exerciseListModal")
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

        // Establecer el nombre de la rutina según la hora del día
        val trainingName = when {
            currentHour < 12 -> "Rutina por la mañana"
            currentHour in 12..14 -> "Rutina al medio día"
            currentHour in 15..18 -> "Rutina por la tarde"
            else -> "Rutina por la noche"
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

    // Funcion para mostrar los ejercicios que se escogen
    fun showMultipleExercises(ejercicios: List<Exercise>) {
        binding.layoutDetalleEjercicio.removeAllViews()

        ejercicios.forEach { ejercicio ->
            val itemView = layoutInflater.inflate(R.layout.exercise_item_routine, null, false)

            val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreEjercicio)
            val contenedorSeries = itemView.findViewById<LinearLayout>(R.id.contenedorSeries)
            val btnAddSerie = itemView.findViewById<Button>(R.id.btnAddSerie)

            tvNombre.text = ejercicio.nombre

            btnAddSerie.setOnClickListener {
                val numeroNuevaSerie = contenedorSeries.childCount + 1
                addNewSerie(numeroNuevaSerie, contenedorSeries, ejercicio.nombre)
            }

            val fondoRojo = TextView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                text = "Borrar"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                gravity = Gravity.CENTER or Gravity.END
                setPadding(0, 0, 32, 0)
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
            }

            val swipeWrapper = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(fondoRojo)
                addView(itemView)
            }

            var xStart = 0f
            val swipeThreshold = 300f

            itemView.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        xStart = event.x
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - xStart
                        if (deltaX < 0) {
                            itemView.translationX = deltaX * 0.6f
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val deltaX = event.x - xStart
                        view.performClick()

                        if (abs(deltaX) > swipeThreshold) {
                            itemView.animate()
                                .translationX(-itemView.width.toFloat())
                                .setDuration(300)
                                .withEndAction {
                                    binding.layoutDetalleEjercicio.removeView(swipeWrapper)
                                    seriesTemporales.removeAll { it.nombreEjercicio == ejercicio.nombre }
                                    Toast.makeText(requireContext(), "${ejercicio.nombre} eliminado", Toast.LENGTH_SHORT).show()

                                    if (binding.layoutDetalleEjercicio.childCount == 0) {
                                        hideExerciseDetail()
                                    }
                                }
                                .start()
                        } else {
                            itemView.animate()
                                .translationX(0f)
                                .setDuration(200)
                                .start()
                        }
                        true
                    }
                    else -> false
                }
            }

            binding.layoutDetalleEjercicio.addView(swipeWrapper)
        }

        binding.layoutDetalleEjercicio.isVisible = true
        binding.swipeContainer.isVisible = true
    }

    private fun hideExerciseDetail() {
        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false
        Toast.makeText(requireContext(), "Ejercicios borrados", Toast.LENGTH_SHORT).show()
    }

    // Funcion para mostrar la rutina guardada
    private fun showRoutine(rutina: Rutina) {
        binding.layoutDetalleEjercicio.removeAllViews()

        rutina.ejercicios.forEach { ejercicio ->
            val itemView = layoutInflater.inflate(R.layout.exercise_item_routine, null, false)

            val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreEjercicio)
            val contenedorSeries = itemView.findViewById<LinearLayout>(R.id.contenedorSeries)
            val btnAddSerie = itemView.findViewById<Button>(R.id.btnAddSerie)

            tvNombre.text = ejercicio.nombreEjercicio

            ejercicio.series.forEach { serie ->
                addNewSerie(
                    numero = serie.numero,
                    contenedorSeries = contenedorSeries,
                    nombreEjercicio = ejercicio.nombreEjercicio,
                    kg = serie.kg,
                    reps = serie.repeticiones,
                    completada = serie.completada
                )
            }

            btnAddSerie.setOnClickListener {
                val nuevaSerieNumero = contenedorSeries.childCount + 1
                addNewSerie(
                    numero = nuevaSerieNumero,
                    contenedorSeries = contenedorSeries,
                    nombreEjercicio = ejercicio.nombreEjercicio
                )
            }

            val fondoRojo = TextView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                text = "Borrar"
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                gravity = Gravity.CENTER or Gravity.END
                setPadding(0, 0, 32, 0)
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
            }

            val swipeWrapper = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(fondoRojo)
                addView(itemView)
            }

            var xStart = 0f
            val swipeThreshold = 300f

            itemView.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        xStart = event.x
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - xStart
                        if (deltaX < 0) {
                            itemView.translationX = deltaX * 0.6f
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val deltaX = event.x - xStart
                        view.performClick()

                        if (abs(deltaX) > swipeThreshold) {
                            itemView.animate()
                                .translationX(-itemView.width.toFloat())
                                .setDuration(300)
                                .withEndAction {
                                    binding.layoutDetalleEjercicio.removeView(swipeWrapper)
                                    seriesTemporales.removeAll { it.nombreEjercicio == ejercicio.nombreEjercicio }
                                    Toast.makeText(requireContext(), "${ejercicio.nombreEjercicio} eliminado", Toast.LENGTH_SHORT).show()
                                    if (binding.layoutDetalleEjercicio.childCount == 0) {
                                        hideExerciseDetail()
                                    }
                                }
                                .start()
                        } else {
                            itemView.animate()
                                .translationX(0f)
                                .setDuration(200)
                                .start()
                        }
                        true
                    }
                    else -> false
                }
            }

            binding.layoutDetalleEjercicio.addView(swipeWrapper)
        }

        binding.layoutDetalleEjercicio.isVisible = true
        binding.swipeContainer.isVisible = true
    }

    // Función para agregar nueva serie en el botón de agregar serie
    private fun addNewSerie(numero: Int, contenedorSeries: LinearLayout, nombreEjercicio: String, kg: Float? = null, reps: Int? = null, completada: Boolean = false) {

        val nuevaFila = LayoutInflater.from(requireContext())
            .inflate(R.layout.fila_serie, contenedorSeries, false)

        val layoutFilaSerie = nuevaFila.findViewById<LinearLayout>(R.id.layoutFilaSerie)
        val tvSerieNumero = nuevaFila.findViewById<TextView>(R.id.tvSerieNumero)
        val etKg = nuevaFila.findViewById<EditText>(R.id.etKg)
        val etReps = nuevaFila.findViewById<EditText>(R.id.etReps)
        val ivCheck = nuevaFila.findViewById<ImageView>(R.id.ivCheck)

        etKg.setText(if (kg == null || kg == 0f) "" else kg.toString())
        etReps.setText(if (reps == null || reps == 0) "" else reps.toString())

        tvSerieNumero.text = numero.toString()

        var estaCompletada = false

        ivCheck.setBackgroundColor(
            ContextCompat.getColor(requireContext(), android.R.color.transparent)
        )

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

            val serieRutina = SerieRutina(
                numero = numero,
                nombreEjercicio = nombreEjercicio,
                kg = kg,
                repeticiones = reps,
                completada = !estaCompletada
            )

            seriesTemporales.removeIf { it.numero == numero && it.nombreEjercicio == nombreEjercicio }
            seriesTemporales.add(serieRutina)

            ivCheck.setBackgroundColor(
                ContextCompat.getColor(requireContext(),
                    if (serieRutina.completada) R.color.greencheck else android.R.color.transparent)
            )

            val msg = if (serieRutina.completada) "marcada como completada ✅" else "desmarcada ❌"
            Toast.makeText(requireContext(), "Serie $numero $msg", Toast.LENGTH_SHORT).show()

            estaCompletada = serieRutina.completada
        }

        var xStart = 0f
        val swipeThreshold = 200f

        nuevaFila.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    xStart = event.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - xStart
                    if (deltaX < 0) {
                        layoutFilaSerie.translationX = deltaX * 0.6f
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
                                contenedorSeries.removeView(view)
                                seriesTemporales.removeIf { it.numero == numero && it.nombreEjercicio == nombreEjercicio }
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

        contenedorSeries.addView(nuevaFila)
    }

    private fun cancelTrainingAndClose() {
        stopChronometer()
        handler?.removeCallbacksAndMessages(null)
        handler = null

        seriesTemporales.clear()

        // Limpiar todos los hijos de layoutDetalleEjercicio (ya no hay un binding.contenedorSeries fijo)
        binding.layoutDetalleEjercicio.removeAllViews()

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

        // Agrupar y convertir a objetos EjercicioRutina y SerieRutina
        val ejerciciosAgrupados = seriesTemporales
            .groupBy { it.nombreEjercicio }
            .map { (nombre, series) ->
                EjercicioRutina(
                    nombreEjercicio = nombre,
                    series = series.map {
                        SerieRutina(
                            numero = it.numero,
                            nombreEjercicio = it.nombreEjercicio,
                            kg = it.kg,
                            repeticiones = it.repeticiones,
                            completada = it.completada
                        )
                    }
                )
            }

        val rutina = Rutina(
            nombre = nombreEntrenamiento,
            ejercicios = ejerciciosAgrupados
        )

        val rutinaJson = Gson().toJson(rutina)

        val savedRoutine = SavedRoutine(
            email = email,
            rutinaJson = rutinaJson
        )

        val db = AppDatabase.getDatabase(requireContext())
        val rutinaDao = db.savedRoutineDao()

        lifecycleScope.launch {
            rutinaDao.insert(savedRoutine)
            parentFragmentManager.setFragmentResult("saved_routine", Bundle())
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