package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

lateinit var id: EditText
lateinit var nom: EditText
lateinit var ape: EditText
lateinit var mod: Button
lateinit var elim: Button
class ModificarEliminar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modificar_eliminar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        id = findViewById(R.id.txtid)
        nom = findViewById(R.id.txtnombremod)
        ape = findViewById(R.id.txtapellidomod)
        mod = findViewById(R.id.btn_modificar)
        elim = findViewById(R.id.btn_eliminar)

        val idusu = intent.getIntExtra("Id", 0)
        val nombre = intent.getStringExtra("Nombre") ?: ""
        val apellido = intent.getStringExtra("Apellido") ?: ""

        id.setText(idusu.toString())
        nom.setText(nombre)
        ape.setText(apellido)

        mod.setOnClickListener {
            modificar(idusu, nom.text.toString(),ape.text.toString())
            onBackPressedDispatcher.onBackPressed()
        }
        elim.setOnClickListener { eliminar(idusu)
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun modificar(id: Int, nombre: String, apellido: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        val sql = "UPDATE USUARIOS SET NOMBRE='$nombre',APELLIDO='$apellido' WHERE ID=$id"
        db.execSQL(sql)
        db.close()
    }
    private fun eliminar(id: Int) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        val sql = "DELETE FROM USUARIOS WHERE ID=$id"
        db.execSQL(sql)
        db.close()
    }
}