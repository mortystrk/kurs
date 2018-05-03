package mrtsk.by.helpme.mongooseModel

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_mongoose_test.*
import mrtsk.by.helpme.R
import okhttp3.OkHttpClient
import okhttp3.Request

class MongooseTest : AppCompatActivity() {

    private val SERVER_ADDRESS = "http://192.168.43.20:3334"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mongoose_test)

        val download = Download()
        download.execute()
    }

    inner class Download : AsyncTask<Unit, Unit, Unit>() {
        private var phone: Phone? = null

        override fun doInBackground(vararg params: Unit?) {
            val url = SERVER_ADDRESS + "/phone"

            val client = OkHttpClient()
            val request = Request.Builder()
                    .url(url)
                    .build()

            //phone = try {
                val moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<Phone> = moshi.adapter(Phone::class.java)

                val response = client.newCall(request).execute()

                phone = jsonAdapter.fromJson(response.body().string())!!
            //} catch (e: Exception) {
              //  null
            //}
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: Unit?) {
            if (phone != null) {
                tv_mongoose.text = "phone name: ${phone!!.name}; owner name: ${phone!!.owner.name}; " +
                        "owner _id: ${phone!!.owner._id}"
            } else {
                tv_mongoose.text = "null"
            }
        }

    }
}
