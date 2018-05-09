package mrtsk.by.helpme.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.fragment_my_subscriptions.*

import mrtsk.by.helpme.R
import mrtsk.by.helpme.callbacks.SimpleCallback
import mrtsk.by.helpme.callbacks.SimpleMeetingCallback
import mrtsk.by.helpme.models.SimpleMeeting
import mrtsk.by.helpme.responses.SimpleMeetingResponse
import mrtsk.by.helpme.responses.SimpleResponse
import mrtsk.by.helpme.util.AppPreferences
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class MySubscriptionsFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var preferences: AppPreferences
    private var mDownloadSub : DownloadMySub? = null
    private var mDeleteSub: DeleteSub? = null
    private lateinit var subs : ArrayList<SimpleMeeting>
    private lateinit var adapter: MySubListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subs = ArrayList()
        preferences = AppPreferences(context)
        mDownloadSub = DownloadMySub(object : SimpleMeetingCallback {
            override fun finish(simpleMeetingResponse: SimpleMeetingResponse) {
                when (simpleMeetingResponse.getError()) {
                    "mongoose find error" -> {
                        Snackbar.make(
                                sub_fragment,
                                "Серверная ошибка. Попробуйте позже",
                                Snackbar.LENGTH_SHORT
                        ).show()
                        return
                    }

                    "meetings not found" -> {
                        Snackbar.make(
                                sub_fragment,
                                "Встреч нет",
                                Snackbar.LENGTH_SHORT
                        ).show()
                        return
                    }

                    "mongoose connect error" -> {
                        Snackbar.make(
                                sub_fragment,
                                "Серверная ошибка. Попробуйте позже",
                                Snackbar.LENGTH_SHORT
                        ).show()
                        return
                    }

                    "no error" -> {
                        subs.addAll(simpleMeetingResponse.getMeetings()!!)
                        adapter = MySubListViewAdapter(subs)

                        my_subscr_list.adapter = adapter
                    }

                    "JSON cast error" -> {
                        Snackbar.make(
                                sub_fragment,
                                "JSON cast error",
                                Snackbar.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
            }
        })
        mDownloadSub!!.execute()
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_my_subscriptions, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    inner class DownloadMySub(private val simpleMeetingCallback: SimpleMeetingCallback) : AsyncTask<Unit, Unit, Unit>() {
        private var simpleMeetingResponse: SimpleMeetingResponse? = null

        override fun doInBackground(vararg params: Unit?) {
            val id = "5aeb6c476eee6ca8514f6f4f"
            val url = "${preferences.getServerAddress()}/select/subscriptions/$id"

            var request = Request.Builder()
                    .url(url)
                    .build()

            simpleMeetingResponse = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<SimpleMeetingResponse> = moshi.adapter(SimpleMeetingResponse::class.java)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                SimpleMeetingResponse("JSON cast error", null)
            }
        }

        override fun onPostExecute(result: Unit?) {
            simpleMeetingCallback.finish(simpleMeetingResponse!!)
        }
    }

    inner class DeleteSub(private val simpleCallback: SimpleCallback) : AsyncTask<String, Unit, Unit>() {

        private var mSimpleResponse: SimpleResponse? = null

        override fun doInBackground(vararg params: String?) {
            var meetingId = params[0]
            var url = "${preferences.getServerAddress()}/delete/subscribe/5aeb6c476eee6ca8514f6f4f/$meetingId"

            var request = Request.Builder()
                    .url(url)
                    .delete()
                    .build()

            mSimpleResponse = try {
                val client = OkHttpClient()
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<SimpleResponse> = moshi.adapter(SimpleResponse::class.java)

                val response = client.newCall(request).execute()

                jsonAdapter.fromJson(response.body().string())!!
            } catch (e: Exception) {
                SimpleResponse("JSON cast err", null)
            }
        }

        override fun onPostExecute(result: Unit?) {
            simpleCallback.finish(mSimpleResponse!!)
        }

    }

    inner class MySubListViewAdapter(private var subList: ArrayList<SimpleMeeting>) : BaseAdapter() {
        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View?
            val viewHolder: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.my_subscribe_item, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            viewHolder.subName.text = subList[position].name
            viewHolder.unSub.setOnClickListener {
                var meetingId = subList[position]._id
                mDeleteSub = DeleteSub(object : SimpleCallback {
                    override fun finish(simpleResponse: SimpleResponse) {
                        when (simpleResponse.error) {
                            "update err" -> {
                                Snackbar.make(
                                        sub_fragment,
                                        "Ошибка поиска",
                                        Snackbar.LENGTH_SHORT
                                ).show()
                                return
                            }
                            "no error" -> {
                                subs.remove(subs.find { it._id == meetingId })
                                adapter.notifyDataSetChanged()
                            }
                            "JSON cast err" -> {
                                Snackbar.make(
                                        sub_fragment,
                                        "JSON cast err",
                                        Snackbar.LENGTH_SHORT
                                ).show()
                                return
                            }
                        }
                    }
                })
                mDeleteSub!!.execute(meetingId)
            }


            return view!!
        }

        override fun getItem(position: Int): Any {
            return subList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return subList.size
        }
    }

    private class ViewHolder(view: View?) {
        val subName: TextView = view?.findViewById<TextView>(R.id.my_sub_name) as TextView
        val unSub: Button = view?.findViewById<Button>(R.id.my_sub_cancel_button) as Button
    }

    companion object {
        fun newInstance() : MySubscriptionsFragment {
            val fragment = MySubscriptionsFragment()
            return fragment
        }
    }
}
