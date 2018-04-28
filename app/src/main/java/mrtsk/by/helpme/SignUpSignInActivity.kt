package mrtsk.by.helpme

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.github.mrengineer13.snackbar.SnackBar
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_sign_up_sign_in.*
import mrtsk.by.helpme.algorithm.SHA256
import mrtsk.by.helpme.callbacks.SignInCallback
import mrtsk.by.helpme.callbacks.SignUpCallback
import mrtsk.by.helpme.responses.SignInResponse
import mrtsk.by.helpme.responses.SignUpResponse
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class SignUpSignInActivity : AppCompatActivity() {

    private val SERVER_ADDRESS = "http://192.168.43.20:3333"
    private var mSignUpRequest: SignUpRequest? = null
    private var mSignInRequest: SignInRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_sign_in)

        sign_up.setOnClickListener { attemptSignUp() }
        sign_in.setOnClickListener { attemptSignIn() }
    }

    inner class SignInRequest(private val signInCallback: SignInCallback) : AsyncTask<String, Unit, Unit>() {
        private lateinit var signInResponse: SignInResponse
        private var exception = false

        override fun doInBackground(vararg params: String?) {
            val url = SERVER_ADDRESS + "/signin"
            val login = params[0]
            val password = params[1]

            val body = buildSignInBody(login!!, password!!)

            val request = buildSingInRequest(url, body)

            try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<SignInResponse> = moshi.adapter(SignInResponse::class.java)

                val response = client.newCall(request).execute()

                signInResponse = jsonAdapter.fromJson(response.body().string())!!

            } catch (e: Exception) {
                exception = true
            }
        }

        override fun onPostExecute(result: Unit?) {
            showProgress(false)
            mSignInRequest = null

            if (exception) signInCallback.finish(null)
            else signInCallback.finish(signInResponse)
        }

        override fun onCancelled() {
            mSignInRequest = null
            showProgress(false)
        }
    }

    inner class SignUpRequest(private val signUpCallback: SignUpCallback) : AsyncTask<String, Unit, Boolean>() {
        private lateinit var signUpResponse: SignUpResponse

        override fun doInBackground(vararg params: String?): Boolean {
            val url = SERVER_ADDRESS + "/registration"
            val login = params[0]
            val password = params[1]

            val body = buildSignUpBody(login!!, password!!)
            val request = buildSignUpRequest(url, body)

            return try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<SignUpResponse> = moshi.adapter(SignUpResponse::class.java)

                val response = client.newCall(request).execute()

                signUpResponse = jsonAdapter.fromJson(response.body().string())!!

                if (signUpResponse!!.response == "login is already taken") {
                    signUpResponse!!.isBusy = true
                    false
                } else {
                    signUpResponse!!.isBusy = false
                    true
                }
            } catch (e: Exception) {
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            mSignUpRequest = null
            showProgress(false)

            if (result) {
                signUpCallback.finish(signUpResponse!!) // ok
            } else if (!result && signUpResponse!!.isBusy == null) {
                signUpCallback.finish(null) // ошибка сети и т.п.
            } else {
                signUpCallback.finish(signUpResponse!!) // логин занят
            }
        }

        override fun onCancelled() {
            mSignUpRequest = null
            showProgress(false)
        }

    }

    private fun buildSignInBody(login: String, password: String) : FormBody {
        val id = SHA256(login)

        return FormBody.Builder()
                .add("userId", id)
                .add("login", login)
                .add("password", password)
                .build()
    }

    private fun buildSingInRequest(url: String, body: FormBody) : Request {
        return Request.Builder()
                .url(url)
                .post(body)
                .build()
    }

    private fun buildSignUpBody(login: String, password: String) : FormBody {
        val id = SHA256(login)

        return FormBody.Builder()
                .add("userId", id)
                .add("login", login)
                .add("password", password)
                .build()
    }

    private fun buildSignUpRequest(url: String, body: FormBody) : Request {
        return Request.Builder()
                .url(url)
                .post(body)
                .build()
    }

    private fun attemptSignUp() {
        if (mSignUpRequest != null) {
            return
        }

        et_login.error = null
        et_password.error = null

        val loginStr = et_login.text.toString()
        val passwordStr = et_password.text.toString()

        var cancel = false
        var focusView: View? = null

        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            et_password.error = "Пароль слишком короткий"
            focusView = et_password
            cancel = true
        }

        if (TextUtils.isEmpty(loginStr)) {
            et_login.error = "Это поле обязательно"
            focusView = et_login
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)
            mSignUpRequest = SignUpRequest(object : SignUpCallback {
                override fun finish(signUpResponse: SignUpResponse?) {
                    if (signUpResponse != null) {
                        if (!signUpResponse.isBusy!!) {
                            val snackbar = SnackBar.Builder(this@SignUpSignInActivity)
                                    .withMessage("Регистрация прошла успешно")
                                    .show()
                        } else {
                            val snackbar = SnackBar.Builder(this@SignUpSignInActivity)
                                    .withMessage("Такой логин уже занят")
                                    .show()
                            et_login.text.clear()
                            et_password.text.clear()
                        }
                    } else {
                        val snackbar = SnackBar.Builder(this@SignUpSignInActivity)
                                .withMessage("Что-то пошло не так. Повторите попытку позже")
                                .show()
                    }
                }

            })
            mSignUpRequest!!.execute(loginStr, passwordStr)
        }
    }

    private fun attemptSignIn() {
        if (mSignInRequest != null) {
            return
        }

        et_login.error = null
        et_password.error = null

        val loginStr = et_login.text.toString()
        val passwordStr = et_password.text.toString()

        var cancel = false
        var focusView: View? = null

        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            et_password.error = "Пароль слишком короткий"
            focusView = et_password
            cancel = true
        } else if (TextUtils.isEmpty(passwordStr)) {
            et_password.error = "Введите пароль"
            focusView = et_password
            cancel = true
        }

        if (TextUtils.isEmpty(loginStr)) {
            et_login.error = "Это поле обязательно"
            focusView = et_login
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)
            mSignInRequest = SignInRequest(object : SignInCallback {
                override fun finish(signInResponse: SignInResponse?) {
                    if (signInResponse != null) {
                        if (!signInResponse.isExist) {
                            val snackbar = SnackBar.Builder(this@SignUpSignInActivity)
                                    .withMessage("Такого пользователя не существует")
                                    .show()
                        } else if (!signInResponse.isCorrectPassword) {
                            val snackbar = SnackBar.Builder(this@SignUpSignInActivity)
                                    .withMessage("Неверный пароль")
                                    .show()
                        } else if (signInResponse.firstEntry == "true") {
                            val intent = Intent(this@SignUpSignInActivity, FirstEntry::class.java)
                            intent.putExtra("id", signInResponse.id)

                            startActivity(intent)
                        } else {
                            val intent = Intent(this@SignUpSignInActivity, MainActivity::class.java)
                            intent.putExtra("id", signInResponse.id)

                            startActivity(intent)
                        }
                    } else {
                        val snackbar = SnackBar.Builder(this@SignUpSignInActivity)
                                .withMessage("Ошибка сети")
                                .show()
                    }
                }
            })
            mSignInRequest!!.execute(loginStr, passwordStr)
        }
    }

    private fun isPasswordValid(passwordStr: String) : Boolean {
        return passwordStr.length > 6
    }

    private fun showProgress(show: Boolean) {
            sign_up_in_progress.visibility = if (show) View.VISIBLE else View.GONE
            sign_up_sign_in_form.alpha = if (show) 0.25f else 1f
    }
}

