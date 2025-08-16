package com.example.myfitplan.utilities

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.FileNotFoundException

interface Camera {
    val imageURI: Uri
    fun takePicture()
}

@Composable
fun rememberCamera(onPhotoTaken: (uri: Uri) -> Unit): Camera {
    val context = LocalContext.current
    val imageUri = remember {
        val imageFile = File.createTempFile("profile_image", ".jpg", context.externalCacheDir)
        Log.v("Camera", "Image file: ${imageFile.absolutePath}")
        FileProvider.getUriForFile(context, context.packageName + ".provider", imageFile)
    }
    var capturedImageUri by remember { mutableStateOf(Uri.EMPTY) }

    val cameraActivityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
            if (pictureTaken) {
                capturedImageUri = imageUri
                val saved = saveImageToStorage(capturedImageUri, context.applicationContext.contentResolver)
                onPhotoTaken(saved)
            }
        }

    val cameraLauncher by remember {
        derivedStateOf {
            object : Camera {
                override val imageURI: Uri
                    get() = imageUri

                override fun takePicture() {
                    cameraActivityLauncher.launch(imageUri)
                }
            }
        }
    }

    return cameraLauncher
}

fun uriToBitmap(imageUri: Uri, contentResolver: ContentResolver): Bitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    } else {
        val source = ImageDecoder.createSource(contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source)
    }
}

fun saveImageToStorage(
    imageUri: Uri,
    contentResolver: ContentResolver,
    name: String = "IMG_${SystemClock.uptimeMillis()}"
): Uri {
    val bitmap = uriToBitmap(imageUri, contentResolver)

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyFitPlan")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val savedImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: throw FileNotFoundException()

    contentResolver.openOutputStream(savedImageUri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val finValues = ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        contentResolver.update(savedImageUri, finValues, null, null)
    }

    return savedImageUri
}