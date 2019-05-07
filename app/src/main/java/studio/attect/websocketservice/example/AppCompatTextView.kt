package studio.attect.websocketservice.example

import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

fun AppCompatTextView.enableClick(){
    setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
    isFocusable = true
    isClickable = true
}

fun AppCompatTextView.disableClick(){
    setTextColor(ContextCompat.getColor(context, R.color.colorDisable))
    isFocusable = false
    isClickable = false
}