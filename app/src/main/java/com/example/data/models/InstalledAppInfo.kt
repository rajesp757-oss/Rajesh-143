package com.example.data.models

import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val versionName: String = "",
    val icon: Drawable? = null
)
