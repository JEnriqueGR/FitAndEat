package com.example.fitandeat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitandeat.databinding.ActivityRoutineCalendarBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*

class RoutineCalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoutineCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutineCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userHasData = true // Aquí conectarías con la DB real
        if (!userHasData) {
            Toast.makeText(this, "No hay datos disponibles", Toast.LENGTH_LONG).show()
            return
        }

        setupChart()
    }

    private fun setupChart() {
        val chart: BarChart = binding.barChart
        val entries = listOf(
            BarEntry(1f, 300f),
            BarEntry(2f, 250f),
            BarEntry(3f, 400f),
            BarEntry(4f, 180f),
        )

        val dataSet = BarDataSet(entries, "Calorías consumidas")
        dataSet.color = resources.getColor(R.color.purple_500, theme)
        val data = BarData(dataSet)
        chart.data = data
        chart.invalidate()
    }
}