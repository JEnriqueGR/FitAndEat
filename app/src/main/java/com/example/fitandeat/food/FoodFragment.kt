package com.example.fitandeat.food

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitandeat.R
import com.example.fitandeat.database.AppDatabase
import com.example.fitandeat.food.adapter.FoodAdapter
import com.example.fitandeat.food.model.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoodFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodAdapter
    private lateinit var db: AppDatabase
    private lateinit var emailUsuario: String

    private lateinit var tvCaloriasConsumidas: TextView
    private lateinit var progressProteinas: ProgressBar
    private lateinit var progressCarbs: ProgressBar
    private lateinit var progressGrasas: ProgressBar

    private var caloriasObjetivo = 2000 // valor predeterminado, se recalcula
    private var proteinasObjetivo = 120
    private var carbsObjetivo = 250
    private var grasasObjetivo = 70

    private val listaComidas = mutableListOf<Food>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_food, container, false)

        db = AppDatabase.Companion.getDatabase(requireContext())

        val prefs = requireActivity().getSharedPreferences("usuario", AppCompatActivity.MODE_PRIVATE)
        emailUsuario = prefs.getString("correo", "") ?: ""

        tvCaloriasConsumidas = view.findViewById(R.id.tvCaloriasConsumidas)
        progressProteinas = view.findViewById(R.id.progressProteinas)
        progressCarbs = view.findViewById(R.id.progressCarbs)
        progressGrasas = view.findViewById(R.id.progressGrasas)
        recyclerView = view.findViewById(R.id.rvComidas)

        adapter = FoodAdapter(listaComidas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.btnAgregarComida).setOnClickListener {
            startActivity(Intent(requireContext(), AddFoodActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        calcularObjetivosUsuario()
        cargarComidasUsuario()
    }

    private fun calcularObjetivosUsuario() {
        lifecycleScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                db.userDao().obtenerUsuarioPorCorreo(emailUsuario)
            }

            usuario?.let {
                // Calcular calorías con fórmula Mifflin-St Jeor (simplificada)
                val tmb = if (it.sexo.lowercase() == "masculino") {
                    10 * it.peso + 6.25 * it.estatura - 5 * it.edad + 5
                } else {
                    10 * it.peso + 6.25 * it.estatura - 5 * it.edad - 161
                }

                caloriasObjetivo = tmb.toInt()

                when (it.objetivo.lowercase()) {
                    "perder grasa" -> caloriasObjetivo -= 300
                    "mantenerte" -> { /* nada */ }
                    "ganar músculo" -> caloriasObjetivo += 300
                }

                // Asignar macros aproximados
                proteinasObjetivo = (caloriasObjetivo * 0.25 / 4).toInt()
                carbsObjetivo = (caloriasObjetivo * 0.50 / 4).toInt()
                grasasObjetivo = (caloriasObjetivo * 0.25 / 9).toInt()
            }
        }
    }

    private fun cargarComidasUsuario() {
        if (emailUsuario.isBlank()) {
            Toast.makeText(requireContext(), "No se detectó usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val comidas = withContext(Dispatchers.IO) {
                db.foodDao().obtenerComidasPorUsuario(emailUsuario)
            }

            listaComidas.clear()
            listaComidas.addAll(comidas)
            adapter.notifyDataSetChanged()

            actualizarProgreso()
        }
    }

    private fun actualizarProgreso() {
        var totalCalorias = 0
        var totalProteinas = 0
        var totalCarbs = 0
        var totalGrasas = 0

        for (comida in listaComidas) {
            totalCalorias += comida.calorias
            totalProteinas += comida.proteinas
            totalCarbs += comida.carbohidratos
            totalGrasas += comida.grasas
        }

        tvCaloriasConsumidas.text = "$totalCalorias / $caloriasObjetivo kcal"

        progressProteinas.max = proteinasObjetivo
        progressCarbs.max = carbsObjetivo
        progressGrasas.max = grasasObjetivo

        progressProteinas.progress = totalProteinas
        progressCarbs.progress = totalCarbs
        progressGrasas.progress = totalGrasas
    }
}