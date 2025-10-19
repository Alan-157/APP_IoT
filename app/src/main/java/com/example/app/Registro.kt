package com.example.app

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

lateinit var nombre: EditText
lateinit var apellido: EditText
lateinit var email: EditText
lateinit var clave: EditText
lateinit var btn_reg: Button
class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        nombre=findViewById(R.id.txtnombres)
        apellido=findViewById(R.id.txtapellidos)
        email=findViewById(R.id.txtemail)
        clave=findViewById(R.id.txtclave)
        btn_reg=findViewById(R.id.btn_registro)

        btn_reg.setOnClickListener { val nombreText = nombre.text.toString().trim()
            val apellidoText = apellido.text.toString().trim()
            val emailText = email.text.toString().trim()
            val claveText = clave.text.toString().trim()

            if (nombreText.isBlank() || apellidoText.isBlank() ||
                emailText.isBlank() || claveText.isBlank()) {
                mostrarAlertaCamposVacios()
            } else {
                guardar(
                    nombre.text.toString(),
                    apellido.text.toString(),
                    email.text.toString(),
                    clave.text.toString()
                )
            }
        }
    }
    fun guardar(nom: String, ape: String, mai: String, cla: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.readableDatabase
        try {
            val datos = ContentValues().apply {
                put("Nombre", nom)
                put("Apellido", ape)
                put("Email", mai)
                put("Clave", cla)
            }
            db.insert("USUARIOS", null, datos)
            Toast.makeText(this, "Datos ingresados sin problemas", Toast.LENGTH_LONG).show()
            val listado = Intent(this, Listado::class.java)
            startActivity(listado)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}",Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarAlertaCamposVacios() {
        SweetAlertDialog(
            this,
            SweetAlertDialog.WARNING_TYPE
        )
            .setTitleText("Campos incompletos")
            .setContentText("Por favor, complete todos los campos antes de continuar.")
                .setConfirmText("Aceptar")
                .setConfirmClickListener { dialog -> dialog.dismissWithAnimation()
                }
                .show()
    }
}