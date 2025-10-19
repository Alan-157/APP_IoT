package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

lateinit var id: EditText
lateinit var nom: EditText
lateinit var ape: EditText
lateinit var mod: Button
lateinit var elim: Button

class ModificarEliminar : AppCompatActivity() {

    // SweetAlert de Éxito, vuelve a la lista al aceptar
    private fun mostrarExito(title: String, content: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onBackPressedDispatcher.onBackPressed() // Vuelve a Listado
            }
            .show()
    }

    // SweetAlert de Error
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
        setContentView(R.layout.activity_modificar_eliminar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        id = findViewById(R.id.txtid)
        nom = findViewById(R.id.txtnombremod)
        ape = findViewById(R.id.txtapellidomod)
        mod = findViewById(R.id.btn_modificar)
        elim = findViewById(R.id.btn_eliminar)

        val idusu = intent.getIntExtra("Id", 0)
        val nombre = intent.getStringExtra("Nombre") ?: ""
        val apellido = intent.getStringExtra("Apellido") ?: ""

        id.setText(idusu.toString())
        nom.setText(nombre)
        ape.setText(apellido)

        // Lógica de Modificar con SweetAlert de éxito
        mod.setOnClickListener {
            try {
                modificar(idusu, nom.text.toString(), ape.text.toString())
                mostrarExito("Modificación Exitosa", "Los datos del usuario han sido actualizados.")
            } catch (e: Exception) {
                mostrarError("Error al Modificar", "Ocurrió un error: ${e.message}")
            }
        }

        // Lógica de Eliminar con SweetAlert de Confirmación
        elim.setOnClickListener {
            mostrarConfirmacionEliminar(idusu)
        }
    }

    // Muestra la confirmación antes de eliminar
    private fun mostrarConfirmacionEliminar(id: Int) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esta acción eliminará el usuario de forma permanente.")
            .setConfirmText("Sí, Eliminar")
            .setCancelText("No, Cancelar")
            .setConfirmClickListener { dialog ->
                try {
                    eliminar(id)
                    dialog.dismissWithAnimation()
                    mostrarExito("Eliminación Exitosa", "El usuario ha sido eliminado correctamente.")
                } catch (e: Exception) {
                    dialog.dismissWithAnimation()
                    mostrarError("Error al Eliminar", "Ocurrió un error: ${e.message}")
                }
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun modificar(id: Int, nombre: String, apellido: String) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        val sql = "UPDATE USUARIOS SET NOMBRE='$nombre',APELLIDO='$apellido' WHERE ID=$id"
        db.execSQL(sql)
        db.close()
    }

    private fun eliminar(id: Int) {
        val helper = ConexionDbHelper(this)
        val db = helper.writableDatabase
        val sql = "DELETE FROM USUARIOS WHERE ID=$id"
        db.execSQL(sql)
        db.close()
    }
}