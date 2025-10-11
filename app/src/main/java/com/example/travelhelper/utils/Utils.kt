package com.example.travelhelper.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

object Utils {

    fun SharedPreferences.edit(block: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        editor.block()
        editor.apply()
    }

    // Пример использования расширений
    fun Context.saveToPrefs(key: String, value: Any) {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Тип не поддерживается")
            }
        }
    }

    inline fun <reified T> Context.getFromPrefs(key: String, defaultValue: T): T {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return when (T::class) {
            String::class -> prefs.getString(key, defaultValue as String) as T
            Int::class -> prefs.getInt(key, defaultValue as Int) as T
            Boolean::class -> prefs.getBoolean(key, defaultValue as Boolean) as T
            Float::class -> prefs.getFloat(key, defaultValue as Float) as T
            Long::class -> prefs.getLong(key, defaultValue as Long) as T
            else -> throw IllegalArgumentException("Тип не поддерживается")
        }
    }
}