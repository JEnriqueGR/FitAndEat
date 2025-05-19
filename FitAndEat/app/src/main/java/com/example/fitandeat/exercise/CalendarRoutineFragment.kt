package com.example.fitandeat.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fitandeat.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class CalendarRoutineFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var textViewInfo: TextView
    private lateinit var chart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar_routine, container, false)
        calendarView = view.findViewById(R.id.calendarView)
        textViewInfo = view.findViewById(R.id.textViewInfo)
        chart = view.findViewById(R.id.barChart)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            textViewInfo.text = "Resumen para el día $selectedDate"

            // Simulación de datos (reemplazar con consulta real)
            val entrenamiento = (1..3).random()
            val comidas = (2..5).random()

            if (entrenamiento == 0 && comidas == 0) {
                Toast.makeText(requireContext(), "No hay datos disponibles", Toast.LENGTH_SHORT).show()
            }

            val entries = listOf(
                BarEntry(0f, entrenamiento.toFloat()),
                BarEntry(1f, comidas.toFloat())
            )

            val dataSet = BarDataSet(entries, "Entrenamientos vs Comidas")
            val data = BarData(dataSet)
            chart.data = data

            val description = Description()
            description.text = ""
            chart.description = description
            chart.invalidate()
        }

        return view
    }
}