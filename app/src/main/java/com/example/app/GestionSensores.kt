// alan-157/app_iot/APP_IoT-0fd35a9b9e51fc57284c5c568fb0e9eda6ee5c8d/app/src/main/java/com/example/app/GestionSensores.kt
package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class GestionSensores : AppCompatActivity() {

    private lateinit var editCodigoSensor: EditText
    private lateinit var radioGroupTipo: RadioGroup
    private lateinit var btnRegistrarSensor: Button
    private lateinit var btnGestionarExistentes: Button
    private lateinit var datos: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestion_sensores)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editCodigoSensor = findViewById(R.id.edit_codigo_sensor)
        radioGroupTipo = findViewById(R.id.radio_group_tipo)
        btnRegistrarSensor = findViewById(R.id.btn_registrar_sensor)
        btnGestionarExistentes = findViewById(R.id.btn_gestionar_sensores_existentes)
        datos = Volley.newRequestQueue(this)

        // 1. Lógica para Registrar Nuevo Sensor
        btnRegistrarSensor.setOnClickListener {
            registrarSensor()
        }

        // 2. Lógica para Abrir el listado de sensores (Punto 128-130)
        btnGestionarExistentes.setOnClickListener {
            // Nota: En una versión completa, esto llevaría a un listado filtrado de SENSORES para modificar su estado.
            mostrarAdvertencia("Próxima Implementación", "Redireccionando a la función de listado para simular la Activación/Desactivación. Debe modificarse Listado.kt para mostrar Sensores, no Usuarios.")
        }
    }

    // Función de registro simulado de sensor (debe llamar a la API en AWS)
    private fun registrarSensor() {
        val codigo = editCodigoSensor.text.toString().trim()
        val selectedId = radioGroupTipo.checkedRadioButtonId
        val tipo = findViewById<RadioButton>(selectedId).text.toString()

        if (codigo.isBlank() || codigo.length < 5) {
            mostrarAdvertencia("Error", "Debe ingresar un código UID/MAC válido (mínimo 5 caracteres).")
            return
        }

        // Simulación de llamada a la API de AWS para registrar el sensor
        val urlRegistro = "http://tu.aws.endpoint/registro_sensor.php?codigo=$codigo&tipo=$tipo&estado=ACTIVO"

        // Aquí iría el Volley Request REAL a la API de AWS.
        mostrarExito("Registro Exitoso (Simulación)", "Sensor $codigo ($tipo) registrado en la BD de AWS. Ahora está ACTIVO.")

        // Limpiar campos
        editCodigoSensor.setText("")
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
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog -> dialog.dismissWithAnimation() }
            .show()
    }
}