package com.example.fitandeat

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment

class RoutineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_routine, container, false)

        val btnStartEmptyWorkout = view.findViewById<Button>(R.id.btnStartEmptyWorkout)
        btnStartEmptyWorkout.setOnClickListener {
            // Aquí mostramos el modal
            val bottomSheet = EntrenamientoBottomSheet()
            bottomSheet.show(parentFragmentManager, "EntrenamientoBottomSheet")
        }

        val btnNuevaRutina = view.findViewById<Button>(R.id.btnNuevaRutina)
        btnNuevaRutina.setOnClickListener {
            val intent = Intent(requireContext(), NewSessionTrainActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
