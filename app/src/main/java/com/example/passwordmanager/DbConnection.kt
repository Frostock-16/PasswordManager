package com.example.passwordmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.IOException
import java.sql.Timestamp

class DbConnection(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PasswordManager.db"
        private const val DATABASE_VERSION = 32

        private const val LOGIN_TABLE = """
            CREATE TABLE IF NOT EXISTS login (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            );
        """

        private const val PASSWORD_DATA_TABLE = """
            CREATE TABLE IF NOT EXISTS password_data(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                sitename TEXT NOT NULL,
                note TEXT,
                date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES login(user_id)
            );
        """

        private const val DELETED_PASSWORD_DATA_TABLE = """
            CREATE TABLE IF NOT EXISTS deleted_password_data(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                sitename TEXT NOT NULL,
                note TEXT,
                date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES login(user_id)
            );
        """

        private const val DROP_LOGIN_TABLE = "DROP TABLE IF EXISTS login"
        private const val DROP_PASSWORD_DATA_TABLE = "DROP TABLE IF EXISTS password_data"
        private const val DROP_DELETED_PASSWORD_DATA_TABLE = "DROP TABLE IF EXISTS deleted_password_data"
    }

    fun insertData(query: String, args: Array<String>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL(query, args)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun insertPasswordData(userid:Int, username: String, password: String, sitename:String, note: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("user_id", userid)
        contentValues.put("username", username)
        contentValues.put("password", password)
        contentValues.put("sitename", sitename)
        contentValues.put("note", note)

        val result = db.insert("password_data", null, contentValues)
        db.close()
        return result
    }

    fun insertDeletedPasswordData(userId:Int, username: String, password: String, sitename:String, note: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("user_id", userId)
        contentValues.put("username", username)
        contentValues.put("password", password)
        contentValues.put("sitename", sitename)
        contentValues.put("note", note)

        val result = db.insert("deleted_password_data", null, contentValues)
        db.close()
        return result
    }

    fun updatePasswordData(id:Int, username: String, password: String, sitename: String, note:String):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("username", username)
        contentValues.put("password", password)
        contentValues.put("sitename", sitename)
        contentValues.put("note", note)

        return db.update("password_data", contentValues, "id=?", arrayOf(id.toString()))
    }

    fun deletePasswordData(id:Int, username:String, password:String, sitename:String, note:String):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("id", id)
            put("username", username)
            put("password", password)
            put("sitename", sitename)
            put("note", note)
        }
        return db.delete("password_data", "id=?", arrayOf(id.toString()))
    }

    fun deletePasswordById(id:Int):Int{
        val db = this.writableDatabase
        return db.delete("password_data", "id=?", arrayOf(id.toString()))
    }
    fun deletePasswordById_Deleted(id:Int):Int{
        val db = this.writableDatabase
        return db.delete("deleted_password_data", "id=?", arrayOf(id.toString()))
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if(oldVersion < newVersion){
//            db.execSQL(DROP_LOGIN_TABLE)
//            db.execSQL(DROP_PASSWORD_DATA_TABLE)
//            db.execSQL(DROP_DELETED_PASSWORD_DATA_TABLE)

            db.execSQL(LOGIN_TABLE)
            db.execSQL(PASSWORD_DATA_TABLE)
            db.execSQL(DELETED_PASSWORD_DATA_TABLE)
        }

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(LOGIN_TABLE)
        db.execSQL(PASSWORD_DATA_TABLE)
        db.execSQL(DELETED_PASSWORD_DATA_TABLE)
    }
}
