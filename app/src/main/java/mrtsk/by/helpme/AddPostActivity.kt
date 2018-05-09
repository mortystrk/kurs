package mrtsk.by.helpme

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_add_post.*
import mrtsk.by.helpme.callbacks.PushPostCallback
import mrtsk.by.helpme.models.Meeting
import mrtsk.by.helpme.models.Post
import mrtsk.by.helpme.responses.SimpleResponse
import mrtsk.by.helpme.util.AppPreferences
import mrtsk.by.helpme.util.fromBase64
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class AddPostActivity : AppCompatActivity() {

    private var preferences: AppPreferences? = null
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userAvatar: String
    private var postRating: Float = 100f
    private var userRating: Float = 0.0f
    private lateinit var SERVER_ADDRESS: String
    private var mPushNewPost: PushPost? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        preferences = AppPreferences(this)
        userId = preferences!!.getUserId()
        userAvatar = preferences!!.getAvatar()
        userName = preferences!!.getName()
        userRating = preferences!!.getUserRating()
        SERVER_ADDRESS = preferences!!.getServerAddress()

        floatingAddPost.setOnClickListener {
            if (mPushNewPost != null) {
                return@setOnClickListener
            }

            val description = add_post_description.text.toString()
            val text = add_post_text.text.toString()

            showProgress(true)

            if (readParameters(description, text)) {
                val post = Post(null, userAvatar, userName, userId,
                        postRating, userRating, description, text)

                mPushNewPost = PushPost(object : PushPostCallback {
                    override fun finish(meeting: Meeting) {
                        when (meeting._id) {
                            "undefined" -> {
                                /*preferences!!.setNewPost(true)
                                preferences!!.setPostDescription(description)
                                preferences!!.setPostText(text)
                                preferences!!.setPostId(simpleResponse.postId!!.toInt())*/

                                Snackbar.make(
                                        add_post_layout,
                                        "Произошла ошибка на сервере, попробуйте позже.",
                                        Snackbar.LENGTH_SHORT
                                ).show()

                                /*Snackbar.make(
                                        add_post_layout,
                                        "Успешный постинг!",
                                        Snackbar.LENGTH_INDEFINITE
                                ).setAction(
                                        "Ok",
                                        {
                                            //onBackPressed()
                                        }
                                ).show()*/
                            }

                            "error to mongoose connect" -> {
                                Snackbar.make(
                                        add_post_layout,
                                        "Произошла ошибка на сервере, попробуйте позже.",
                                        Snackbar.LENGTH_SHORT
                                ).show()
                            }

                            "error to save" -> {
                                Snackbar.make(
                                        add_post_layout,
                                        "Ошибка сохранения в БД.",
                                        Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            else -> {

                                fromBase64(meeting.creator!!.avatar, imageView, this@AddPostActivity)
                                Toast.makeText(this@AddPostActivity, meeting.creator!!.name, Toast.LENGTH_SHORT).show()
                                Snackbar.make(
                                        add_post_layout,
                                        "Успешный постинг!",
                                        Snackbar.LENGTH_INDEFINITE
                                ).setAction(
                                        "Ok",
                                        {
                                            //onBackPressed()
                                        }
                                ).show()
                            }
                        }
                    }
                })
                mPushNewPost!!.execute(description, text, preferences!!.getUserId())
            }
        }
    }

    inner class PushPost(private val pushPostCallback: PushPostCallback) : AsyncTask<String, Unit, Unit>() {
        private var mMeeting: Meeting? = null

        override fun doInBackground(vararg params: String) {
            val url = SERVER_ADDRESS + "/create/meeting"
            val name = params[0]
            val description = params[1]
            val id = params[2]

            val body = FormBody.Builder()
                    .add("name", name)
                    .add("description", description)
                    .add("creatorId", id)
                    .build()

            /*val body = FormBody.Builder()
                    .add("userId", post.userId)
                    .add("postRating", "100")
                    .add("userAvatar", post.userAvatar)
                    .add("userName", post.userName)
                    .add("postDescription", post.postDescription)
                    .add("postText", post.postText)
                    .add("userRating", preferences!!.getUserRating().toString())
                    .build()*/

            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            val client = OkHttpClient()
            val moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<Meeting> = moshi.adapter(Meeting::class.java)

            val response = client.newCall(request).execute()
            jsonAdapter.fromJson(response.body().string())!!

            mMeeting = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<Meeting> = moshi.adapter(Meeting::class.java)

                val response = client.newCall(request).execute()
                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                Meeting("undefined", "", "", null)
            }
        }

        override fun onPostExecute(result: Unit?) {
            mPushNewPost = null
            showProgress(false)

            pushPostCallback.finish(mMeeting!!)
        }

    }

    private fun readParameters(description: String, text: String) : Boolean {

        add_post_description.error = null

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(description)) {
            add_post_description.error = "Это поле обязательно"
            focusView = add_post_description
            cancel = true
        }

        if (TextUtils.isEmpty(text)) {
            add_post_text.error = "Шутите? А где текст?"
            focusView = add_post_text
            cancel = true
        }

        if (!validationPostText(text)) {
            add_post_text.error = "Маловато будет"
            focusView = add_post_text
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
            showProgress(false)

            return false
        }

        return true
    }

    private fun validationPostText(text: String): Boolean {
        return text.length > 5
    }

    private fun showProgress(show: Boolean) {
        add_post_progress.visibility = if (show) View.VISIBLE else View.GONE
        add_post_form.alpha = if (show) 0.25f else 1f
    }
}
