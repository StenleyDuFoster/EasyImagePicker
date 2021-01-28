package stanley.du_foster.easyimagepicker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class EasyImagePicker {

    lateinit var activity: Activity

    fun pickImage(activity: Activity) {
        this.activity = activity
    }

    private fun choseDialog(title: String, messege: String, positiveText: String, negativeString: String, style: Int?) {
//        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(
//            activity, R.style.AlertDialogCustom
//        )
//        myAlertDialog.setTitle(getString(R.string.upload_picture_options))
//        myAlertDialog.setMessage(getString(R.string.upload_picture_text))
//
//        myAlertDialog.setPositiveButton(getString(R.string.upload_picture_positive),
//            { arg0, arg1 ->
//                getGallerySingleImageLauncher.launch("image/*")
//            })
//
//        myAlertDialog.setNegativeButton(getString(R.string.upload_picture_negative),
//            { arg0, arg1 ->
//                if (hasCameraPermission()) {
//                    takeFromPhoto()
//                } else {
//                    requestSinglePermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
//                }
//            })
//
//        myAlertDialog.show()
    }

    private fun takeFromPhoto() {
        val photoFile = File.createTempFile(
            "JPEG_myfile",
            ".jpg",
            requireContext().filesDir
        )
        photoUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", photoFile ?: return)

        getCameraImageLauncher.launch(photoUri)
    }

    private val getCameraImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let {
                getImageFileFromUri(it)?.let { file ->
                    val requestFile =
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                    var imageName = loadImageManeForm

                    val body = MultipartBody.Part.createFormData(
                        imageName,
                        file.getName(),
                        requestFile
                    )

                    imageData.add(body)

                    adapter.imageAddedToRequest(loadImageManeForm)
                    binding.recycler.apply {
                        val myAdapter = adapter
                        adapter = myAdapter
                    }
                }
            }
        }
    }

    private val requestSinglePermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultsMap ->
        if (resultsMap.all { it.value == true }) {
            takeFromPhoto()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private val getPdfLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                getPdfFileFromUri(it)?.let { file ->
                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                    val body = MultipartBody.Part.createFormData(
                        loadImageManeForm,
                        file.getName(),
                        requestFile
                    )

                    imageData.add(body)

                    adapter.imageAddedToRequest(loadImageManeForm)
                    binding.recycler.apply {
                        val myAdapter = adapter
                        adapter = myAdapter
                    }
                }
            }
        }

    private val getGallerySingleImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                getImageFileFromUri(it)?.let { file ->
                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                    val body = MultipartBody.Part.createFormData(
                        loadImageManeForm,
                        file.getName(),
                        requestFile
                    )

                    imageData.add(body)

                    adapter.imageAddedToRequest(loadImageManeForm)
                    binding.recycler.apply {
                        val myAdapter = adapter
                        adapter = myAdapter
                    }
                }
            }
        }

    private val getGalleryMultipleImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            uriList?.forEachIndexed { index, uri ->
                uri?.let {
                    getImageFileFromUri(it)?.let { file ->
                        val requestFile =
                            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                        var imageName = loadImageManeForm.substring(0, loadImageManeForm.length - 1)
                        imageName += index.toString()
                        imageName += "]"

                        val body = MultipartBody.Part.createFormData(
                            imageName,
                            file.getName(),
                            requestFile
                        )

                        imageData.add(body)

                        adapter.imageAddedToRequest(loadImageManeForm)
                        binding.recycler.apply {
                            val myAdapter = adapter
                            adapter = myAdapter
                        }
                    }

                }
            }
        }

    private fun getPdfFileFromUri(uri: Uri): File? {
        val inputStreamForBytes =
            requireActivity().contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStreamForBytes.readBytes()
        inputStreamForBytes.close()

        val bitmapOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val filesDir: File = requireContext().getFilesDir()
        val imageFile = File(filesDir, android.R.attr.name.toString() + ".jpg")

        val os = FileOutputStream(imageFile)
        bitmapOriginal?.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()

        return imageFile
    }

    private fun getImageFileFromUri(uri: Uri): File? {
        val inputStreamForBytes =
            requireActivity().contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStreamForBytes.readBytes()
        inputStreamForBytes.close()

        val inputStreamForExif =
            requireActivity().contentResolver.openInputStream(uri) ?: return null
        val exifInterface = ExifInterface(inputStreamForExif)
        val rotation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStreamForExif.close()

        val bitmapOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val bitmapRotated = bitmapOriginal.rotateImageIfRequired(rotation)

        val filesDir: File = requireContext().getFilesDir()
        val imageFile = File(filesDir, android.R.attr.name.toString() + ".jpg")

        val os = FileOutputStream(imageFile)
        bitmapRotated?.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()

        return imageFile
    }
}