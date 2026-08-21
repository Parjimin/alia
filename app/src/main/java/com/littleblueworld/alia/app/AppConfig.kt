package com.littleblueworld.alia.app

import com.littleblueworld.alia.BuildConfig

data class AppConfig(
    val supabaseUrl: String,
    val supabasePublishableKey: String,
) {
    companion object {
        fun fromBuildConfig() = AppConfig(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabasePublishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        )
    }
}
