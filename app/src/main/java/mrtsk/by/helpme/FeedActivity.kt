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
import mrtsk.by.helpme.models.Meeting
import mrtsk.by.helpme.models.Post
import mrtsk.by.helpme.models.PostsList
import mrtsk.by.helpme.models.User
import mrtsk.by.helpme.util.AppPreferences
import mrtsk.by.helpme.util.fromBase64
import okhttp3.OkHttpClient
import okhttp3.Request

class FeedActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    //private var mDownloadUserData: DownloadUserData? = null
    private var mDownloadAllMeetings: DownloadAllMeetings? = null
    private lateinit var meetings: ArrayList<Meeting>
    private lateinit var adapter: MeetingListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        preferences = AppPreferences(this)

        floating_pencil.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }

        floating_home.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        mDownloadAllMeetings = DownloadAllMeetings(object : DownloadPostsCallback {
            override fun finish(meetings: List<Meeting>?) {
                if (meetings != null) {
                    this@FeedActivity.meetings = ArrayList(meetings)
                    adapter = MeetingListViewAdapter(this@FeedActivity.meetings)
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

        mDownloadAllMeetings!!.execute()

        /*mDownloadUserData = DownloadUserData(object : UserDataCallback {
            @SuppressLint("SetTextI18n")
            override fun finish(user: User?) {
                if (user != null) {
                    preferences.setAvatar(user.avatar)
                    preferences.setUserName(user.name)
                    if (user.age != null) {
                        preferences.setAge(user.age!!)
                        preferences.setUserRating(user.userRating!!)
                    } else {
                        preferences.setUserRating(user.userRating!!)
                    }
                } else {
                    Snackbar.make(
                            feed_layout,
                            "Не удалось загрузить страницу.",
                            Snackbar.LENGTH_SHORT
                    ).show()
                }

                mDownloadAllMeetings = DownloadAllPosts(object : DownloadPostsCallback {
                    override fun finish(posts: List<Post>?) {
                        if (posts != null) {
                            this@FeedActivity.posts = PostsList(posts as ArrayList<Post>)
                            adapter = PostListViewAdapter(this@FeedActivity.posts)
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
                mDownloadAllMeetings!!.execute()
            }
        })
        mDownloadUserData!!.execute()*/
    }

    /*override fun onResume() {
        super.onResume()
        if (preferences.isNewPost()) {
            val newPost = Post(preferences.getPostId(), preferences.getAvatar(), preferences.getName(),
                    preferences.getUserId(), preferences.getPostRating(), preferences.getUserRating(),
                    preferences.getPostDescription(), preferences.getPostText())

            posts.addPost(newPost)
            adapter.notifyDataSetChanged()
            preferences.setNewPost(false)
        }
    }*/

    inner class MeetingListViewAdapter(private var meetingsList: ArrayList<Meeting>) : BaseAdapter() {
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

            fromBase64(meetingsList[position].creator!!.avatar, viewHolder.avatar, baseContext)
            viewHolder.userName.text = meetingsList[position].creator!!.name
            viewHolder.userAge.text = "Возраст: ${meetingsList[position].creator!!.age}"
            viewHolder.meetingDescription.text = meetingsList[position].name

            viewHolder.meetingDescription.setOnClickListener {
                val intent = Intent(this@FeedActivity, MeetingDetail::class.java)
                intent.putExtra("_id", meetingsList[position]._id)

                startActivity(intent)
            }
            return view!!
        }

        override fun getItem(position: Int): Any {
            return meetingsList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return meetingsList.size
        }
    }

    private class ViewHolder(view: View?) {
        val avatar: CircleImageView = view?.findViewById<CircleImageView>(R.id.post_user_avatar) as CircleImageView
        val userName: TextView = view?.findViewById<TextView>(R.id.feed_user_name) as TextView
        val userAge: TextView = view?.findViewById<TextView>(R.id.feed_user_age) as TextView
        val meetingDescription: TextView = view?.findViewById<TextView>(R.id.meeting_description) as TextView
    }

    /*inner class DownloadUserData(private var userDataCallback: UserDataCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var user: User? = null

        override fun doInBackground(vararg params: Unit?) {
            val url = preferences.getServerAddress() + "/get/mainpage?_id=${preferences.getUserId()}"

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

    }*/

    inner class DownloadAllMeetings(private var downloadPostsCallback: DownloadPostsCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var meetings: List<Meeting>? = null

        override fun doInBackground(vararg params: Unit?) {
            val url = "${preferences.getServerAddress()}/select/meetings"

            val request = Request.Builder()
                    .url(url)
                    .build()

            meetings = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val type = Types.newParameterizedType(List::class.java, Meeting::class.java)
                val jsonAdapter: JsonAdapter<List<Meeting>> = moshi.adapter(type)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: Unit?) {
            downloadPostsCallback.finish(meetings)
        }
    }

    private fun showProgress(show: Boolean) {
        feed_download_progress.visibility = if (show) View.VISIBLE else View.GONE
        feed_list_view.alpha = if (show) 0.25f else 1f
    }
}
