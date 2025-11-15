package com.example.app

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

private lateinit var btnCrudUsuario: Button
private lateinit var btnDatosSensor: Button
private lateinit var btnGestionSensores: Button // NUEVO: Botón para Admin
private lateinit var btnDesarrollador: Button
private lateinit var txtFechaMenu: TextView

class MenuPrincipal : AppCompatActivity() {

    // FUNCIÓN ESTATICA CORREGIDA (Resuelve el error de compilación)
    companion object {
        fun fechahora(): String {
            val c: Calendar = Calendar.getInstance()
            val sdf: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault())
            return sdf.format(c.time)
        }
    }

    private val refrescarReloj = object : Runnable {
        override fun run() {
            txtFechaMenu.text = "Fecha/Hora: " + fechahora()
            Handler(Looper.getMainLooper()).postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtFechaMenu = findViewById(R.id.txt_fecha_menu)
        btnCrudUsuario = findViewById(R.id.btn_crud_usuario)
        btnDatosSensor = findViewById(R.id.btn_datos_sensor)
        btnGestionSensores = findViewById(R.id.btn_gestion_sensores) // NUEVO ENLACE
        btnDesarrollador = findViewById(R.id.btn_desarrollador)

        Handler(Looper.getMainLooper()).post(refrescarReloj)

        // Opción 1: CRUD de Usuarios
        btnCrudUsuario.setOnClickListener()
        {
            val listado = Intent(this, CRUD_Usuarios::class.java)
            startActivity(listado)
        }

        // Opción 2: CONTROL BARRERA (Datos De Sensores)
        btnDatosSensor.setOnClickListener {
            val sensores = Intent(this, DatosDeSensores::class.java)
            startActivity(sensores)
        }

        // Opción 3: GESTIÓN DE SENSORES (ADMIN)
        btnGestionSensores.setOnClickListener {
            val gestion = Intent(this, GestionSensores::class.java)
            startActivity(gestion)
        }

        // Opción 4: Datos del desarrollador
        btnDesarrollador.setOnClickListener {
            val desarrollador = Intent(this, Desarrollador::class.java)
            startActivity(desarrollador)
        }
    }
}