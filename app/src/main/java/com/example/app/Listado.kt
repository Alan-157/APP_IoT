package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private lateinit var listado: ListView
private lateinit var listausuario: ArrayList<String>
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

        listado= findViewById(R.id.lista);
        CargarLista()

        listado.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val item = listausuario[position]
                val datos = item.split(" ")
                if (datos.size >= 3) {
                    val idusu = datos[0].toIntOrNull() ?: 0
                    val nombre = datos[1]
                    val apellido = datos[2]
                    val intent = Intent(this@Listado, ModificarEliminar::class.java).apply {
                        putExtra("Id", idusu)
                        putExtra("Nombre", nombre)
                        putExtra("Apellido", apellido) }
                    startActivity(intent)
                }
            }
    }
    private fun listaUsuario(): ArrayList<String> {
        val datos = ArrayList<String>()
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        val sql = "SELECT * FROM USUARIOS"
        val c = db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val linea = "${c.getInt(0)} ${c.getString(1)}${c.getString(2)}"
                datos.add(linea)
            } while (c.moveToNext())
        }
        c.close()
        db.close()
        return datos
    }
    private fun CargarLista() {
        listausuario = listaUsuario()
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            listausuario
        )
        listado.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        CargarLista()
    }
}