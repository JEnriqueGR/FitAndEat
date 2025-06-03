package com.example.fitandeat.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitandeat.R
import android.content.Intent
import android.widget.Button
import com.example.fitandeat.account.UserGoalActivity



class UserFragment : Fragment() {

    private lateinit var tvUserEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        tvUserEmail = view.findViewById(R.id.tvUserEmail)

        // Obtener el correo desde SharedPreferences
        val prefs = requireActivity().getSharedPreferences("usuario", AppCompatActivity.MODE_PRIVATE)
        val email = prefs.getString("correo", "Correo no disponible")
        tvUserEmail.text = email

        val btnCambiarObjetivo = view.findViewById<Button>(R.id.btnCambiarObjetivo)
        btnCambiarObjetivo.setOnClickListener {
            val intent = Intent(requireContext(), UserGoalActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        return view
    }

}