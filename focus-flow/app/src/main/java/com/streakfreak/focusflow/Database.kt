package com.streakfreak.focusflow


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class Database(context: Context) : SQLiteOpenHelper(context, "streak_freak", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("" +
                "CREATE TABLE UserInfo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, app TEXT, streak INTEGER DEFAULT 0, date TEXT DEFAULT CURRENT_DATE)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UserInfo")
        onCreate(db)
    }

    fun addUser(userName: String, app: String): Long {
        return writableDatabase.use {
            val values = ContentValues().apply {
                put("username", userName)
                put("app", app)
            }
            it.insert("UserInfo", null, values)
        }
    }

//    fun getUserById(userId: Long) = readableDatabase.use {
//        it.rawQuery("SELECT * FROM Users WHERE id = ?", arrayOf(userId.toString())).use { cursor ->
//            if (cursor.moveToFirst()) User(
//                cursor.getLong(0),
//                cursor.getString(1),
//                cursor.getString(2),
//                cursor.getString(3)
//            )
//            else null
//        }
//    }

//    fun updateUser(user: User): Boolean {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put("name", user.name)
//            put("email", user.email)
//            put("dob", user.dob)
//        }
//
//        val rowsUpdated = db.update("UserInfo", values, "id=?", arrayOf(user.id.toString()))
//        db.close()
//
//        return rowsUpdated > 0
//    }

    fun deleteUser(userName: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("UserInfo", "username=?", arrayOf(userName))
        db.close()

        return rowsDeleted > 0
    }

    fun deleteByUserAndApp(userName: String, app: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("UserInfo", "username=? and app=?", arrayOf(userName, app))
        db.close()

        return rowsDeleted > 0
    }

    fun deleteByApp(appName: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("UserInfo", "app=?", arrayOf(appName))
        db.close()

        return rowsDeleted > 0
    }

    fun getUsernamesByApp(db: SQLiteDatabase, appName: String): List<String> {
        val usernames = mutableListOf<String>()
        val query = "SELECT username FROM UserInfo WHERE app = ?"
        val cursor = db.rawQuery(query, arrayOf(appName))

        cursor.use {
            while (it.moveToNext()) {
                usernames.add(it.getString(0))
            }
        }
        return usernames
    }

    fun getAppsByUsername(db: SQLiteDatabase, username: String): List<String> {
        val apps = mutableListOf<String>()
        val query = "SELECT app FROM UserInfo WHERE username = ?"
        val cursor = db.rawQuery(query, arrayOf(username))

        cursor.use {
            while (it.moveToNext()) {
                apps.add(it.getString(0))
            }
        }
        return apps
    }

    fun getUsernameAppPairs(db: SQLiteDatabase): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val query = "SELECT username, app FROM UserInfo"
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val username = it.getString(0)
                val app = it.getString(1)
                result.add(Pair(username, app))
            }
        }
        return result
    }

}


data class User(
    val id: Long,
    val username: String,
    val email: String,
    val dob: String
)