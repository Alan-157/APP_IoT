package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import android.util.Patterns

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
                // *** AQUÍ VA LA LÓGICA DE ENVIAR CÓDIGO Y REDIRIGIR A INGRESO DE CÓDIGO ***
                mostrarAdvertencia("Proceso Pendiente", "Se iniciará la comunicación con el servidor para enviar el código.")
            }
        }
    }
}