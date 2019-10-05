package com.nopoint.midpoint.views

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import com.nopoint.midpoint.R
import kotlinx.android.synthetic.main.view_search.view.*
import android.hardware.input.InputManager


class SearchView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val duration: Long = 200

    init {
        LayoutInflater.from(context).inflate(R.layout.view_search, this, true)

        search_button_open.setOnClickListener { openSearch() }
        close_search_button.setOnClickListener { closeSearch() }
    }

    private fun openSearch() {
        search_input.setText("")
        search_open_view.visibility = View.VISIBLE
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            search_open_view,
            (search_button_open.right + search_button_open.left) / 2,
            (search_button_open.top + search_button_open.bottom) / 2,
            0f, width.toFloat()
        )
        circularReveal.duration = duration
        circularReveal.start()
        search_input.requestFocus()
        //todo open keyboard
        // val inputManager = context.getSystemService(Context) as InputManager
    }

    private fun closeSearch() {
        val circularConceal = ViewAnimationUtils.createCircularReveal(
            search_open_view,
            (search_button_open.right + search_button_open.left) / 2,
            (search_button_open.top + search_button_open.bottom) / 2,
            width.toFloat(), 0f
        )

        circularConceal.duration = duration
        circularConceal.start()
        circularConceal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) = Unit
            override fun onAnimationCancel(animation: Animator?) = Unit
            override fun onAnimationStart(animation: Animator?) = Unit
            override fun onAnimationEnd(animation: Animator?) {
                search_open_view.visibility = View.INVISIBLE
                search_input.setText("")
                circularConceal.removeAllListeners()
            }
        })
    }

}