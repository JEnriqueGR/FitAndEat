package com.example.fitandeat

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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.databinding.FragmentTrainingModalBinding
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

    // Variables para el temporizador de serie
    private var countdownHandler: Handler? = null
    private var countdownRunnable: Runnable? = null
    private var isCountdownRunning = false

    // Variables de series para guardar entrenamientos en bd
    private val seriesTemporales = mutableListOf<Serie>()



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingModalBinding.inflate(inflater, container, false)

        // OCULTAR LA SECCIÓN DE EJERCICIO AL PRINCIPIO
        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false

        // Actualiza el texto del entrenamiento y la fecha cuando el BottomSheet es creado
        updateTrainingInfo()

        // Inicializamos el cronómetro
        startChronometer()

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

                        val paramsTitulo = binding.tvTituloEntrenamiento.layoutParams as ViewGroup.MarginLayoutParams
                        val paramsDuracion = binding.tvDuracion.layoutParams as ViewGroup.MarginLayoutParams

                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                            dialog?.window?.setDimAmount(0f)

                            // Ocultar elementos
                            binding.btnTerminar.isVisible = false
                            binding.btnAnEjercicio.isVisible = false
                            binding.btnCancelarEntrenamiento.isVisible = false
                            binding.tvFecha.isVisible = false

                            // Centrar título y duración
                            binding.tvTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.tvTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            paramsTitulo.marginStart = 0
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginStart = 0
                            paramsDuracion.marginEnd = 0
                            binding.tvTituloEntrenamiento.layoutParams = paramsTitulo
                            binding.tvDuracion.layoutParams = paramsDuracion

                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                            dialog?.window?.setDimAmount(0f)

                            // Mostrar elementos
                            binding.btnTerminar.isVisible = true
                            binding.btnAnEjercicio.isVisible = true
                            binding.btnCancelarEntrenamiento.isVisible = true
                            binding.tvFecha.isVisible = true

                            // Alinear texto a la izquierda y restaurar márgenes
                            binding.tvTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.tvTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            paramsTitulo.marginStart = (16 * density).toInt()
                            paramsDuracion.marginStart = (16 * density).toInt()
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginEnd = 0
                            binding.tvTituloEntrenamiento.layoutParams = paramsTitulo
                            binding.tvDuracion.layoutParams = paramsDuracion
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                        if (!isAdded || view == null) return

                        val paramsTitulo = binding.tvTituloEntrenamiento.layoutParams as ViewGroup.MarginLayoutParams
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
                            binding.btnAnEjercicio.isVisible = alpha > 0.2f

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
                            binding.btnAnEjercicio.isVisible = true

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
                            binding.btnAnEjercicio.isVisible = true

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
                            binding.tvTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            binding.tvTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            paramsTitulo.marginStart = 0
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginStart = 0
                            paramsDuracion.marginEnd = 0
                        } else {
                            // Restaurar márgenes originales cuando está más de la mitad
                            binding.tvTituloEntrenamiento.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.tvDuracion.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                            binding.tvTituloEntrenamiento.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            binding.tvDuracion.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            paramsTitulo.marginStart = (16 * resources.displayMetrics.density).toInt()
                            paramsDuracion.marginStart = (16 * resources.displayMetrics.density).toInt()
                            paramsTitulo.marginEnd = 0
                            paramsDuracion.marginEnd = 0
                        }

                        binding.tvTituloEntrenamiento.layoutParams = paramsTitulo
                        binding.tvDuracion.layoutParams = paramsDuracion
                    }

                })
            }
        }

        dialog?.window?.setDimAmount(0.4f)

        binding.btnAnEjercicio.setOnClickListener {
            // Crear y mostrar el modal (DialogFragment) para la lista de ejercicios
            val modalFragment = ExerciseListDialogFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .add(modalFragment, "exerciseListModal")
                .commit()
        }

        // Partes del view
        binding.ivCheck.setOnClickListener {
            startSerie()
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

                                //Añadir funcion para limpiar datos de la serie
                                clearCurrentSerieData()

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
        binding.tvTituloEntrenamiento.text = trainingName

        // Obtener la fecha actual y establecerla
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.tvFecha.text = currentDate
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

    private fun startSerie() {
        val kgText = binding.etKg.text.toString()
        val repsText = binding.etReps.text.toString()
        val nombreEjercicio = binding.tvNombreEjercicio.text.toString()

        // Primero: validamos que no estén vacíos
        if (kgText.isBlank() || repsText.isBlank()) {
            Toast.makeText(requireContext(), "Por favor llena kg y reps", Toast.LENGTH_SHORT).show()
            return
        }

        val kg = binding.etKg.text.toString().toFloat()
        val reps = binding.etReps.text.toString().toInt()

        val nuevaSerie = Serie(
            numero = seriesTemporales.size + 1,
            nombreEjercicio = nombreEjercicio,
            kg = kg,
            repeticiones = reps,
            completada = false  // lo marcas como completada cuando termine el tiempo
        )

        seriesTemporales.add(nuevaSerie)

        if (isCountdownRunning) {
            // Si ya está corriendo, lo reseteamos
            countdownHandler?.removeCallbacks(countdownRunnable!!)
            resetTimerState()
            return
        }

        val totalTimeMillis = 2 * 60 * 1000L  // 2 minutos en milisegundos
        var timeLeft = totalTimeMillis

        // Pintar el fondo verde al arrancar
        binding.ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greenlist))

        isCountdownRunning = true

        countdownHandler = Handler()
        countdownRunnable = Runnable {
            val seconds = (timeLeft / 1000 % 60).toInt()
            val minutes = (timeLeft / 1000 / 60).toInt()
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            binding.tvTemporizador.text = timeString

            // Cambiar color según tiempo
            when {
                timeLeft <= 10 * 1000L -> {
                    binding.tvTemporizador.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                }
                timeLeft <= 45 * 1000L -> {
                    binding.tvTemporizador.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                }
                else -> {
                    binding.tvTemporizador.setTextColor(ContextCompat.getColor(requireContext(), R.color.bblue))
                }
            }

            if (timeLeft > 0) {
                timeLeft -= 1000
                countdownHandler?.postDelayed(countdownRunnable!!, 1000)
            } else {
                // Al terminar, marcar como completada la última serie
                if (seriesTemporales.isNotEmpty()) {
                    val ultima = seriesTemporales.last()
                    val completada = ultima.copy(completada = true)
                    seriesTemporales[seriesTemporales.lastIndex] = completada
                }
                binding.ivCheck.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.greencheck))
            }
        }

        countdownHandler?.post(countdownRunnable!!)
    }


    private fun resetTimerState() {
        isCountdownRunning = false
        binding.layoutFilaSerie.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))  // color original
        binding.ivCheck.setBackgroundColor(0)
        binding.tvTemporizador.text = getString(R.string.temporizador)
        binding.tvTemporizador.setTextColor(resources.getColor(R.color.blue, null))
    }

    private fun clearCurrentSerieData() {
        // Limpiar los campos de texto
        binding.etKg.text?.clear()
        binding.etReps.text?.clear()

        // Reiniciar visuales
        binding.tvTemporizador.text = getString(R.string.temporizador)
        binding.tvTemporizador.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        binding.layoutFilaSerie.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        binding.ivCheck.setBackgroundColor(0)

        // Opcional: si quieres también eliminar la última serie temporal
        if (seriesTemporales.isNotEmpty()) {
            seriesTemporales.removeAt(seriesTemporales.lastIndex)
        }
    }


    private fun hideExerciseDetail() {
        binding.layoutDetalleEjercicio.isVisible = false
        binding.swipeContainer.isVisible = false
        Toast.makeText(requireContext(), "Ejercicio borrado", Toast.LENGTH_SHORT).show()
    }

    private fun cancelTrainingAndClose() {
        // ⚠️ Protegemos acceso a binding
        val safeBinding = _binding ?: return

        // Detener cronómetro general
        stopChronometer()

        // Detener temporizador de serie si está corriendo
        if (isCountdownRunning) {
            countdownHandler?.removeCallbacks(countdownRunnable!!)
            isCountdownRunning = false
        }

        // Limpiar Handlers
        handler?.removeCallbacksAndMessages(null)
        countdownHandler?.removeCallbacksAndMessages(null)

        // Limpiar referencias
        handler = null
        countdownHandler = null
        countdownRunnable = null
        timeElapsed = 0L
        startTime = 0L

        // Limpiar series temporales
        seriesTemporales.clear()

        // Resetear vistas
        safeBinding.layoutDetalleEjercicio.isVisible = false
        safeBinding.swipeContainer.isVisible = false
        safeBinding.etKg.text?.clear()
        safeBinding.etReps.text?.clear()
        safeBinding.tvTemporizador.text = getString(R.string.temporizador)
        safeBinding.tvDuracion.text = getString(R.string.cronometro)

        safeBinding.layoutFilaSerie.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        safeBinding.ivCheck.setBackgroundColor(0)
        safeBinding.tvTemporizador.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))

        // Cerrar el modal solo si está activo
        if (isAdded) {
            dismiss()
        }
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
        val nombreEntrenamiento = binding.tvTituloEntrenamiento.text.toString()

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

        lifecycleScope.launch {
            savedTrainDao.insert(savedTrain)
            Toast.makeText(requireContext(), "Entrenamiento guardado en la base de datos", Toast.LENGTH_SHORT).show()

            // Notificar al fragmento padre
            parentFragmentManager.setFragmentResult("saved_training", Bundle())

            // Limpiar
            seriesTemporales.clear()
            countdownHandler?.removeCallbacksAndMessages(null)
            handler?.removeCallbacksAndMessages(null)
            isCountdownRunning = false
            running = false

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopChronometer()
        _binding = null
    }
}