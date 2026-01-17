package com.example.travelhelper.utils

import android.graphics.Canvas
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.ui.liked_places.RoutesAdapter

class StripedBackgroundDecoration(
    private val evenColor: Int,
    private val oddColor: Int
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter
        
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            
            if (position == RecyclerView.NO_POSITION) continue

            // Если это RoutesAdapter, пропускаем отрисовку полосок для заголовков
            if (adapter is RoutesAdapter) {
                if (adapter.getItemViewType(position) == 0) { // TYPE_HEADER = 0
                    continue
                }
            }

            // Определяем цвет в зависимости от позиции
            val color = if (position % 2 == 0) evenColor else oddColor
            paint.color = color

            // Рисуем фон
            c.drawRect(
                child.left.toFloat(),
                child.top.toFloat(),
                child.right.toFloat(),
                child.bottom.toFloat(),
                paint
            )
        }
    }
}
