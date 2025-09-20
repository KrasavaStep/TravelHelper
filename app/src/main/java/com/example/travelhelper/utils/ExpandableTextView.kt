package com.example.travelhelper.utils

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet

class ExpandableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private var isExpanded = false
    private val defaultMaxLines = 5

    init {
        maxLines = defaultMaxLines
        ellipsize = TextUtils.TruncateAt.END
        setOnClickListener { toggle() }
        isClickable = true
        isFocusable = true
    }

    fun toggle() {
        isExpanded = !isExpanded
        if (isExpanded) {
            maxLines = Integer.MAX_VALUE
            ellipsize = null
        } else {
            maxLines = defaultMaxLines
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        if (isExpanded) {
            maxLines = Integer.MAX_VALUE
            ellipsize = null
        } else {
            maxLines = defaultMaxLines
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    fun isExpanded(): Boolean = isExpanded
}