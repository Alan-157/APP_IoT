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

private lateinit var btn1: Button
private lateinit var btn2: Button
private lateinit var txtFechaMenu: TextView // Variable para el reloj (ID: txt_fecha_menu)

class MenuPrincipal : AppCompatActivity() {

    // Función de formato de fecha/hora (dd/MM/yyyy HH:mm:ss, 24h) - Punto 71
    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
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

        // Iniciar el reloj
        txtFechaMenu = findViewById(R.id.txt_fecha_menu)
        Handler(Looper.getMainLooper()).post(refrescarReloj)

        btn1=findViewById(R.id.btn_ing) // REGISTRAR USUARIOS
        btn2=findViewById(R.id.btn_lis) // LISTAR USUARIOS

        // Redirección a Registro (El Menú Principal redirige a la Gestión de Usuarios)
        btn1.setOnClickListener()
        {
            val crud = Intent(this, Registro::class.java)
            startActivity(crud)
        }

        // Redirección a Listado
        btn2.setOnClickListener {
            val listado = Intent(this, Listado::class.java)
            startActivity(listado)
        }

        // PENDIENTE: Añadir navegación para DATOS SENSOR (DatosDeSensores.kt) y DESARROLLADOR
    }
}