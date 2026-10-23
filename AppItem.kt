package com.pleit.clearhome

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val label: String,
    val image: Drawable,
    val isBanner: Boolean,
    val launchIntent: Intent
)
