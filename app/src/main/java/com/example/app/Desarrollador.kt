package com.example.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView // Importar TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Desarrollador : AppCompatActivity() {

    private lateinit var btnGithub: Button
    private lateinit var txtUserNameDinamico: TextView
    private lateinit var txtUserEmailDinamico: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_desarrollador)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Enlazar Vistas Dinámicas
        btnGithub = findViewById(R.id.btn_github)
        txtUserNameDinamico = findViewById(R.id.txt_user_name_dinamico)
        txtUserEmailDinamico = findViewById(R.id.txt_user_email_dinamico)

        // 2. Cargar datos del usuario logueado
        loadLoggedInUserInfo()

        // 3. Lógica para abrir el enlace
        btnGithub.setOnClickListener {
            val url = "https://github.com/tu-usuario-iot" // Cambia esto por tu enlace

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun loadLoggedInUserInfo() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Obtener datos guardados tras el login
        val userName = sharedPref.getString("LOGGED_IN_NAME", "N/A (No Logueado)")
        val userEmail = sharedPref.getString("LOGGED_IN_EMAIL", "N/A (No Logueado)")

        // Actualizar TextViews con los datos del usuario
        txtUserNameDinamico.text = "Nombre: $userName"
        txtUserEmailDinamico.text = "Email: $userEmail"
    }
}