package com.example.app

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

lateinit var fecha: TextView
lateinit var temp: android.widget.TextView
lateinit var hum: android.widget.TextView
lateinit var imagenTemp: ImageView

lateinit var datos: RequestQueue
val mHandler = Handler(Looper.getMainLooper())

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
        fecha=findViewById(R.id.txt_fecha)
        temp=findViewById(R.id.txt_temp)
        hum=findViewById(R.id.txt_humedad)
        imagenTemp=findViewById(R.id.imagen_temp)

        //fecha.text=fechahora()
        datos = Volley.newRequestQueue(this)
        //obtenerDatos()
        mHandler.post(refrescar)
    }
    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat = SimpleDateFormat("dd MMMM YYYY, hh:mm:ss a")
            val strDate: String = sdf.format(c.getTime())
        return strDate
    }

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

    private fun cambiarImagen(valor: Float) {
        if (valor >= 20) {
            imagenTemp.setImageResource(R.drawable.tempalta)
        } else {
            imagenTemp.setImageResource(R.drawable.tempbaja)
        }
    }

        private val refrescar = object : Runnable {
            override fun run() {
                fecha?.text = fechahora() // Asignación de la hora actual
                obtenerDatos() // Llamada a la función que actualiza datos desde la API
                mHandler.postDelayed(this, 1000) // Se vuelve a ejecutar en 1 segundo
            }
        }
}

