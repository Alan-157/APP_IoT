package com.example.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import cn.pedant.SweetAlert.SweetAlertDialog
import java.util.Random

private lateinit var btnAbrirBarrera: Button
private lateinit var btnCerrarBarrera: Button
private lateinit var txtEstadoBarrera: TextView
private lateinit var txtUltimoEvento: TextView

private lateinit var datos: RequestQueue
private val mHandler = Handler(Looper.getMainLooper())

private var barrierState: String = "CERRADA"
private var lastEvent: String = "Ningún evento registrado."


class DatosDeSensores : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_datos_de_sensores)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnAbrirBarrera = findViewById(R.id.btn_abrir_barrera)
        btnCerrarBarrera = findViewById(R.id.btn_cerrar_barrera)
        txtEstadoBarrera = findViewById(R.id.txt_estado_barrera_dinamico)
        txtUltimoEvento = findViewById(R.id.txt_ultimo_evento_dinamico)

        datos = Volley.newRequestQueue(this)
        mHandler.post(refrescarEstado)

        btnAbrirBarrera.setOnClickListener {
            controlBarrera("ABRIR_MANUAL")
        }

        btnCerrarBarrera.setOnClickListener {
            controlBarrera("CERRAR_MANUAL")
        }
    }

    private fun controlBarrera(accion: String) {
        val nuevoEstado = if (accion == "ABRIR_MANUAL") "ABIERTA" else "CERRADA"
        val evento = if (accion == "ABRIR_MANUAL") "Apertura manual enviada." else "Cierre manual enviado."

        barrierState = nuevoEstado
        // LLAMADA ESTÁTICA CORREGIDA A LA FUNCIÓN DE FECHA
        lastEvent = "$evento Estado: $nuevoEstado (${MenuPrincipal.Companion.fechahora()})"
        txtEstadoBarrera.text = nuevoEstado

        val colorId = if (nuevoEstado == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
        txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, colorId))

        txtUltimoEvento.text = lastEvent

        // Aquí iría el Volley Request POST REAL a control_barrera.php
        mostrarExito("Control Manual", "Orden de '$nuevoEstado' enviada al NodeMCU. \nSe registrará en EVENTOS_ACCESO.")
    }

    private fun obtenerEstadoYEvento() {
        val randomEvent = Random().nextInt(100)

        if (randomEvent < 10) {
            val esValido = randomEvent % 2 == 0
            val rfidStatus = if (esValido) "ACCESO_VALIDO" else "ACCESO_RECHAZADO"
            val rfidMsg = if (esValido) "¡Bienvenido! Barrera se abre." else "Acceso denegado (RFID no válido)."

            if (esValido) barrierState = "ABIERTA" else barrierState = "CERRADA"
            lastEvent = "RFID Evento: $rfidMsg Tipo: $rfidStatus (${MenuPrincipal.Companion.fechahora()})"

            txtEstadoBarrera.text = barrierState
            val barrierColorId = if (barrierState == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))
            txtUltimoEvento.text = lastEvent

            if (esValido) {
                mHandler.postDelayed({
                    barrierState = "CERRADA"
                    txtEstadoBarrera.text = "CERRADA (Auto)"
                    txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                }, 10000)
            }
        } else {
            txtEstadoBarrera.text = barrierState
            val barrierColorId = if (barrierState == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))
        }

        if (!txtUltimoEvento.text.contains("Evento:")) {
            txtUltimoEvento.text = lastEvent
        }
    }

    private val refrescarEstado = object : Runnable {
        override fun run() {
            obtenerEstadoYEvento()
            mHandler.postDelayed(this, 1000)
        }
    }

    private fun mostrarExito(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }

    private fun mostrarError(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(refrescarEstado)
    }
}

