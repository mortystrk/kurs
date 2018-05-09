package mrtsk.by.helpme

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_meeting_detail.*
import mrtsk.by.helpme.callbacks.MeetingDetailCallback
import mrtsk.by.helpme.callbacks.SimpleCallback
import mrtsk.by.helpme.responses.MeetingDetailResponse
import mrtsk.by.helpme.responses.SimpleResponse
import mrtsk.by.helpme.util.AppPreferences
import mrtsk.by.helpme.util.fromBase64
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class MeetingDetail : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private var mDownloadMeetingDetail: DownloadMeetingDetail? = null
    private var mSubscribe: Subscribe? = null
    private var mMeetingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)

        meeting_detail_ok.setOnClickListener {
            mSubscribe = Subscribe(object : SimpleCallback {
                override fun finish(simpleResponse: SimpleResponse) {
                    when (simpleResponse.error) {
                        "JSON parse error" -> {
                            Snackbar.make(
                                    meeting_detail_layout,
                                    "Ошибка парсинга ответа",
                                    Snackbar.LENGTH_LONG
                            ).show()
                        }

                        "connection error" -> {
                            Snackbar.make(
                                    meeting_detail_layout,
                                    "Серверная ошибка.",
                                    Snackbar.LENGTH_LONG
                            ).show()
                        }

                        "find error" -> {
                            Snackbar.make(
                                    meeting_detail_layout,
                                    "Такой встречи не найдено. Error 404.",
                                    Snackbar.LENGTH_LONG
                            ).show()
                        }

                        "save error" -> {
                            Snackbar.make(
                                    meeting_detail_layout,
                                    "Не удалось сохранить.",
                                    Snackbar.LENGTH_LONG
                            ).show()
                        }

                        "ok save sub" -> {
                            Snackbar.make(
                                    meeting_detail_layout,
                                    "Удачная подписка.",
                                    Snackbar.LENGTH_LONG
                            ).setAction(
                                    "Ok",
                                    {
                                        onBackPressed()
                                    }
                            ).show()
                        }
                    }
                }
            })
            mSubscribe!!.execute()
        }

        preferences = AppPreferences(this)
        mDownloadMeetingDetail = DownloadMeetingDetail(object : MeetingDetailCallback {
            @SuppressLint("SetTextI18n")
            override fun finish(meetingDetailResponse: MeetingDetailResponse) {
                val error = meetingDetailResponse.error
                when (error) {
                    "not found" -> {
                        Snackbar.make(
                                meeting_detail_layout,
                                "Такой встречи не найдено. Error 404.",
                                Snackbar.LENGTH_LONG
                        ).show()
                        meeting_detail_ok.isEnabled = false
                        return
                    }
                    "server error" -> {
                        Snackbar.make(
                                meeting_detail_layout,
                                "Серверная ошибка. Error 500.",
                                Snackbar.LENGTH_LONG
                        ).show()
                        meeting_detail_ok.isEnabled = false
                        return
                    }
                    "json error" -> {
                        Snackbar.make(
                                meeting_detail_layout,
                                "JSON cast error.",
                                Snackbar.LENGTH_LONG
                        ).show()
                        meeting_detail_ok.isEnabled = false
                        return
                    }
                    "no error" -> {
                        fromBase64(meetingDetailResponse.meeting!!.creator!!.avatar,
                                meeting_detail_avatar, this@MeetingDetail)
                        meeting_detail_name.text = meetingDetailResponse.meeting.name
                        meeting_detail_descr.text = meetingDetailResponse.meeting.description
                        meeting_user_name.text = meetingDetailResponse.meeting.creator!!.name
                        meeting_user_age.text = "Возраст: ${meetingDetailResponse.meeting.creator!!.age}"
                        meeting_user_phone.text = "тел.: ${meetingDetailResponse.meeting.creator!!.phone}"
                        meeting_user_vk.text = "vk: ${meetingDetailResponse.meeting.creator!!.vk}"
                        meeting_detail_ok.isEnabled = true

                        mMeetingId = meetingDetailResponse.meeting._id
                    }
                }
            }
        })
        mDownloadMeetingDetail!!.execute(intent.getStringExtra("_id"))
    }

    inner class DownloadMeetingDetail(private val meetingDetailCallback: MeetingDetailCallback) : AsyncTask<String, Unit, Unit>() {

        private lateinit var meetingDetailResponse: MeetingDetailResponse

        override fun doInBackground(vararg params: String?) {
            val id = params[0]
            val url = "${preferences.getServerAddress()}/select/meetings/$id"

            val request = Request.Builder()
                    .url(url)
                    .build()

            meetingDetailResponse = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<MeetingDetailResponse> = moshi.adapter(MeetingDetailResponse::class.java)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                MeetingDetailResponse("json error", null)
            }
        }

        override fun onPostExecute(result: Unit?) {
            meetingDetailCallback.finish(meetingDetailResponse)
        }
    }

    inner class Subscribe(private val simpleCallback: SimpleCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var mSimpleResponse : SimpleResponse? = null

        override fun doInBackground(vararg params: Unit) {
            val url = "${preferences.getServerAddress()}/create/subscribe"
            val body = FormBody.Builder()
                    .add("userId", preferences.getUserId())
                    .add("meetingId", mMeetingId)
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            mSimpleResponse = try {
                val client = OkHttpClient()

                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<SimpleResponse> = moshi.adapter(SimpleResponse::class.java)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                SimpleResponse("JSON parse error", null)
            }
        }

        override fun onPostExecute(result: Unit?) {
            simpleCallback.finish(mSimpleResponse!!)
        }

    }
}
