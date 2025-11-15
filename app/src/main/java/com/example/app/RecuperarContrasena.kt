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
import java.util.Random
import java.util.HashMap

class RecuperarContrasena : AppCompatActivity() {

    private lateinit var editEmailRecuperar: EditText
    private lateinit var btnRecuperarEnviar: Button
    private lateinit var datos: RequestQueue

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
        setContentView(R.layout.activity_recuperar_contrasena)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        datos = Volley.newRequestQueue(this)
        editEmailRecuperar = findViewById(R.id.edit_text_email_recuperar)
        btnRecuperarEnviar = findViewById(R.id.btn_recuperar_enviar)

        btnRecuperarEnviar.setOnClickListener {
            val emailText = editEmailRecuperar.text.toString().trim()

            if (emailText.isBlank()) {
                mostrarAdvertencia("Campo Obligatorio", "Por favor, ingrese su email.")
            } else if (!isValidEmail(emailText)) {
                mostrarAdvertencia("Formato Inválido", "El formato del email no es correcto.")
            } else {
                processRecoveryRequestAWS(emailText.toLowerCase()) // Llamada a la API
            }
        }
    }

    // FUNCIÓN MODIFICADA: Envia el email a AWS para generar y guardar el código
    private fun processRecoveryRequestAWS(email: String) {
        // URL de tu API para solicitar el código. DEBE SER REEMPLAZADA.
        val url = "http://107.20.82.249/api/solicitar_codigo.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                when (val code = response.trim()) {
                    "NO_EXISTE" -> {
                        mostrarAdvertencia("Email no Encontrado", "El email ingresado no está asociado a ninguna cuenta en AWS.")
                    }
                    "ERROR_DB" -> {
                        mostrarError("Error Servidor", "No se pudo iniciar el proceso de recuperación en la BD.")
                    }
                    else -> {
                        // El API devuelve el código generado (e.g., "12345")
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Código Enviado (Simulación)")
                            .setContentText("El código ($code) ha sido generado y estará activo por 60 segundos. Ingrese el código para continuar.")
                            .setConfirmText("Continuar")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                val intent = Intent(this@RecuperarContrasena, IngresarCodigo::class.java).apply {
                                    putExtra("EMAIL_RECUPERACION", email)
                                    putExtra("CODIGO_GENERADO", code) // Pasamos el código que nos devolvió AWS
                                }
                                startActivity(intent)
                                finish()
                            }
                            .show()
                    }
                }
            },
            { error ->
                mostrarError("Error de Conexión", "No se pudo conectar a la API de recuperación. Error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                // Parámetros que se envían al API
                val params: MutableMap<String, String> = HashMap()
                params["email"] = email
                return params
            }
        }
        datos.add(stringRequest)
    }
}