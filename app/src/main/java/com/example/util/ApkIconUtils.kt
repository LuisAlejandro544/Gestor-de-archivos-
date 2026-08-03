package com.example.util

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ApkIconUtils {

    suspend fun getApkIcon(context: Context, apkPath: String): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val info: PackageInfo? = pm.getPackageArchiveInfo(apkPath, 0)
            val appInfo = info?.applicationInfo
            if (appInfo != null) {
                appInfo.sourceDir = apkPath
                appInfo.publicSourceDir = apkPath
                val iconDrawable: Drawable? = appInfo.loadIcon(pm)
                if (iconDrawable != null) {
                    val bitmap = drawableToBitmap(iconDrawable)
                    return@withContext bitmap.asImageBitmap()
                }
            }
        } catch (e: Exception) {
            // Ignore error if APK is mock or unparseable
        }
        return@withContext null
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

@Composable
fun rememberApkIcon(context: Context, apkPath: String): ImageBitmap? {
    return produceState<ImageBitmap?>(initialValue = null, key1 = apkPath) {
        value = ApkIconUtils.getApkIcon(context, apkPath)
    }.value
}
