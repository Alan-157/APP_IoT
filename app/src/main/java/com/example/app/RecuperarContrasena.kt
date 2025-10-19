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
import java.util.Random

class RecuperarContrasena : AppCompatActivity() {

    private lateinit var editEmailRecuperar: EditText
    private lateinit var btnRecuperarEnviar: Button

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

    // Función para generar código de 5 dígitos
    private fun generateRecoveryCode(): String {
        return (10000..99999).random().toString()
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

        editEmailRecuperar = findViewById(R.id.edit_text_email_recuperar)
        btnRecuperarEnviar = findViewById(R.id.btn_recuperar_enviar)

        btnRecuperarEnviar.setOnClickListener {
            val emailText = editEmailRecuperar.text.toString().trim()

            if (emailText.isBlank()) {
                mostrarAdvertencia("Campo Obligatorio", "Por favor, ingrese su email.")
            } else if (!isValidEmail(emailText)) {
                mostrarAdvertencia("Formato Inválido", "El formato del email no es correcto.")
            } else {
                processRecoveryRequest(emailText)
            }
        }
    }

    private fun processRecoveryRequest(email: String) {
        val helper = ConexionDbHelper(this)

        // NORMALIZACIÓN: Convertimos el email a minúsculas para la búsqueda y almacenamiento
        val normalizedEmail = email.toLowerCase()

        // 1. Verificar si el email existe en la BD
        if (!helper.checkEmailExists(normalizedEmail)) {
            mostrarAdvertencia("Email no Encontrado", "El email ingresado no está asociado a ninguna cuenta.")
            return
        }

        // 2. Generar código y tiempo de expiración (60 segundos)
        val code = generateRecoveryCode()
        val expirationTime = System.currentTimeMillis() + 60000

        try {
            // 3. Guardar el código y el tiempo de expiración en la tabla RECUPERACION (usando email normalizado)
            helper.saveRecoveryCode(normalizedEmail, code, expirationTime)

            // 4. Notificar éxito (Simulando envío de correo) y redirigir
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Código Enviado (Simulación)")
                .setContentText("El código ($code) ha sido generado y estará activo por 60 segundos. Ingrese el código para continuar.")
                .setConfirmText("Continuar")
                .setConfirmClickListener { dialog ->
                    dialog.dismissWithAnimation()

                    val intent = Intent(this@RecuperarContrasena, IngresarCodigo::class.java).apply {
                        // ASEGURAMOS DE PASAR EL EMAIL NORMALIZADO A LA SIGUIENTE ACTIVIDAD
                        putExtra("EMAIL_RECUPERACION", normalizedEmail)
                    }

                    startActivity(intent)
                    finish()
                }
                .show()
        } catch (e: Exception) {
            mostrarAdvertencia("Error", "No se pudo iniciar el proceso de recuperación.")
        }
    }
}