package mrtsk.by.helpme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_feed.*
import mrtsk.by.helpme.callbacks.DownloadPostsCallback
import mrtsk.by.helpme.callbacks.UserDataCallback
import mrtsk.by.helpme.models.Post
import mrtsk.by.helpme.models.PostsList
import mrtsk.by.helpme.models.User
import mrtsk.by.helpme.util.AppPreferences
import mrtsk.by.helpme.util.fromBase64
import okhttp3.OkHttpClient
import okhttp3.Request

class FeedActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private var mDownloadUserData: DownloadUserData? = null
    private var mDownloadAllPosts: DownloadAllPosts? = null
    private lateinit var posts: PostsList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        preferences = AppPreferences(this)

        floating_pencil.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }

        mDownloadUserData = DownloadUserData(object : UserDataCallback {
            @SuppressLint("SetTextI18n")
            override fun finish(user: User?) {
                if (user != null) {
                    preferences.setAvatar(user.image)
                    preferences.setUserName(user.name)
                    if (user.age != null) {
                        preferences.setAge(user.age!!)
                        preferences.setUserRating(user.rating!!)
                    } else {
                        preferences.setUserRating(user.rating!!)
                    }
                } else {
                    Snackbar.make(
                            feed_layout,
                            "Не удалось загрузить страницу.",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }

                mDownloadAllPosts = DownloadAllPosts(object : DownloadPostsCallback {
                    override fun finish(posts: List<Post>?) {
                        if (posts != null) {
                            this@FeedActivity.posts = PostsList(posts as ArrayList<Post>)
                            val adapter = PostListViewAdapter(this@FeedActivity.posts)
                            feed_list_view.adapter = adapter
                        } else {
                            Snackbar.make(
                                    feed_layout,
                                    "Не удалось загрузить ленту.",
                                    Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
                mDownloadAllPosts!!.execute()
            }
        })
        mDownloadUserData!!.execute()
    }

    inner class PostListViewAdapter(private var postsList: PostsList) : BaseAdapter() {
        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View?
            val viewHolder: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.feed_item, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            fromBase64((postsList.getPost(position)).userAvatar, viewHolder.avatar, baseContext)
            viewHolder.userName.text = postsList.getPost(position).userName
            viewHolder.userRating.text = "Рейтинг: ${postsList.getPost(position).postRating}"
            viewHolder.postDescription.text = postsList.getPost(position).postDescription
            viewHolder.postText.text = postsList.getPost(position).postText
            viewHolder.postRating.text = "Рейтинг записи: ${postsList.getPost(position).postRating}"

            viewHolder.like.setOnClickListener {
                Toast.makeText(this@FeedActivity, "Like!", Toast.LENGTH_SHORT).show()
            }

            viewHolder.dislike.setOnClickListener {
                Toast.makeText(this@FeedActivity, "Dislike!", Toast.LENGTH_SHORT).show()
            }

            return view!!
        }

        override fun getItem(position: Int): Any {
            return postsList.getPost(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return postsList.size()
        }
    }

    private class ViewHolder(view: View?) {
        val avatar: CircleImageView = view?.findViewById<CircleImageView>(R.id.post_user_avatar) as CircleImageView
        val userName: TextView = view?.findViewById<TextView>(R.id.post_user_name) as TextView
        val userRating: TextView = view?.findViewById<TextView>(R.id.post_user_rating) as TextView
        val postRating: TextView = view?.findViewById<TextView>(R.id.post_rating) as TextView
        val postDescription: TextView = view?.findViewById<TextView>(R.id.post_description) as TextView
        val postText: TextView = view?.findViewById<TextView>(R.id.post_text) as TextView
        val like: CircleImageView = view?.findViewById<CircleImageView>(R.id.like) as CircleImageView
        val dislike: CircleImageView = view?.findViewById<CircleImageView>(R.id.dislike) as CircleImageView
    }

    inner class DownloadUserData(private var userDataCallback: UserDataCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var user: User? = null

        override fun doInBackground(vararg params: Unit?) {
            val url = preferences.getServerAddress() + "/get/mainpage?id=${preferences.getUserId()}"

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

    inner class DownloadAllPosts(private var downloadPostsCallback: DownloadPostsCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var posts: List<Post>? = null

        override fun doInBackground(vararg params: Unit?) {
            val url = "${preferences.getServerAddress()}/get/all/posts"

            val request = Request.Builder()
                    .url(url)
                    .build()

            /*val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(ArrayList::class.java, Post::class.java)
            val jsonAdapter: JsonAdapter<ArrayList<Post>> = moshi.adapter(type)*/

            val client = OkHttpClient()
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type = Types.newParameterizedType(List::class.java, Post::class.java)
            val jsonAdapter: JsonAdapter<List<Post>> = moshi.adapter(type)

            val response = client.newCall(request).execute()

            jsonAdapter.fromJson(response.body().string())!!

            posts = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(List::class.java, Post::class.java)
                val jsonAdapter: JsonAdapter<List<Post>> = moshi.adapter(type)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {

                null
            }
        }

        override fun onPostExecute(result: Unit?) {
            downloadPostsCallback.finish(posts)
        }
    }
}
