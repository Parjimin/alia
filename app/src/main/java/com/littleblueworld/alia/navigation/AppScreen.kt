package com.littleblueworld.alia.navigation

import android.view.View
import com.littleblueworld.alia.state.AppState

interface AppScreen {
    val id: ScreenId
    val view: View

    fun render(state: AppState)

    /** Returns true when this screen consumed system Back. */
    fun onBackPressed(): Boolean = false

    fun onShown() = Unit

    fun onForegrounded() = Unit

    fun onBackgrounded() = Unit

    fun onHidden() = Unit
}
