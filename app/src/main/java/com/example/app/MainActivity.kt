package com.example.app

import android.content.Context
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

// Variables globales para la UI (Campos y Botones)
private lateinit var txtEmail: EditText
private lateinit var txtClave: EditText
private lateinit var btnIngresar: Button
private lateinit var btnRegistrarse: Button
private lateinit var btnRecuperar: Button
private lateinit var datos: RequestQueue

class MainActivity : AppCompatActivity() {

    // Constantes para SharedPreferences (ampliadas)
    private val LOGGED_IN_NAME = "LOGGED_IN_NAME"
    private val LOGGED_IN_EMAIL = "LOGGED_IN_EMAIL"
    private val IS_LOGGED_IN = "IS_LOGGED_IN"
    private val LOGGED_IN_ROL = "LOGGED_IN_ROL"
    private val LOGGED_IN_ID_DEPARTAMENTO = "LOGGED_IN_ID_DEPARTAMENTO"

    private fun mostrarError(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }

    private fun mostrarExito(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Continuar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val ventana = Intent(this@MainActivity, MenuPrincipal::class.java)
                startActivity(ventana)
                finish()
            }
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

        datos = Volley.newRequestQueue(this)

        // 1. Enlazar Vistas
        txtEmail = findViewById(R.id.editTextTextEmailAddress2)
        txtClave = findViewById(R.id.editTextTextPassword2)
        btnIngresar = findViewById(R.id.btningresar)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnRecuperar = findViewById(R.id.btnRecuperar)

        // 2. Lógica del Botón Ingresar (Autenticación AWS)
        btnIngresar.setOnClickListener {
            val emailText = txtEmail.text.toString().trim()
            val claveText = txtClave.text.toString().trim()

            if (emailText.isBlank() || claveText.isBlank()) {
                mostrarError("Campos Obligatorios", "Por favor, ingrese su email y contraseña.")
            } else if (!isValidEmail(emailText)) {
                mostrarError("Formato Inválido", "El formato del email no es correcto.")
            } else {
                checkCredentialsAWS(emailText.toLowerCase(), claveText)
            }
        }

        // 3. Lógica para Registrarse
        btnRegistrarse.setOnClickListener {
            val registroIntent = Intent(this, Registro::class.java)
            startActivity(registroIntent)
        }

        // 4. Lógica para Recuperar Contraseña
        btnRecuperar.setOnClickListener {
            val intent = Intent(this, RecuperarContrasena::class.java)
            startActivity(intent)
        }
    }

    // FUNCIÓN FINAL: Implementa la validación de estado y lectura robusta
    private fun checkCredentialsAWS(email: String, clave: String) {

        // CORRECCIÓN CRÍTICA DE LA URL: Apunta al endpoint de login.php en AWS
        val url = "http://107.20.82.249/api/login.php?email=$email&clave=$clave"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val estado = response.optString("estado", "0")

                    if (estado == "1") {
                        // **VALIDACIÓN CRÍTICA DEL ESTADO DEL USUARIO**
                        val userStatus = response.optString("estado", "INACTIVO") // Lee la columna 'estado' de la BD

                        if (userStatus == "INACTIVO" || userStatus == "BLOQUEADO") {
                            // Punto 8: Usuarios desactivados no deben permitir el funcionamiento
                            mostrarError("Acceso Denegado", "Su cuenta se encuentra inactiva o bloqueada.")
                            return@JsonObjectRequest
                        }

                        // LOGIN EXITOSO: Lectura Robusta de datos de sesión
                        val nombre = response.optString("nombre", "")
                        val apellido = response.optString("apellido", "")
                        val userEmail = response.optString("email", "")

                        val rol = response.optString("rol", "OPERADOR")
                        val idDepartamentoRaw = response.optString("id_departamento", "0")
                        val idDepartamento = idDepartamentoRaw.toIntOrNull() ?: 0

                        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putString(LOGGED_IN_NAME, "$nombre $apellido")
                            putString(LOGGED_IN_EMAIL, userEmail)
                            putBoolean(IS_LOGGED_IN, true)

                            putString(LOGGED_IN_ROL, rol)
                            putInt(LOGGED_IN_ID_DEPARTAMENTO, idDepartamento)

                            apply()
                        }

                        mostrarExito("¡Bienvenido!", "Acceso concedido desde AWS.")
                    } else {
                        mostrarError("Error de Acceso", "Credenciales inválidas. Usuario no encontrado o contraseña incorrecta.")
                    }
                } catch (e: JSONException) {
                    mostrarError("Error de Servidor", "No se pudo procesar la respuesta JSON: ${e.message}.")
                }
            },
            { error ->
                mostrarError("Error de Conexión", "No se pudo conectar a la API de autenticación. Verifique la conexión a AWS.")
            }
        )
        datos.add(jsonObjectRequest)
    }
}