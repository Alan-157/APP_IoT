package com.example.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionDbHelper(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Usamos sqlUsuarios para mantener el nombre original del campo
    val sqlUsuarios = "CREATE TABLE USUARIOS (ID INTEGER PRIMARY KEY, NOMBRE TEXT, APELLIDO TEXT, EMAIL TEXT, CLAVE TEXT)";

    //Guarda Email, Codigo de 5 digitos, y tiempo de expiración (timestamp Long)
    val sqlRecuperacion = "CREATE TABLE RECUPERACION (EMAIL TEXT PRIMARY KEY, CODIGO TEXT, EXPIRACION INTEGER)";

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlUsuarios)
        db.execSQL(sqlRecuperacion) // Crear la nueva tabla
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS RECUPERACION") // Asegurarse de eliminar la nueva tabla
        onCreate(db)
    }

    // --- Funciones CRUD y de Soporte Existentes ---

    fun checkEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val sql = "SELECT COUNT(*) FROM USUARIOS WHERE EMAIL = ?"
        val cursor = db.rawQuery(sql, arrayOf(email))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count > 0
    }

    // --- NUEVAS FUNCIONES DE RECUPERACION (Punto 3 & 4) ---

    // Función para guardar o actualizar el código de recuperación y su expiración
    fun saveRecoveryCode(email: String, code: String, expirationTime: Long) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("EMAIL", email)
            put("CODIGO", code)
            put("EXPIRACION", expirationTime)
        }
        // Reemplaza si el email ya existe, o inserta si es nuevo
        db.replace("RECUPERACION", null, values)
        db.close()
    }

    // Función para verificar si el código es correcto y NO ha caducado
    fun checkRecoveryCode(email: String, code: String): Boolean {
        val db = this.readableDatabase
        val currentTime = System.currentTimeMillis()

        // La consulta verifica que el CÓDIGO y el EMAIL coincidan, Y que la EXPIRACION sea mayor al tiempo actual
        val sql = "SELECT COUNT(*) FROM RECUPERACION WHERE EMAIL = ? AND CODIGO = ? AND EXPIRACION > ?"
        val cursor = db.rawQuery(sql, arrayOf(email, code, currentTime.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()

        return count > 0
    }

    // Función para eliminar el código una vez usado o caducado
    fun deleteRecoveryCode(email: String) {
        val db = this.writableDatabase
        db.delete("RECUPERACION", "EMAIL = ?", arrayOf(email))
        db.close()
    }

    //actualizar la clave del usuario (final del proceso de recuperación)
    fun updateClave(email: String, newClave: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("CLAVE", newClave)
        }
        db.update("USUARIOS", values, "EMAIL = ?", arrayOf(email))
        db.close()
    }

    //Obtener Nombre, Apellido, y Email del usuario logueado
    fun getUserData(email: String, clave: String): Map<String, String>? {
        val db = this.readableDatabase
        // Normalizar el email para la búsqueda, ya que al guardar se normaliza
        val normalizedEmail = email.toLowerCase()

        val sql = "SELECT NOMBRE, APELLIDO, EMAIL FROM USUARIOS WHERE EMAIL = ? AND CLAVE = ?"
        val cursor = db.rawQuery(sql, arrayOf(normalizedEmail, clave))

        var userData: Map<String, String>? = null

        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE"))
            val apellido = cursor.getString(cursor.getColumnIndexOrThrow("APELLIDO"))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow("EMAIL"))
            userData = mapOf(
                "nombre" to nombre,
                "apellido" to apellido,
                "email" to userEmail
            )
        }
        cursor.close()
        db.close()
        return userData
    }
    companion object {
        private const val DATABASE_NAME = "CRUD"
        // Cambiar a versión 2 para forzar la ejecución de onUpgrade si ya tienes la versión 1.
        private const val DATABASE_VERSION = 2
    }
}

