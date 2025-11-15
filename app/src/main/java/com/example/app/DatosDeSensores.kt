// alan-157/app_iot/APP_IoT-0fd35a9b9e51fc57284c5c568fb0e9eda6ee5c8d/app/src/main/java/com/example/app/DatosDeSensores.kt
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

// Variables globales actualizadas
private lateinit var btnAbrirBarrera: Button
private lateinit var btnCerrarBarrera: Button
private lateinit var txtEstadoBarrera: TextView
private lateinit var txtUltimoEvento: TextView

private lateinit var datos: RequestQueue
private val mHandler = Handler(Looper.getMainLooper())

// Variables de estado simulado
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

        // 1. Inicialización de vistas
        btnAbrirBarrera = findViewById(R.id.btn_abrir_barrera)
        btnCerrarBarrera = findViewById(R.id.btn_cerrar_barrera)
        txtEstadoBarrera = findViewById(R.id.txt_estado_barrera_dinamico)
        txtUltimoEvento = findViewById(R.id.txt_ultimo_evento_dinamico)

        datos = Volley.newRequestQueue(this)
        mHandler.post(refrescarEstado) // Iniciar el refresco del estado

        // 2. Lógica de Control Manual de Barrera
        btnAbrirBarrera.setOnClickListener {
            controlBarrera("ABRIR_MANUAL")
        }

        btnCerrarBarrera.setOnClickListener {
            controlBarrera("CERRAR_MANUAL")
        }
    }

    // Función para enviar la orden al backend/API (simulado)
    private fun controlBarrera(accion: String) {
        val nuevoEstado = if (accion == "ABRIR_MANUAL") "ABIERTA" else "CERRADA"
        val evento = if (accion == "ABRIR_MANUAL") "Apertura manual enviada." else "Cierre manual enviado."

        // 1. Actualizar el estado (simulado) y registrar evento (simulado)
        barrierState = nuevoEstado
        // Llamada a la función estática de fecha
        lastEvent = "$evento Estado: $nuevoEstado (${MenuPrincipal.fechahora()})"
        txtEstadoBarrera.text = nuevoEstado

        // Uso de ContextCompat.getColor()
        val colorId = if (nuevoEstado == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
        txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, colorId))

        txtUltimoEvento.text = lastEvent

        mostrarExito("Control Manual", "Orden de '$nuevoEstado' enviada al NodeMCU. \nSe registrará en EVENTOS_ACCESO.")
    }

    // Función para obtener el estado y el último evento desde el API (simulado)
    private fun obtenerEstadoYEvento() {
        // SIMULACIÓN DE POLLING PARA CUMPLIR CON EL TIEMPO REAL
        val randomEvent = Random().nextInt(100)

        if (randomEvent < 10) { // Simula un acceso RFID
            val esValido = randomEvent % 2 == 0
            val rfidStatus = if (esValido) "ACCESO_VALIDO" else "ACCESO_RECHAZADO"
            val rfidMsg = if (esValido) "¡Bienvenido! Barrera se abre." else "Acceso denegado (RFID no válido)."

            // Actualizar estado (simulado)
            if (esValido) barrierState = "ABIERTA" else barrierState = "CERRADA"
            // Llamada a la función estática de fecha
            lastEvent = "RFID Evento: $rfidMsg Tipo: $rfidStatus (${MenuPrincipal.fechahora()})"

            // Actualizar UI
            txtEstadoBarrera.text = barrierState
            // Uso de ContextCompat.getColor()
            val barrierColorId = if (barrierState == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))

            txtUltimoEvento.text = lastEvent

            // Simular cierre automático 10s después
            if (esValido) {
                mHandler.postDelayed({
                    barrierState = "CERRADA"
                    txtEstadoBarrera.text = "CERRADA (Auto)"
                    txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                }, 10000)
            }
        } else if (randomEvent < 20) {
            // Simula un cambio de estado de barrera (e.g., por el microcontrolador)
            barrierState = if (barrierState == "ABIERTA") "CERRADA" else "ABIERTA"
            txtEstadoBarrera.text = barrierState
            val barrierColorId = if (barrierState == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))
        } else {
            // Mantiene el estado y solo refresca el texto del evento
            txtEstadoBarrera.text = barrierState
            val barrierColorId = if (barrierState == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
            txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))
        }

        // Actualizar el estado del último evento si cambió
        if (!txtUltimoEvento.text.contains("Evento:")) {
            txtUltimoEvento.text = lastEvent
        }
    }

    // Runnable para refrescar el estado de la barrera cada segundo (simula tiempo real)
    private val refrescarEstado = object : Runnable {
        override fun run() {
            obtenerEstadoYEvento()
            mHandler.postDelayed(this, 1000)
        }
    }

    private fun mostrarAdvertencia(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
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
        mHandler.removeCallbacks(refrescarEstado) // Detener el polling al salir
    }
}

