package com.example.kalamesazi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class WordsDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_WORDS = "Words"
        private const val COLUMN_LEVEL = "level"
        private const val COLUMN_WORD = "word"
        private const val TAG = "WordsDbHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating database table if not exists")

        val createTableSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_WORDS (
                $COLUMN_LEVEL INTEGER,
                $COLUMN_WORD TEXT
            );
        """
        db.execSQL(createTableSql)

        // Insert default words
        insertDefaultWords(db)
    }

    private fun insertDefaultWords(db: SQLiteDatabase) {
        Log.d(TAG, "Inserting default words")
        val words = listOf(
            Pair(1, "BOOK"),
            Pair(2, "HOUSE"),
            Pair(3, "GARDEN")
        )

        for (word in words) {
            val insertSql = """
                INSERT INTO $TABLE_WORDS ($COLUMN_LEVEL, $COLUMN_WORD) VALUES (${word.first}, '${word.second}');
            """
            db.execSQL(insertSql)
            Log.d(TAG, "Inserted word: Level ${word.first}, Word ${word.second}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun getWordForLevel(level: Int): String? {
        Log.d(TAG, "Fetching word for level: $level")
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT $COLUMN_WORD FROM $TABLE_WORDS WHERE $COLUMN_LEVEL = ?", arrayOf(level.toString()))
        return if (cursor.moveToFirst()) {
            val word = cursor.getString(0)
            Log.d(TAG, "Fetched word: $word")
            word
        } else {
            Log.d(TAG, "No word found for level: $level")
            null
        }.also {
            cursor.close()
        }
    }
}
