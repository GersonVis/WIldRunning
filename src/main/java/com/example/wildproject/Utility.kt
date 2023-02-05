package com.example.wildproject

import android.animation.ObjectAnimator
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.core.content.ContextCompat

object Utility {
    fun setHeightLinearLayout(ly: LinearLayout, value: Int): Unit {
        val params: LinearLayout.LayoutParams = ly.layoutParams as LayoutParams
        params.height = value
        ly.layoutParams = params
    }
    fun animateViewofInt(v: View, attr: String, value: Int, time: Long):Unit{
         ObjectAnimator.ofInt(
            v, attr,value
        ).apply{
            duration= time
             start()
        }
    }
    fun animateViewofFloat(v:View, attr: String, value: Float, time: Long):Unit{
        ObjectAnimator.ofFloat(
            v, attr, value
        ).apply {
            duration = time
            start()
        }
    }
    fun getSecondFromWatch(ca: String){}
}