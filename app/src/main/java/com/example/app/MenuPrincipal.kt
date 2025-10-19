package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private lateinit var btn1: Button
private lateinit var btn2: Button
class MenuPrincipal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn1=findViewById(R.id.btn_ing)
        btn2=findViewById(R.id.btn_lis)

        btn1.setOnClickListener()
        {
            val registro = Intent(this, Registro::class.java)
            startActivity(registro)
        }

        btn2.setOnClickListener {
            val listado = Intent(this, Listado::class.java)
            startActivity(listado)
        }
    }
}