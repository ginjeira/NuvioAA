package com.nuvio.car

import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import android.graphics.Bitmap

object CarImageUtils {

    fun fromBitmap(bitmap: Bitmap): CarIcon {
        val icon = IconCompat.createWithBitmap(bitmap)
        return CarIcon.Builder(icon).build()
    }
}
