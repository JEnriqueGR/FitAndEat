package com.example.fitandeat.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fitandeat.R
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.exercise.model.SesionTrain
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var chartReps: BarChart
    private lateinit var chartPeso: LineChart
    private lateinit var chartComparativa: BarChart
    private lateinit var chartPie: PieChart

    private lateinit var tvTotalSesiones: TextView
    private lateinit var tvTotalReps: TextView
    private lateinit var tvTotalPeso: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        chartReps = view.findViewById(R.id.chartReps)
        chartPeso = view.findViewById(R.id.chartPeso)
        chartComparativa = view.findViewById(R.id.chartComparativa)
        chartPie = view.findViewById(R.id.chartPie)

        tvTotalSesiones = view.findViewById(R.id.tvTotalSesiones)
        tvTotalReps = view.findViewById(R.id.tvTotalReps)
        tvTotalPeso = view.findViewById(R.id.tvTotalPeso)


        db = AppDatabase.Companion.getDatabase(requireContext())
        val prefs = requireActivity().getSharedPreferences("usuario", AppCompatActivity.MODE_PRIVATE)
        val correo = prefs.getString("correo", "") ?: ""

        lifecycleScope.launch {
            val sesiones = withContext(Dispatchers.IO) {
                db.sessionTrainDao().obtenerSesionesPorUsuario(correo)
            }

            if (sesiones.isNotEmpty()) {
                mostrarGraficoRepeticiones(sesiones)
                mostrarGraficoPeso(sesiones)
                mostrarGraficoComparativa(sesiones)
                mostrarGraficoPie(sesiones)

                // Calcular resumen
                val totalReps = sesiones.sumOf { it.repeticiones }
                val totalPeso = sesiones.sumOf { it.peso.toDouble() }

                tvTotalSesiones.text = "Sesiones registradas: ${sesiones.size}"
                tvTotalReps.text = "Repeticiones totales: $totalReps"
                tvTotalPeso.text = "Peso total levantado: ${"%.1f".format(totalPeso)} kg"

            } else {
                Toast.makeText(requireContext(), "No hay sesiones para mostrar.", Toast.LENGTH_SHORT).show()
            }

        }

        return view
    }

    private fun mostrarGraficoRepeticiones(sesiones: List<SesionTrain>) {
        val entries = sesiones.mapIndexed { index, sesion ->
            BarEntry(index.toFloat(), sesion.repeticiones.toFloat())
        }
        val dataSet = BarDataSet(entries, "Repeticiones").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }
        chartReps.data = BarData(dataSet)
        chartReps.setFitBars(true)
        chartReps.invalidate()
    }

    private fun mostrarGraficoPeso(sesiones: List<SesionTrain>) {
        val entries = sesiones.mapIndexed { index, sesion ->
            Entry(index.toFloat(), sesion.peso)
        }
        val dataSet = LineDataSet(entries, "Peso levantado").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
        }
        chartPeso.data = LineData(dataSet)
        chartPeso.invalidate()
    }

    private fun mostrarGraficoComparativa(sesiones: List<SesionTrain>) {
        val entriesSeries = ArrayList<BarEntry>()
        val entriesReps = ArrayList<BarEntry>()

        sesiones.forEachIndexed { index, sesion ->
            entriesSeries.add(BarEntry(index.toFloat(), sesion.series.toFloat()))
            entriesReps.add(BarEntry(index.toFloat(), sesion.repeticiones.toFloat()))
        }

        val dataSetSeries = BarDataSet(entriesSeries, "Series").apply { color = Color.GREEN }
        val dataSetReps = BarDataSet(entriesReps, "Repeticiones").apply { color = Color.MAGENTA }

        chartComparativa.data = BarData(dataSetSeries, dataSetReps)
        chartComparativa.groupBars(0f, 0.3f, 0.05f)
        chartComparativa.invalidate()
    }

    private fun mostrarGraficoPie(sesiones: List<SesionTrain>) {
        val ejercicioMap = sesiones.groupBy { it.ejercicio }
        val entries = ejercicioMap.map { (ejercicio, lista) ->
            PieEntry(lista.sumOf { it.series + it.repeticiones }.toFloat(), ejercicio)
        }

        val dataSet = PieDataSet(entries, "Esfuerzo por ejercicio").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
        }

        chartPie.data = PieData(dataSet)
        chartPie.invalidate()
    }
}