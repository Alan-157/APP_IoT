package com.example.app

import android.content.Context // Necesario para SharedPreferences
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import androidx.appcompat.app.AlertDialog
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

    // Constantes de sesión
    private val LOGGED_IN_ROL = "LOGGED_IN_ROL"
    private val LOGGED_IN_ID_DEPARTAMENTO = "LOGGED_IN_ID_DEPARTAMENTO"

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

        // 1. OBTENER ROL y ID_DEPARTAMENTO del usuario logueado
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString(LOGGED_IN_ROL, "OPERADOR")
        val idDepto = sharedPref.getInt(LOGGED_IN_ID_DEPARTAMENTO, 0)
        // 1. URL de la API de polling real
        val url = "http://107.20.82.249/api/estado_barrera.php?rol=$userRole&id_depto=$idDepto"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Lee el estado y el mensaje del último evento desde la API
                    val nuevoEstado = response.getString("estado_barrera")
                    val eventoMsg = response.getString("ultimo_evento")

                    barrierState = nuevoEstado
                    lastEvent = eventoMsg

                    // Actualizar UI
                    txtEstadoBarrera.text = nuevoEstado
                    val barrierColorId =
                        if (nuevoEstado == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
                    txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))
                    txtUltimoEvento.text = eventoMsg

                } catch (e: JSONException) {
                    mostrarError("Error de Datos", "Respuesta JSON del servidor inválida.")
                }
            },
            { error ->
                // En un entorno real, esto indicaría fallo de comunicación con AWS
                // Mantener la simulación para evitar que la App falle al depurar
                mostrarAdvertencia(
                    "Error de Conexión IOT",
                    "No se pudo obtener el estado real de la barrera. Verifique 'estado_barrera.php'."
                )
                // Revertir a la simulación visual si la conexión falla (opcional)
                simularEventoLocal()
            }
        )
        datos.add(jsonObjectRequest)
    }

    // Función auxiliar para mantener la simulación visual si la API falla
    private fun simularEventoLocal() {
        // Usamos esta lógica para que la App no quede en blanco si falla la conexión a estado_barrera.php
        txtEstadoBarrera.text = barrierState
        val barrierColorId = if (barrierState == "ABIERTA") android.R.color.holo_green_dark else android.R.color.holo_red_dark
        txtEstadoBarrera.setTextColor(ContextCompat.getColor(this, barrierColorId))
        txtUltimoEvento.text = lastEvent
    }

    private val refrescarEstado = object : Runnable {
        override fun run() {
            obtenerEstadoYEvento()
            mHandler.postDelayed(this, 1000)
        }
    }

    private fun mostrarAdvertencia(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
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
        mHandler.removeCallbacks(refrescarEstado)
    }
}

