package stanley.du_foster.easyimagepicker

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

fun Bitmap.rotateImageIfRequired(rotation: Int): Bitmap? {
    return when (rotation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(270)
        else -> this
    }
}

private fun Bitmap.rotateImage(degree: Int): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(degree * 1.0f)
    val rotatedImg = Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    this.recycle()
    return rotatedImg
}