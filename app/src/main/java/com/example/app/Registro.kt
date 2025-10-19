package com.example.app

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import android.util.Patterns // Importado para validar el formato de email

// Variables globales declaradas fuera de la clase
lateinit var nombre: EditText
lateinit var apellido: EditText
lateinit var email: EditText
lateinit var clave: EditText
lateinit var clave_repite: EditText
lateinit var btn_reg: Button


class Registro : AppCompatActivity() {

    // Función de validación de robustez (Punto 59: >=8, Mayús, Minús, Número, Especial)
    private fun isPasswordRobust(password: String): Boolean {
        // Expresión regular para 8+ caracteres, Mayús, Minús, Número y Carácter Especial
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

            val helper = ConexionDbHelper(this)

            if (nombreText.isBlank() || apellidoText.isBlank() ||
                emailText.isBlank() || claveText.isBlank() || repiteText.isBlank()) {
                mostrarAdvertencia("Campos Obligatorios", "Por favor, complete todos los campos.")
            } else if (!isValidEmail(emailText)) {
                mostrarAdvertencia("Formato Inválido", "El formato del E-mail no es correcto.")
            } else if (claveText != repiteText) {
                mostrarAdvertencia("Contraseña No Coincide", "Las contraseñas ingresadas no coinciden.")
            } else if (!isPasswordRobust(claveText)) {
                mostrarAdvertencia("Contraseña Débil", "La clave debe tener al menos 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial.")
            } else if (helper.checkEmailExists(emailText)) {
                mostrarAdvertencia("Email Duplicado", "El E-mail ingresado ya se encuentra registrado.")
            } else {
                guardar(nombreText, apellidoText, emailText, claveText)
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

            // SweetAlert de éxito y redirigir a Login (MainActivity) - Punto 59
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("¡Registro Exitoso!")
                .setContentText("El usuario ha sido registrado. Inicie sesión.")
                .setConfirmText("Aceptar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()
                    val loginIntent = Intent(this, MainActivity::class.java)
                    startActivity(loginIntent)
                    finish()
                }
                .show()

        } catch (e: Exception) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error de Servidor")
                .setContentText("Ocurrió un error al guardar: ${e.message}")
                .setConfirmText("Cerrar")
                .show()
        } finally {
            db.close()
        }
    }
}