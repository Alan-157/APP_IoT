package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private lateinit var btningresar: Button
private lateinit var btnRegistrarse: Button // Declaramos la variable para el botón de registro

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btn_ingresar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Enlazar botones
        btningresar = findViewById(R.id.btningresar)
        btnRegistrarse = findViewById(R.id.btnRegistrarse) // Enlaza el botón "Registrarse" del XML

        // 2. Lógica para el botón Ingresar (Redirige al menú principal)
        btningresar.setOnClickListener {
            // Actualmente redirige a MenuPrincipal, pero deberías implementar la lógica de Login aquí o en Login.kt
            val intent = Intent(this, MenuPrincipal::class.java)
            startActivity(intent)
        }

        // 3. Lógica para el botón Registrarse (Redirige a Registro)
        btnRegistrarse.setOnClickListener {
            val registroIntent = Intent(this, Registro::class.java)
            startActivity(registroIntent)
        }
    }
}