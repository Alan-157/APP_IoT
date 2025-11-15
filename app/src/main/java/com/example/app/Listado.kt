package com.example.app

import android.content.Intent
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
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.Locale

private lateinit var listado: ListView
private lateinit var listausuario: ArrayList<String>
private lateinit var listaCompleta: ArrayList<String>
private lateinit var adapter: ArrayAdapter<String>
private lateinit var searchView: SearchView
private lateinit var datos: RequestQueue // Inicializamos Volley

class Listado : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Inicialización de Vistas y Volley
        listado = findViewById(R.id.lista)
        searchView = findViewById(R.id.searchView)
        datos = Volley.newRequestQueue(this) // Inicializar RequestQueue

        // 2. Lógica de click para abrir ModificarEliminar
        listado.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = listausuario[position]
            // Formato esperado de la lista: ID | Nombre Apellido | Email
            val datosLinea = item.split(" | ")
            if (datosLinea.size >= 3) {
                val idusu = datosLinea[0].toIntOrNull() ?: 0
                val nombreCompleto = datosLinea[1] // "Nombre Apellido"
                val nombreApellido = nombreCompleto.split(" ")

                val nombre = nombreApellido.firstOrNull() ?: ""
                val apellido = nombreApellido.drop(1).joinToString(" ") // Maneja apellidos compuestos

                val intent = Intent(this@Listado, ModificarEliminar::class.java).apply {
                    putExtra("Id", idusu)
                    putExtra("Nombre", nombre)
                    putExtra("Apellido", apellido)
                }
                startActivity(intent)
            } else {
                mostrarError("Error de Datos", "Formato de usuario incorrecto: $item")
            }
        }

        // 3. Implementación de la búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText ?: "")
                return true
            }
        })

        // Carga Inicial de la lista
        CargarListaDesdeAWS()
    }

    private fun CargarListaDesdeAWS() {
        // *** IMPORTANTE: REEMPLAZA ESTA URL CON TU ENDPOINT REAL DE AWS ***
        val url = "http://107.20.82.249/api/listar_usuarios.php"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Reinicializar listas antes de la carga
                    listaCompleta = ArrayList()
                    listausuario = ArrayList()

                    for (i in 0 until response.length()) {
                        val usuario = response.getJSONObject(i)
                        val id = usuario.getInt("id_usuario") // Asumiendo que AWS retorna id_usuario
                        val nombre = usuario.getString("nombre")
                        val apellido = usuario.getString("apellido")
                        val email = usuario.getString("email")

                        // Nuevo formato de lista: ID | Nombre Apellido | Email
                        val linea = "$id | $nombre $apellido | $email"
                        listaCompleta.add(linea)
                    }

                    // Copiar la lista completa a la lista visible y configurar el adapter
                    listausuario.addAll(listaCompleta)
                    adapter = ArrayAdapter(
                        this, android.R.layout.simple_list_item_1,
                        listausuario
                    )
                    listado.adapter = adapter
                } catch (e: JSONException) {
                    e.printStackTrace()
                    mostrarError("Error de Datos", "No se pudo procesar la respuesta del servidor.")
                }
            },
            { error ->
                error.printStackTrace()
                // Simulación para mostrar estructura si falla la conexión AWS
                mostrarAdvertencia("Error de Conexión AWS", "No se pudo conectar a la API. Se cargarán datos de ejemplo de SQLite (Solo para referencia de estructura).")
                // Fallback a la antigua función SQLite temporalmente si es necesario, O:
                simularCargaDeLista() // Se debe eliminar el fallback real en la entrega final
            }
        )
        datos.add(jsonArrayRequest)
    }

    // Función de simulación para pruebas si AWS no está lista
    private fun simularCargaDeLista() {
        // ESTO DEBE ELIMINARSE CUANDO EL PROYECTO ESTÉ COMPLETO EN AWS
        listaCompleta = ArrayList()
        listaCompleta.add("1 | Juan Perez | juan@mail.com")
        listaCompleta.add("2 | Ana López | ana@mail.com")
        listaCompleta.add("3 | Admin System | admin@sys.com")

        listausuario = ArrayList(listaCompleta)
        adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            listausuario
        )
        listado.adapter = adapter
    }

    private fun filtrarLista(texto: String) {
        listausuario.clear()
        if (texto.isEmpty()) {
            listausuario.addAll(listaCompleta)
        } else {
            val textoNormalizado = texto.toLowerCase(Locale.getDefault())
            for (item in listaCompleta) {
                if (item.toLowerCase(Locale.getDefault()).contains(textoNormalizado)) {
                    listausuario.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    // SweetAlert para errores
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

    override fun onStart() {
        super.onStart()
        // Recargar la lista al volver de Modificar/Eliminar
        CargarListaDesdeAWS()
    }
}