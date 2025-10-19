package com.example.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionDbHelper(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val sql="CREATE TABLE USUARIOS (ID INTEGER PRIMARY KEY, NOMBRE TEXT, APELLIDO TEXT, EMAIL TEXT, CLAVE TEXT)";
    override fun onCreate(db: SQLiteDatabase) {
        // Aquí va tu SQL para crear tablas
        db.execSQL(sql)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Aquí puedes actualizar tu esquema
        db.execSQL("DROP TABLE USUARIOS")
        onCreate(db)
    }

    // FUNCIÓN: Verificar si el email ya existe (Requerimiento de Unicidad)
    fun checkEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val sql = "SELECT COUNT(*) FROM USUARIOS WHERE EMAIL = ?"
        // Usamos un placeholder (?) para insertar la variable de forma segura
        val cursor = db.rawQuery(sql, arrayOf(email))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count > 0
    }

    companion object {
        private const val DATABASE_NAME = "CRUD"
        private const val DATABASE_VERSION = 1
    }
}

