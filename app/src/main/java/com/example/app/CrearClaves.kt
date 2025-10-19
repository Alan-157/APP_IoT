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

class CrearClaves : AppCompatActivity() {

    private lateinit var editNuevaClave: EditText
    private lateinit var editRepetirClave: EditText
    private lateinit var btnCrearClaves: Button
    private lateinit var emailUsuario: String

    // Función de validación de robustez (copiada de Registro.kt)
    private fun isPasswordRobust(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$")
        return passwordPattern.matches(password)
    }

    private fun mostrarAdvertencia(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }

    private fun mostrarExito(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Ir a Login")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val intent = Intent(this@CrearClaves, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_claves)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailUsuario = intent.getStringExtra("EMAIL_RECUPERACION") ?: ""

        editNuevaClave = findViewById(R.id.edit_text_nueva_clave)
        editRepetirClave = findViewById(R.id.edit_text_repetir_clave)
        btnCrearClaves = findViewById(R.id.btn_crear_claves)

        btnCrearClaves.setOnClickListener {
            val nuevaClave = editNuevaClave.text.toString().trim()
            val repetirClave = editRepetirClave.text.toString().trim()

            if (nuevaClave.isBlank() || repetirClave.isBlank()) {
                mostrarAdvertencia("Campos Vacíos", "Debe ingresar y repetir la nueva clave.")
            } else if (nuevaClave != repetirClave) {
                mostrarAdvertencia("No Coinciden", "Las claves ingresadas no son idénticas.")
            } else if (!isPasswordRobust(nuevaClave)) {
                mostrarAdvertencia("Contraseña Débil", "La clave no cumple con los requisitos de robustez (8+ caracteres, mayúscula, minúscula, número, especial).")
            } else {
                updatePassword(nuevaClave)
            }
        }
    }

    private fun updatePassword(newClave: String) {
        val helper = ConexionDbHelper(this)
        try {
            helper.updateClave(emailUsuario, newClave)
            mostrarExito("¡Éxito!", "Su contraseña ha sido actualizada correctamente.")
        } catch (e: Exception) {
            mostrarAdvertencia("Error", "No se pudo actualizar la contraseña.")
        }
    }
}