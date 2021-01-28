package stanley.du_foster.easyimagepicker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream

class EasyImagePicker(private val activity: AppCompatActivity) {

    companion object {
        const val IMAGE_FORMAT = "image/*"
        const val PDF_FORMAT = "application/pdf"
    }

    private var photoUri: Uri? = null
     var resualtUri = MutableLiveData<Uri>()
    private var resualtFile = MutableLiveData<File>()

    fun pickImageUri(): MutableLiveData<Uri> {
        choseDialog(galleryLauncher = {
            getGallerySingleImageLauncher.launch(IMAGE_FORMAT)
        })
        return resualtUri
    }

    fun pickImagesUri() {
        choseDialog(galleryLauncher = {
            getGalleryMultipleImagesLauncher.launch(IMAGE_FORMAT)
        })
    }

    fun pickPdfUri() {
        getPdfLauncher.launch(PDF_FORMAT)
    }

    fun pickPdfsUris() {
        getPdfLauncher.launch(PDF_FORMAT)
    }

    fun pickImageFile() {
        choseDialog(galleryLauncher = {
            getGallerySingleImageLauncher.launch(IMAGE_FORMAT)
        })
    }

    fun pickImagesFiles() {
        choseDialog(galleryLauncher = {
            getGalleryMultipleImagesLauncher.launch(IMAGE_FORMAT)
        })
    }

    fun pickPdfFile() {
        getPdfLauncher.launch(PDF_FORMAT)
    }

    fun pickPdfFiles() {
        getPdfLauncher.launch(PDF_FORMAT)
    }

    fun onDestroy() {
        requestSinglePermissionLauncher.unregister()
        getCameraImageLauncher.unregister()
        getGallerySingleImageLauncher.unregister()
        getGalleryMultipleImagesLauncher.unregister()
    }

    private fun choseDialog(title: String = "1", messege: String = "2", positiveText: String = "3", negativeString: String = "4", style: Int? = null, galleryLauncher: () -> Unit) {
        val myAlertDialog: AlertDialog.Builder = if (style == null) {
            AlertDialog.Builder(activity)
        } else {
            AlertDialog.Builder(activity, style)
        }


        myAlertDialog.setTitle(title)
        myAlertDialog.setMessage(messege)

        myAlertDialog.setPositiveButton(positiveText
        ) { arg0, arg1 ->
            galleryLauncher()
        }

        myAlertDialog.setNegativeButton(negativeString
        ) { arg0, arg1 ->
            if (hasCameraPermission()) {
                takeFromPhoto()
            } else {
                requestSinglePermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }

        myAlertDialog.show()
    }

    private fun takeFromPhoto() {
        val photoFile = File.createTempFile(
            "JPEG_myfile",
            ".jpg",
            activity.filesDir
        )
        photoUri = FileProvider.getUriForFile(activity.applicationContext, activity.packageName + ".fileprovider", photoFile ?: return)

        getCameraImageLauncher.launch(photoUri)
    }

    private val getCameraImageLauncher = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let { uri ->
                resualtUri.postValue(uri)
            }
        }
    }

    private val requestSinglePermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultsMap ->
        if (resultsMap.all { it.value == true }) {
            takeFromPhoto()
        } else {
            Toast.makeText(activity.applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private val getPdfLauncher =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                getPdfFileFromUri(it)?.let { file ->

                }
            }
        }

    private val getGallerySingleImageLauncher =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uriNullable: Uri? ->
            uriNullable?.let { uri ->
                resualtUri.postValue(uri)
                getImageFileFromUri(uri)?.let { file ->

                }
            }
        }

    private val getGalleryMultipleImagesLauncher =
        activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            uriList?.forEachIndexed { index, uri ->
                uri?.let {
                    getImageFileFromUri(it)?.let { file ->


                    }
                }
            }
        }

    private fun getPdfFileFromUri(uri: Uri): File? {
        val inputStreamForBytes = activity.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStreamForBytes.readBytes()
        inputStreamForBytes.close()

        val bitmapOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val filesDir: File = activity.applicationContext.filesDir
        val imageFile = File(filesDir, android.R.attr.name.toString() + ".jpg")

        val os = FileOutputStream(imageFile)
        bitmapOriginal?.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()

        return imageFile
    }

    private fun getImageFileFromUri(uri: Uri): File? {
        val inputStreamForBytes = activity.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStreamForBytes.readBytes()
        inputStreamForBytes.close()

        val inputStreamForExif =
            activity.contentResolver.openInputStream(uri) ?: return null
        val exifInterface = ExifInterface(inputStreamForExif)
        val rotation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStreamForExif.close()

        val bitmapOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val bitmapRotated = bitmapOriginal.rotateImageIfRequired(rotation)

        val filesDir: File = activity.applicationContext.filesDir
        val imageFile = File(filesDir, android.R.attr.name.toString() + ".jpg")

        val os = FileOutputStream(imageFile)
        bitmapRotated?.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()

        return imageFile
    }
}