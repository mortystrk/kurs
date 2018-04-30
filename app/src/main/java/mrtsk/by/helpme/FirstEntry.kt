package mrtsk.by.helpme

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_first_entry.*

class FirstEntry : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1
    private val SELECT_PHOTO = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_entry)

        profile_image.setOnClickListener {
            requestReadWriteStorage()
        }

    }

    private fun requestReadWriteStorage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(
                        first_entry_view,
                        "Необходимо получить доступ к галерее.",
                        Snackbar.LENGTH_INDEFINITE
                ).setAction(
                        "Понял",
                        {
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    PERMISSION_REQUEST_CODE)
                        }
                ).show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE)
            }
        } else {
            getPhotoFromStorage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getPhotoFromStorage()
                } else {
                    Snackbar.make(
                            first_entry_view,
                            "Для загрузки фото, необходимо разрешить доступ к хранилищу",
                            Snackbar.LENGTH_LONG
                    ).show()
                }
                return
            } else -> {
                return
            }
        }
    }

    private fun getPhotoFromStorage() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, SELECT_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECT_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val selectedImage = data!!.data
                    Glide.with(this).load(selectedImage).into(profile_image)
                    profile_image.borderColor = android.R.attr.colorPrimary
                } else {
                    Snackbar.make(
                            first_entry_view,
                            "Не удалось загрузить фото",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }
                return
            } else -> {
                    Snackbar.make(
                            first_entry_view,
                            "Не удалось получить доступ к галерее",
                            Snackbar.LENGTH_SHORT
                    ).show()
                return
            }
        }
    }
}
