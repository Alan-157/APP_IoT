// alan-157/app_iot/APP_IoT-0fd35a9b9e51fc57284c5c568fb0e9eda6ee5c8d/app/src/main/java/com/example/app/CRUD_Usuarios.kt
package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private lateinit var btnIngresarUsuario: Button
private lateinit var btnListarUsuarios: Button
private lateinit var btnRegistrarDepartamento: Button // NUEVA VARIABLE (Declara la referencia)

class CRUD_Usuarios : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crud_usuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Enlace de vistas
        btnIngresarUsuario = findViewById(R.id.btn_ingresar_usuario)
        btnListarUsuarios = findViewById(R.id.btn_listar_usuarios)
        // CORRECCIÓN: Enlazar el nuevo ID del XML
        btnRegistrarDepartamento = findViewById(R.id.btn_registro_depto)

        // Lógica para Ingresar Usuarios -> Redirige a Registro.kt
        btnIngresarUsuario.setOnClickListener {
            val intentRegistro = Intent(this, Registro::class.java)
            startActivity(intentRegistro)
        }

        // Lógica para Listar Usuarios -> Redirige a Listado.kt
        btnListarUsuarios.setOnClickListener {
            val intentListado = Intent(this, Listado::class.java)
            startActivity(intentListado)
        }

        // Lógica para Registrar Departamento (NUEVA FUNCIÓN ADMIN)
        btnRegistrarDepartamento.setOnClickListener {
            val intentDepto = Intent(this, RegistroDepartamento::class.java)
            startActivity(intentDepto)
        }
    }
}