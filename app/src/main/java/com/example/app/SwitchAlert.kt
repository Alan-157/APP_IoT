package com.example.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

private lateinit var btn1: Button
private lateinit var btn2: Button
private lateinit var btn3: Button
private lateinit var btn4: Button
private lateinit var btn5: Button
private lateinit var btn6: Button

class SwitchAlert : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_switch_alert)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btn_ingresar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        btn5 = findViewById(R.id.btn5)
        btn6 = findViewById(R.id.btn6)

        btn1.setOnClickListener {
            mostrarAdvertencia()
        }
        btn2.setOnClickListener{
            mostrarExito()
        }
        btn3.setOnClickListener{
            mostrarError()
        }
        btn4.setOnClickListener{
            mostrarCargando()
        }
        btn5.setOnClickListener{
            mostrarConfirmacion()
        }
        btn6.setOnClickListener{
            procesoConCargandoYExito()
        }
    }

    private fun mostrarAdvertencia() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Advertencia")
            .setContentText("Este cambio no se puede deshacer.")
            .setConfirmText("Entiendo")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarExito() {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("¡Éxito!")
            .setContentText("La operación fue realizada correctamente.")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarError() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Algo salió mal. Intenta nuevamente.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarCargando()
    {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.dismissWithAnimation()
        }, 3000) // 3000 milisegundos = 3 segundos
    }

    private fun mostrarConfirmacion() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esta acción eliminará los datos.")
            .setConfirmText("Sí")
            .setCancelText("No")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                mostrarExito() // Aquí podrías ejecutar la acción real
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun procesoConCargandoYExito() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()
// Simular una operación (por ejemplo, guardar datos)
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            pDialog.titleText = "¡Éxito!"
            pDialog.contentText = "La operación se completó correctamente."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
        }, 3000) // Espera 3 segundos simulando un proceso
    }
}