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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Patterns

class Login : AppCompatActivity() {

    // Se asumen IDs de activity_main.xml:
    private lateinit var txtEmail: EditText
    private lateinit var txtClave: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button // button2
    private lateinit var btnRecuperar: EditText // editTextText2
    private lateinit var datos: RequestQueue

    // --- Funciones de SweetAlert (Reutilizables) ---
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

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !target.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btn_ingresar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Casteo de Vistas (Usando IDs de activity_main.xml)
        txtEmail = findViewById(R.id.editTextTextEmailAddress2)
        txtClave = findViewById(R.id.editTextTextPassword2)
        btnIngresar = findViewById(R.id.btningresar)
        btnRegistrar = findViewById(R.id.button2)
        btnRecuperar = findViewById(R.id.editTextText2)

        datos = Volley.newRequestQueue(this)

        // Lógica del Botón Ingresar
        btnIngresar.setOnClickListener {
            val emailText = txtEmail.text.toString().trim()
            val claveText = txtClave.text.toString().trim()

            // Validaciones (Punto 53)
            if (emailText.isBlank() || claveText.isBlank()) {
                mostrarAdvertencia("Campos Obligatorios", "Por favor, ingrese su email y contraseña.")
            } else if (!isValidEmail(emailText)) {
                mostrarAdvertencia("Formato Inválido", "El formato del email no es correcto.")
            } else {
                consultarDatos(emailText, claveText)
            }
        }

        // Navegación a Registro
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // Navegación a Recuperar Contraseña (PENDIENTE: Crea la actividad 'RecuperarContrasena')
        btnRecuperar.setOnClickListener {
            mostrarAdvertencia("Funcionalidad Pendiente", "Debe implementar la pantalla de Recuperar Contraseña.")
        }
    }

    // Función de Consulta a WebService con Volley (Punto 53)
    private fun consultarDatos(usu: String, pass: String) {
        // *** CAMBIA ESTA URL POR LA URL REAL DE TU SERVICIO ***
        // URL de ejemplo basada en CONSULTAR LOGIN AWS + PHP + MYSQL.pdf
        val url = "http://52.2.255.205/apiconsultausu.php?usu=$usu&pass=$pass"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    val estado = response.getString("estado")
                    if (estado == "0") {
                        // Credenciales inválidas [cite: 344]
                        mostrarError("Error de Acceso", "Credenciales inválidas.")
                    } else {
                        // Login exitoso (Punto 53)
                        SweetAlertDialog(this@Login, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Bienvenido!")
                            .setContentText("Acceso concedido.")
                            .setConfirmText("Continuar")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                val ventana = Intent(this@Login, MenuPrincipal::class.java)
                                startActivity(ventana)
                                finish()
                            }
                            .show()
                    }
                } catch (e: org.json.JSONException) {
                    e.printStackTrace()
                    mostrarError("Error de Servidor", "No se pudo procesar la respuesta.")
                }
            },
            { error ->
                error.printStackTrace()
                mostrarError("Error de Conexión", "No se pudo conectar al servidor.")
            }
        )
        datos.add(request)
    }
}