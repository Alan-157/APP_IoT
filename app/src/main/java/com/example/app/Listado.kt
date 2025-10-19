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
import java.util.Locale

private lateinit var listado: ListView
private lateinit var listausuario: ArrayList<String> // Lista visible y filtrada
private lateinit var listaCompleta: ArrayList<String> // Lista original con todos los datos
private lateinit var adapter: ArrayAdapter<String>
private lateinit var searchView: SearchView

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

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        listado = findViewById(R.id.lista);
        searchView = findViewById(R.id.searchView)

        CargarLista()

        // Implementación de la búsqueda (Punto 112)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText ?: "")
                return true
            }
        })

        // Lógica de click para abrir ModificarEliminar
        listado.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = listausuario[position]
            // La lista en este punto contiene: ID Nombre Apellido
            val datos = item.split(" ")
            if (datos.size >= 3) {
                val idusu = datos[0].toIntOrNull() ?: 0
                val nombre = datos[1]
                val apellido = datos[2]
                val intent = Intent(this@Listado, ModificarEliminar::class.java).apply {
                    putExtra("Id", idusu)
                    putExtra("Nombre", nombre)
                    putExtra("Apellido", apellido)
                }
                startActivity(intent)
            }
        }
    }

    private fun filtrarLista(texto: String) {
        listausuario.clear()
        if (texto.isEmpty()) {
            listausuario.addAll(listaCompleta)
        } else {
            // Convierte a minúsculas para la búsqueda (insensible a mayúsculas)
            val textoNormalizado = texto.toLowerCase(Locale.getDefault())
            for (item in listaCompleta) {
                if (item.toLowerCase(Locale.getDefault()).contains(textoNormalizado)) {
                    listausuario.add(item)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun listaUsuario(): ArrayList<String> {
        val datos = ArrayList<String>()
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val sql = "SELECT * FROM USUARIOS"
        val c = db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                // Formato de salida: ID Nombre Apellido Email
                val linea = "${c.getInt(0)} ${c.getString(1)} ${c.getString(2)} ${c.getString(3)}"
                datos.add(linea)
            } while (c.moveToNext())
        }
        c.close()
        db.close()
        return datos
    }

    private fun CargarLista() {
        listaCompleta = listaUsuario()
        listausuario = ArrayList(listaCompleta)
        adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            listausuario
        )
        listado.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        // Recargar la lista para reflejar cambios de Modificar/Eliminar
        CargarLista()
    }
}