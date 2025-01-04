package com.example.k4rt

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "item_database"
        private const val DATABASE_VERSION = 2

        const val TABLE_ITEMS = "items"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_PRICE = "price"


        const val TABLE_USERS = "users"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createItemsTableQuery = """
            CREATE TABLE $TABLE_ITEMS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_QUANTITY INTEGER,
                $COLUMN_PRICE REAL
            )
        """
        db.execSQL(createItemsTableQuery)

        val createUsersTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USERNAME TEXT PRIMARY KEY,
                $COLUMN_PASSWORD TEXT
            )
        """
        db.execSQL(createUsersTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }


    fun addItem(item: Item): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_PRICE, item.price)
        }
        return db.insert(TABLE_ITEMS, null, values)
    }

    fun getAllItems(): MutableList<Item> {
        val itemList = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ITEMS", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                itemList.add(Item(id.toString(), name, quantity, price))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemList
    }

    fun updateItem(item: Item): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_PRICE, item.price)
        }
        return db.update(TABLE_ITEMS, values, "$COLUMN_ID=?", arrayOf(item.id.toString()))
    }

    fun deleteItem(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_ITEMS, "$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun addUser(username: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password)
        )
        val isUserFound = cursor.count > 0
        cursor.close()
        return isUserFound
    }
}
