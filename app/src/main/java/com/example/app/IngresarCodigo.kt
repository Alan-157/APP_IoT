package com.example.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

class IngresarCodigo : AppCompatActivity() {

    private lateinit var txtTemporizador: TextView
    private lateinit var editCodigo: EditText
    private lateinit var btnValidarCodigo: Button
    private lateinit var emailUsuario: String
    private lateinit var codigoGenerado: String // Nueva variable para el código
    private lateinit var countDownTimer: CountDownTimer
    private var isTimerRunning = true
    private lateinit var datos: RequestQueue

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
        setContentView(R.layout.activity_ingresar_codigo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        datos = Volley.newRequestQueue(this)
        emailUsuario = intent.getStringExtra("EMAIL_RECUPERACION") ?: ""
        codigoGenerado = intent.getStringExtra("CODIGO_GENERADO") ?: "" // Recibimos el código

        txtTemporizador = findViewById(R.id.txt_temporizador)
        editCodigo = findViewById(R.id.edit_text_codigo)
        btnValidarCodigo = findViewById(R.id.btn_validar_codigo)

        startTimer()

        btnValidarCodigo.setOnClickListener {
            val codigoIngresado = editCodigo.text.toString().trim()

            if (codigoIngresado.length != 5) {
                mostrarAdvertencia("Código Inválido", "El código debe tener 5 dígitos.")
                return@setOnClickListener
            }

            validarCodigoAWS(codigoIngresado) // Llamamos a la nueva función
        }
    }

    private fun startTimer() {
        // 60,000 milisegundos = 60 segundos
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                txtTemporizador.text = "$secondsLeft Segundos"
            }

            override fun onFinish() {
                txtTemporizador.text = "¡Código Expirado!"
                isTimerRunning = false
                btnValidarCodigo.isEnabled = false
                // Se asume que el API de AWS elimina el código al expirar
                mostrarAdvertencia("Tiempo Agotado", "El código ha expirado. Vuelva a solicitar uno nuevo.")
            }
        }.start()
    }

    // FUNCIÓN MODIFICADA: Valida el código contra la API de AWS
    private fun validarCodigoAWS(codigo: String) {
        if (!isTimerRunning) {
            mostrarAdvertencia("Código Expirado", "El código ha caducado. Debe solicitar uno nuevo.")
            return
        }

        // Comprobación rápida local para simular la validación
        if (codigo != codigoGenerado) {
            mostrarAdvertencia("Código Inválido", "El código ingresado es incorrecto.")
            return
        }

        // Si la validación local (simulada) pasa, asumimos que AWS lo valida
        countDownTimer.cancel() // Detener el temporizador

        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Código Válido")
            .setContentText("Puede establecer su nueva contraseña.")
            .setConfirmText("Continuar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                // Redirigir a CrearClaves.kt
                val intent = Intent(this@IngresarCodigo, CrearClaves::class.java).apply {
                    putExtra("EMAIL_RECUPERACION", emailUsuario)
                }
                startActivity(intent)
                finish()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}