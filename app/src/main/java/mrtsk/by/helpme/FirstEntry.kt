package mrtsk.by.helpme

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_first_entry.*
import mrtsk.by.helpme.callbacks.SimpleCallback
import mrtsk.by.helpme.models.User
import mrtsk.by.helpme.responses.SimpleResponse
import mrtsk.by.helpme.util.AppPreferences
import mrtsk.by.helpme.util.toBase64
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class FirstEntry : AppCompatActivity() {

    private val SERVER_ADDRESS = "http://192.168.43.20:3333"
    private val PERMISSION_REQUEST_CODE = 1
    private val SELECT_PHOTO = 123
    private var mUploadUserData: UploadUserData? = null
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_entry)

        preferences = AppPreferences(this)

        post_user_avatar.setOnClickListener {
            requestReadWriteStorage()
        }

        back_to_signIn_page.setOnClickListener {
            val intent = Intent(this, SignUpSignInActivity::class.java)
            startActivity(intent)
        }

        go_to_homepage.setOnClickListener {
            uploadUserDataToServer()
        }

    }

    inner class UploadUserData(private val simpleCallback: SimpleCallback) : AsyncTask<User, Unit, Unit>() {
        private lateinit var simpleResponse: SimpleResponse

        override fun doInBackground(vararg params: User?) {
            val url = SERVER_ADDRESS + "/signin/first"
            val user = params[0]
            preferences.setUserRating(100.0)

            val body = if (user!!.age != null) {
                FormBody.Builder()
                        .add("userId", preferences.getUserId())
                        .add("userName", user.name)
                        .add("userAge", user.age.toString())
                        .add("userAvatar", user.image)
                        .add("userRating", preferences.getUserRating().toString())
                        .build()
            } else {
                FormBody.Builder()
                        .add("userId", preferences.getUserId())
                        .add("userName", user.name)
                        .add("userAvatar", user.image)
                        .build()
            }

            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            simpleResponse = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<SimpleResponse> = moshi.adapter(SimpleResponse::class.java)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                SimpleResponse("network error")
            }
        }

        override fun onPostExecute(result: Unit?) {
            mUploadUserData = null
            showProgress(false)

            simpleCallback.finish(simpleResponse)
        }

        override fun onCancelled() {
            mUploadUserData = null
            showProgress(false)
        }
    }

    private fun uploadUserDataToServer() {
        if (mUploadUserData != null) {
            return
        }

        user_name.error = null

        var cancel = false
        var focusView: View? = null

        val nameStr = user_name.text.toString()
        val userAge = if (!user_age.text.isEmpty()) {
            user_age.text.toString().toInt()
        } else {
            null
        }
        val userAvatar = toBase64(post_user_avatar)

        if (TextUtils.isEmpty(nameStr)) {
            user_name.error = "Это поле обязательно"
            focusView = user_name
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)
            mUploadUserData = UploadUserData(object : SimpleCallback {
                override fun finish(simpleResponse: SimpleResponse) {
                    when (simpleResponse.text) {
                        "error from insert" -> {
                            Snackbar.make(
                                    first_entry_view,
                                    "Серверная ошибка. Попробуйте позже.",
                                    Snackbar.LENGTH_SHORT
                            ).show()
                            return
                        }

                        "successful insertion" -> {
                            val intent = Intent(this@FirstEntry, FeedActivity::class.java)

                            startActivity(intent)
                            return
                        }

                        "network error" -> {
                            Snackbar.make(
                                    first_entry_view,
                                    "Ошибка сети. Проверьте интернет-соединение.",
                                    Snackbar.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                }
            })

            val user = User(preferences.getUserId(), nameStr, userAge, null, userAvatar)
            mUploadUserData!!.execute(user)
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
                    Glide.with(this).load(selectedImage).into(post_user_avatar)
                    post_user_avatar.borderColor = android.R.attr.colorPrimary
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

    private fun showProgress(show: Boolean) {
        first_entry_progress.visibility = if (show) View.VISIBLE else View.GONE
        first_entry_form.alpha = if (show) 0.25f else 1f
    }
}
