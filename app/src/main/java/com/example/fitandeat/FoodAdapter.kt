package com.example.fitandeat

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(private var listaComidas: List<Food>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    fun actualizarLista(nuevaLista: List<Food>) {
        listaComidas = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comida, parent, false)
        return FoodViewHolder(view)
    }

    override fun getItemCount(): Int = listaComidas.size

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(listaComidas[position])
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(comida: Food) {
            itemView.findViewById<TextView>(R.id.tvNombreComida).text = comida.nombre
            itemView.findViewById<TextView>(R.id.tvCalorias).text = "${comida.calorias} kcal"
            itemView.findViewById<TextView>(R.id.tvMacros).text =
                "P: ${comida.proteinas}g  C: ${comida.carbohidratos}g  G: ${comida.grasas}g"
        }
    }
}
