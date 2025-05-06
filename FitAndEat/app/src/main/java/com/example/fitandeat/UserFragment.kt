package com.example.fitandeat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

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

        return view
    }
}

