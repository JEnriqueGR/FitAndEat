package com.example.fitandeat

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {

    private lateinit var countdownText: TextView
    private lateinit var cycleCounterText: TextView
    private lateinit var startPauseButton: Button
    private lateinit var configureRestButton: Button
    private lateinit var seriesInput: EditText

    private var restTimeMillis = 2 * 60 * 1000L
    private var isRunning = false
    private var inExercisePhase = true
    private var currentCycle = 0
    private var totalCycles = 1

    private var exerciseStartTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var countdownTimer: android.os.CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        countdownText = view.findViewById(R.id.tvCountdown)
        cycleCounterText = view.findViewById(R.id.tvCycleCounter)
        startPauseButton = view.findViewById(R.id.btnStartPause)
        configureRestButton = view.findViewById(R.id.btnConfigurarDescanso)
        seriesInput = view.findViewById(R.id.etSeries)

        startPauseButton.setOnClickListener {
            if (!isRunning && inExercisePhase) {
                startExercise()
            } else if (isRunning && inExercisePhase) {
                endExerciseAndStartRest()
            }
        }

        configureRestButton.setOnClickListener {
            showRestTimeOptions()
        }

        updateCountdownDisplay(0L)
        updateCycleDisplay()

        return view
    }

    private fun startExercise() {
        val inputSeries = seriesInput.text.toString()
        totalCycles = if (inputSeries.isNotBlank()) inputSeries.toInt() else 1

        if (currentCycle >= totalCycles) {
            Toast.makeText(requireContext(), "Ya completaste todas las series", Toast.LENGTH_SHORT).show()
            return
        }

        inExercisePhase = true
        isRunning = true
        exerciseStartTime = System.currentTimeMillis()

        timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - exerciseStartTime
                updateCountdownDisplay(elapsed)
                handler.postDelayed(this, 1000)
            }
        }

        handler.post(timerRunnable!!)
        startPauseButton.text = "Terminar ejercicio"
    }

    private fun endExerciseAndStartRest() {
        handler.removeCallbacks(timerRunnable!!)
        val duration = System.currentTimeMillis() - exerciseStartTime
        Toast.makeText(requireContext(), "Ejercicio duró: ${formatTime(duration)}", Toast.LENGTH_SHORT).show()

        inExercisePhase = false
        isRunning = true
        startPauseButton.text = "Descansando..."

        countdownTimer = object : android.os.CountDownTimer(restTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                currentCycle++
                updateCycleDisplay()
                isRunning = false
                inExercisePhase = true

                if (currentCycle >= totalCycles) {
                    countdownText.text = "¡Completado!"
                    startPauseButton.text = "Iniciar ejercicio"
                } else {
                    startPauseButton.text = "Iniciar ejercicio"
                }
            }
        }.start()
    }

    private fun showRestTimeOptions() {
        val options = arrayOf("30 segundos", "1 minuto", "90 segundos", "2 minutos", "3 minutos")
        val timesInMillis = arrayOf(30L, 60L, 90L, 120L, 180L).map { it * 1000 }

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona tiempo de descanso")
            .setItems(options) { _, which ->
                restTimeMillis = timesInMillis[which]
                Toast.makeText(requireContext(), "Tiempo de descanso actualizado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun updateCountdownDisplay(millis: Long) {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        countdownText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateCycleDisplay() {
        cycleCounterText.text = "Ciclos realizados: $currentCycle"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable ?: Runnable {})
        countdownTimer?.cancel()
    }

    private fun formatTime(millis: Long): String {
        val min = (millis / 1000) / 60
        val seg = (millis / 1000) % 60
        return String.format("%02d:%02d", min, seg)
    }
}
