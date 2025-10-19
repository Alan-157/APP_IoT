package com.example.app

import android.content.Context
import android.hardware.camera2.CameraManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import cn.pedant.SweetAlert.SweetAlertDialog

// Variables globales (fecha eliminada)
lateinit var temp: android.widget.TextView
lateinit var hum: android.widget.TextView
lateinit var imagenTemp: ImageView
lateinit var imagenAmpolleta: ImageView // Nueva: para la ampolleta
lateinit var imagenLinterna: ImageView // Nueva: para la linterna

lateinit var datos: RequestQueue
val mHandler = Handler(Looper.getMainLooper())

// Variables de estado
private var isLightOn: Boolean = false // Estado de la ampolleta (UI)
private var isFlashlightOn: Boolean = false // Estado de la linterna (Hardware)


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

        // Inicialización de vistas (DEBES AÑADIR LOS IDs en activity_datos_de_sensores.xml)
        temp=findViewById(R.id.txt_temp)
        hum=findViewById(R.id.txt_humedad)
        imagenTemp=findViewById(R.id.imagen_temp)
        imagenAmpolleta = findViewById(R.id.imagen_ampolleta) // PENDIENTE: Añadir ID en XML
        imagenLinterna = findViewById(R.id.imagen_linterna) // PENDIENTE: Añadir ID en XML

        datos = Volley.newRequestQueue(this)
        mHandler.post(refrescar)

        // Lógica de la Ampolleta (Toggle UI con SweetAlert) - Punto 139
        imagenAmpolleta.setOnClickListener {
            isLightOn = !isLightOn
            if (isLightOn) {
                imagenAmpolleta.setImageResource(R.drawable.ampoencendida)
                mostrarAdvertencia("Ampolleta", "La ampolleta está ENCENDIDA.")
            } else {
                imagenAmpolleta.setImageResource(R.drawable.ampoapagada)
                mostrarAdvertencia("Ampolleta", "La ampolleta está APAGADA.")
            }
        }

        // Lógica de la Linterna (Toggle Hardware) - Punto 139
        imagenLinterna.setOnClickListener {
            toggleFlashlight()
        }
    }

    // Función para manejar el hardware de la linterna (Punto 139)
    private fun toggleFlashlight() {
        if (isFlashlightOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

    private fun turnOnFlashlight() {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
            isFlashlightOn = true
            imagenLinterna.setImageResource(R.drawable.linternaencendida)
        } catch (e: Exception) {
            mostrarError("Error de Linterna", "No se pudo encender. Verifique permisos o hardware.")
        }
    }

    private fun turnOffFlashlight() {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            isFlashlightOn = false
            imagenLinterna.setImageResource(R.drawable.linternaapagada)
        } catch (e: Exception) {
            mostrarError("Error de Linterna", "No se pudo apagar. Verifique permisos o hardware.")
        }
    }

    // ELIMINADA: La función fechahora se movió a MenuPrincipal
    // fun fechahora(): String { ... }

    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    temp?.text = "${response.getString("temperatura")} C"
                    hum?.text = "${response.getString("humedad")} %"

                    val valor = response.getString("temperatura").toFloat()
                    cambiarImagen(valor)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    // Lógica de cambio de imagen de temperatura (>20 alta, <=20 baja) - Punto 139
    private fun cambiarImagen(valor: Float) {
        if (valor > 20) {
            imagenTemp.setImageResource(R.drawable.tempalta) // > 20 C alta
        } else {
            imagenTemp.setImageResource(R.drawable.tempbaja) // <= 20 C baja
        }
    }

    // Runnable sin la actualización de fecha
    private val refrescar = object : Runnable {
        override fun run() {
            // ELIMINADO/COMENTADO: La fecha se actualiza en MenuPrincipal
            obtenerDatos() // Llamada a la función que actualiza datos desde la API
            mHandler.postDelayed(this, 1000) // Se vuelve a ejecutar en 1 segundo
        }
    }

    // SweetAlerts simplificadas
    private fun mostrarAdvertencia(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
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
}

