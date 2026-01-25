package com.example.travelhelper.utils

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.example.travelhelper.R

/**
 * Используем Snackbar, стилизованный под Toast, так как обычные Toast 
 * на новых Android (11+) не позволяют менять фон и цвет текста.
 */
fun View.showCustomToast(message: String) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    
    // Настраиваем внешний вид (белый фон, черный текст)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundResource(R.drawable.bg_custom_toast)
    
    // Находим текстовый элемент внутри Snackbar и красим его в черный
    val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.setTextColor(Color.BLACK)
    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    
    snackbar.show()
}

// Удобная функция для вызова прямо из фрагмента
fun Activity.showCustomToast(message: String) {
    val rootView = this.findViewById<View>(android.R.id.content)
    rootView?.showCustomToast(message)
}
