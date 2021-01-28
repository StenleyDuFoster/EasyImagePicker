package stanley.du_foster.easyimagepicker

import android.graphics.Bitmap
import android.graphics.Matrix

class Bitmap(val bitmap: Bitmap) {

    private fun rotate(degree: Int): Bitmap? {
        val matrix = Matrix()
        bitmap.apply {
            matrix.postRotate(degree * 1.0f)
            val rotatedImg = Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
            this.recycle()
            return rotatedImg
        }
    }
}