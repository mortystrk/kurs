package mrtsk.by.helpme.fragments

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.user_main_page.*
import mrtsk.by.helpme.R
import mrtsk.by.helpme.callbacks.UserDataCallback
import mrtsk.by.helpme.models.User
import mrtsk.by.helpme.util.fromBase64
import okhttp3.OkHttpClient
import okhttp3.Request

class MainPageFragment: Fragment() {

    private val SERVER_ADDRESS = "http://192.168.43.20:3333"
    private var mDownloadUserData: DownloadUserData? = null
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            id = arguments.getString(USER_ID)
        }

        mDownloadUserData = DownloadUserData(object : UserDataCallback {
            @SuppressLint("SetTextI18n")
            override fun finish(user: User?) {
                if (user != null) {
                    fromBase64(user.image, user_main_page_avatar, context)
                    user_main_page_name.text = user.name
                    if (user.age != null) {
                        user_main_page_age.text = "Возраст: ${user.age.toString()}"
                        user_main_page_rating.text = "Рейтинг: ${user.rating.toString()}"
                    } else {
                        user_main_page_age.text = "Рейтинг: ${user.rating.toString()}"
                    }
                } else {
                    Snackbar.make(
                            user_main_page,
                            "Не удалось загрузить страницу.",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        })
        mDownloadUserData!!.execute()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_main_page, container, false)
    }

    inner class DownloadUserData(private var userDataCallback: UserDataCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var user: User? = null

        override fun doInBackground(vararg params: Unit?) {
            val url = SERVER_ADDRESS + "/get/mainpage?id=$id"

            val request = Request.Builder()
                    .url(url)
                    .build()

            user = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: Unit?) {
            mDownloadUserData = null

            userDataCallback.finish(user)
        }

    }

    companion object {
        private val USER_ID = "id"

        fun newInstance(param: String) : MainPageFragment {
            val fragment = MainPageFragment()
            val args = Bundle()
            args.putString(USER_ID, param)
            fragment.arguments = args
            return fragment
        }
    }
}
