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

// Se renombra btn1 y btn2 para ser más claros, y se añade el tercer botón
private lateinit var btnCrudUsuario: Button
private lateinit var btnDatosSensor: Button
private lateinit var btnDesarrollador: Button
private lateinit var txtFechaMenu: TextView

class MenuPrincipal : AppCompatActivity() {

    // Función para obtener la fecha/hora en formato dd/MM/yyyy HH:mm:ss (Punto 71)
    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        // El formato de 24h es HH (no hh) [cite: 1127]
        val sdf: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(c.time)
    }

    // Runnable para refrescar el reloj cada 1 segundo (Punto 71)
    private val refrescarReloj = object : Runnable {
        override fun run() {
            txtFechaMenu.text = "Fecha/Hora: " + fechahora()
            Handler(Looper.getMainLooper()).postDelayed(this, 1000) // 1000ms = 1 segundo
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

        // 1. Enlace de vistas
        txtFechaMenu = findViewById(R.id.txt_fecha_menu)
        btnCrudUsuario = findViewById(R.id.btn_crud_usuario)
        btnDatosSensor = findViewById(R.id.btn_datos_sensor)
        btnDesarrollador = findViewById(R.id.btn_desarrollador)

        // 2. Iniciar el reloj (Punto 71)
        Handler(Looper.getMainLooper()).post(refrescarReloj)

        // 3. Lógica de botones (Punto 1129-1131)

        // Opción 1: CRUD de Usuarios (Redirige a la Gestión/Listado del CRUD)
        btnCrudUsuario.setOnClickListener()
        {
            // Redirigimos al Activity Listado o al Menu CRUD si lo llegas a crear
            val listado = Intent(this, CRUD_Usuarios::class.java)
            startActivity(listado)
        }

        // Opción 2: Ver datos de sensores
        btnDatosSensor.setOnClickListener {
            val sensores = Intent(this, DatosDeSensores::class.java)
            startActivity(sensores)
        }

        // Opción 3: Datos del desarrollador (PENDIENTE: Crea el Activity 'Desarrollador')
        btnDesarrollador.setOnClickListener {
            // Reemplaza 'SwitchAlert' por tu Activity 'Desarrollador' cuando lo crees.
            val desarrollador = Intent(this, Desarrollador::class.java) // Usamos SwitchAlert como placeholder
            startActivity(desarrollador)
        }
    }
}