// alan-157/app_iot/APP_IoT-0fd35a9b9e51fc57284c5c568fb0e9eda6ee5c8d/app/src/main/java/com/example/app/Registro.kt

package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import android.util.Patterns
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.HashMap

// Variables globales declaradas fuera de la clase
lateinit var nombre: EditText
lateinit var apellido: EditText
lateinit var email: EditText
lateinit var clave: EditText
lateinit var clave_repite: EditText
lateinit var btn_reg: Button

private lateinit var datos: RequestQueue // Inicializar Volley RequestQueue

class Registro : AppCompatActivity() {

    // Función de validación de robustez
    private fun isPasswordRobust(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
        return passwordPattern.matches(password)
    }

    // Función para validar el formato de email
    private fun isValidEmail(target: CharSequence?): Boolean {
        return !target.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        datos = Volley.newRequestQueue(this)

        nombre = findViewById(R.id.txtnombres)
        apellido = findViewById(R.id.txtapellidos)
        email = findViewById(R.id.txtemail)
        clave = findViewById(R.id.txtclave)
        clave_repite = findViewById(R.id.txtclave_repite)
        btn_reg = findViewById(R.id.btn_registro)

        btn_reg.setOnClickListener {
            val nombreText = nombre.text.toString().trim()
            val apellidoText = apellido.text.toString().trim()
            val emailText = email.text.toString().trim()
            val claveText = clave.text.toString().trim()
            val repiteText = clave_repite.text.toString().trim()

            // NORMALIZACIÓN: Convertimos a minúsculas antes de validar unicidad
            val normalizedEmail = emailText.toLowerCase()

            if (nombreText.isBlank() || apellidoText.isBlank() ||
                emailText.isBlank() || claveText.isBlank() || repiteText.isBlank()) {
                mostrarAdvertencia("Campos Obligatorios", "Por favor, complete todos los campos.")
            } else if (!isValidEmail(emailText)) {
                mostrarAdvertencia("Formato Inválido", "El formato del E-mail no es correcto.")
            } else if (claveText != repiteText) {
                mostrarAdvertencia("Contraseña No Coincide", "Las contraseñas ingresadas no coinciden.")
            } else if (!isPasswordRobust(claveText)) {
                mostrarAdvertencia("Contraseña Débil", "La clave debe tener al menos 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial.")
            } else {
                // Se llama directamente a la función de registro en AWS
                registrarUsuarioAWS(nombreText, apellidoText, normalizedEmail, claveText)
            }
        }
    }

    // Función MODIFICADA: Ahora usa Volley para enviar datos de registro a AWS
    private fun registrarUsuarioAWS(nom: String, ape: String, mai: String, cla: String) {

        // URL de tu API para registro. DEBE SER REEMPLAZADA.
        val url = "http://107.20.82.249/api/registrar_usuario.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                // Asumiendo que el API devuelve "EXITO" o "DUPLICADO" o "ERROR_DB"
                when (response.trim()) {
                    "EXITO" -> {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Registro Exitoso!")
                            .setContentText("El usuario ha sido registrado en AWS. Inicie sesión.")
                            .setConfirmText("Aceptar")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                val loginIntent = Intent(this@Registro, MainActivity::class.java)
                                startActivity(loginIntent)
                                finish()
                            }
                            .show()
                    }
                    "DUPLICADO" -> {
                        mostrarAdvertencia("Email Duplicado", "El E-mail ingresado ya se encuentra registrado en AWS.")
                    }
                    else -> {
                        mostrarError("Error de Servidor", "Ocurrió un error al guardar en AWS: $response")
                    }
                }
            },
            { error ->
                mostrarError("Error de Conexión", "No se pudo conectar a la API de registro. Error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                // Parámetros que se envían al API (POST data)
                val params: MutableMap<String, String> = HashMap()
                params["nombre"] = nom
                params["apellido"] = ape
                params["email"] = mai
                params["clave"] = cla
                return params
            }
        }
        datos.add(stringRequest)
    }
}