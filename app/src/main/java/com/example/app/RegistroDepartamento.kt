package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.HashMap
import androidx.appcompat.app.AlertDialog

class RegistroDepartamento : AppCompatActivity() {

    private lateinit var editNombreDepto: EditText // Usaremos este campo para 'numero'
    private lateinit var editBloqueTorre: EditText // Usaremos este campo para 'torre'
    private lateinit var btnRegistrarDepto: Button
    private lateinit var datos: RequestQueue

    // ... (funciones SweetAlerts) ...

    private fun mostrarAdvertencia(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarExito(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                // You can add an action here, like closing the activity
                dialog.dismiss()
                finish() // Optional: closes the current screen after showing the success message
            }
            .setCancelable(false) // Optional: prevents the user from closing the dialog by tapping outside
            .show()
    }

    private fun mostrarError(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_departamento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        datos = Volley.newRequestQueue(this)
        // Usamos los IDs existentes del layout:
        editNombreDepto = findViewById(R.id.edit_nombre_depto)
        editBloqueTorre = findViewById(R.id.edit_bloque_torre)
        btnRegistrarDepto = findViewById(R.id.btn_registrar_depto)

        btnRegistrarDepto.setOnClickListener {
            // El campo 'nombre' del layout ahora es 'numero' en la BD
            val numero = editNombreDepto.text.toString().trim()
            val torre = editBloqueTorre.text.toString().trim()

            if (numero.isBlank()) {
                mostrarAdvertencia("Campo Obligatorio", "Debe ingresar el número del departamento.")
            } else {
                registrarDepartamentoAWS(numero, torre) // Llamada con los nuevos nombres de campo
            }
        }
    }

    private fun registrarDepartamentoAWS(numero: String, torre: String) {
        val url = "http://107.20.82.249/api/registrar_departamento.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                when (response.trim()) {
                    "EXITO" -> {
                        mostrarExito("Registro Exitoso", "El departamento $numero ha sido creado.")
                        editNombreDepto.setText("")
                        editBloqueTorre.setText("")
                    }
                    "DUPLICADO" -> {
                        mostrarAdvertencia("Error", "El número de departamento ya existe.")
                    }
                    else -> {
                        mostrarError("Error Servidor", "Fallo al registrar: $response")
                    }
                }
            },
            { error ->
                mostrarError("Error de Conexión", "No se pudo conectar a la API de registro de departamento.")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                // ENVÍO DE PARÁMETROS: Coinciden con el script PHP
                params["numero_depto"] = numero
                params["torre"] = torre
                return params
            }
        }
        datos.add(stringRequest)
    }
}