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

// Variables globales para la UI (Campos y Botones)
private lateinit var txtEmail: EditText
private lateinit var txtClave: EditText
private lateinit var btnIngresar: Button
private lateinit var btnRegistrarse: Button
private lateinit var btnRecuperar: Button

class MainActivity : AppCompatActivity() {

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

        // 1. Enlazar Vistas
        txtEmail = findViewById(R.id.editTextTextEmailAddress2)
        txtClave = findViewById(R.id.editTextTextPassword2)
        btnIngresar = findViewById(R.id.btningresar)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnRecuperar = findViewById(R.id.btnRecuperar)

        // 2. Lógica del Botón Ingresar (Autenticación Local)
        btnIngresar.setOnClickListener {
            val emailText = txtEmail.text.toString().trim()
            val claveText = txtClave.text.toString().trim()

            // Validación de campos
            if (emailText.isBlank() || claveText.isBlank()) {
                mostrarAdvertencia("Campos Obligatorios", "Por favor, ingrese su email y contraseña.")
            } else if (!isValidEmail(emailText)) {
                mostrarAdvertencia("Formato Inválido", "El formato del email no es correcto.")
            } else {
                checkCredentialsLocal(emailText, claveText) // Llama a la autenticación LOCAL
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

    // FUNCIÓN MODIFICADA: Autenticación local contra SQLite y guarda datos del usuario
    private fun checkCredentialsLocal(email: String, clave: String) {
        val helper = ConexionDbHelper(this)

        // Obtener datos del usuario (la función getUserData se encarga de la normalización)
        val userData = helper.getUserData(email, clave)

        if (userData != null) {
            // LOGIN EXITOSO: Guardar datos en SharedPreferences
            val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                // Guardamos el nombre completo (Nombre + Apellido)
                putString("LOGGED_IN_NAME", userData["nombre"] + " " + userData["apellido"])
                putString("LOGGED_IN_EMAIL", userData["email"])
                putBoolean("IS_LOGGED_IN", true)
                apply()
            }

            mostrarExito("¡Bienvenido!", "Acceso concedido.")
        } else {
            mostrarError("Error de Acceso", "Credenciales inválidas. Usuario no encontrado o contraseña incorrecta.")
        }
    }
}