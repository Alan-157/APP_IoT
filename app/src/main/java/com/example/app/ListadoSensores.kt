package com.example.app

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.HashMap
import java.util.Locale

private lateinit var listaSensores: ListView
private lateinit var listaCompletaSensores: ArrayList<String>
private lateinit var listaVisibleSensores: ArrayList<String>
private lateinit var adapterSensores: ArrayAdapter<String>
private lateinit var searchViewSensores: SearchView
private lateinit var datos: RequestQueue

class ListadoSensores : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listado_sensores)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listaSensores = findViewById(R.id.lista_sensores)
        searchViewSensores = findViewById(R.id.searchView_sensores)
        datos = Volley.newRequestQueue(this)

        // 1. Clic para cambiar estado (ACTIVO/INACTIVO)
        listaSensores.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = listaVisibleSensores[position]
            val datosLinea = item.split(" | ")
            if (datosLinea.size >= 4) {
                val idSensor = datosLinea[0].toIntOrNull() ?: 0
                val estadoActual = datosLinea[3].trim()

                // Toggle simple entre ACTIVO e INACTIVO
                val nuevoEstado = if (estadoActual == "ACTIVO") "INACTIVO" else "ACTIVO"

                mostrarConfirmacionCambio(idSensor, estadoActual, nuevoEstado)
            }
        }

        // 2. Implementación de la búsqueda
        searchViewSensores.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return false }
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText ?: "")
                return true
            }
        })
    }

    override fun onStart() {
        super.onStart()
        CargarListaSensoresDesdeAWS()
    }

    // --- LÓGICA DE CONEXIÓN A AWS (Listar y Cambiar Estado) ---
    private fun CargarListaSensoresDesdeAWS() {
        // LISTADO DE SENSORES
        val url = "http://107.20.82.249/api/listar_sensores.php"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    listaCompletaSensores = ArrayList()
                    listaVisibleSensores = ArrayList()

                    for (i in 0 until response.length()) {
                        val sensor = response.getJSONObject(i)
                        val id = sensor.getInt("id_sensor")
                        val codigo = sensor.getString("codigo_sensor")
                        val tipo = sensor.getString("tipo")
                        val estado = sensor.getString("estado")

                        val linea = "$id | $codigo | $tipo | $estado"
                        listaCompletaSensores.add(linea)
                    }

                    listaVisibleSensores.addAll(listaCompletaSensores)
                    adapterSensores = ArrayAdapter(
                        this, android.R.layout.simple_list_item_1,
                        listaVisibleSensores
                    )
                    listaSensores.adapter = adapterSensores
                } catch (e: JSONException) {
                    mostrarError("Error de Datos", "No se pudo procesar la respuesta del servidor.")
                }
            },
            { error ->
                mostrarAdvertencia("Error de Conexión AWS", "No se pudo obtener la lista de sensores. Verifique el API.")
                simularCargaDeLista()
            }
        )
        datos.add(jsonArrayRequest)
    }

    private fun cambiarEstadoSensor(idSensor: Int, nuevoEstado: String) {
        // CAMBIO DE ESTADO
        val url = "http://107.20.82.249/api/cambiar_estado_sensor.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.trim() == "EXITO") {
                    mostrarExito("Cambio Exitoso", "Sensor $idSensor actualizado a '$nuevoEstado'.")
                    CargarListaSensoresDesdeAWS() // Recargar la lista
                } else {
                    mostrarError("Error API", "Respuesta no esperada de AWS: $response")
                }
            },
            { error ->
                mostrarError("Error de Conexión", "No se pudo conectar a la API de cambio de estado.")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["id_sensor"] = idSensor.toString()
                params["nuevo_estado"] = nuevoEstado
                return params
            }
        }
        datos.add(stringRequest)
    }

    // --- FUNCIONES DE SOPORTE ---

    private fun simularCargaDeLista() {
        listaCompletaSensores = ArrayList()
        listaCompletaSensores.add("101 | A0B1C2D3 | Tarjeta | ACTIVO")
        listaCompletaSensores.add("102 | E4F5G6H7 | Llavero | INACTIVO")
        listaCompletaSensores.add("103 | I8J9K0L1 | Llavero | PERDIDO")

        listaVisibleSensores = ArrayList(listaCompletaSensores)
        adapterSensores = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaVisibleSensores)
        listaSensores.adapter = adapterSensores
    }

    private fun filtrarLista(texto: String) {
        listaVisibleSensores.clear()
        val textoNormalizado = texto.toLowerCase(Locale.getDefault())
        for (item in listaCompletaSensores) {
            if (item.toLowerCase(Locale.getDefault()).contains(textoNormalizado)) {
                listaVisibleSensores.add(item)
            }
        }
        adapterSensores.notifyDataSetChanged()
    }

    private fun mostrarConfirmacionCambio(idSensor: Int, estadoActual: String, nuevoEstado: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Cambiar Estado")
            .setContentText("¿Desea cambiar el estado del Sensor $idSensor de '$estadoActual' a '$nuevoEstado'?")
            .setConfirmText("Sí, Cambiar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                cambiarEstadoSensor(idSensor, nuevoEstado)
            }
            .setCancelClickListener { dialog -> dialog.dismissWithAnimation() }
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

    private fun mostrarAdvertencia(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }
}