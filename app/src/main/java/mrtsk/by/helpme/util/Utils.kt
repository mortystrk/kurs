package mrtsk.by.helpme.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream

fun toBase64(imageView: ImageView) : String {
    val drawable = imageView.drawable as BitmapDrawable
    val bitmap = drawable.bitmap

    var byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

    val bytes = byteArrayOutputStream.toByteArray()

    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun fromBase64(image: String, imageView: ImageView, context: Context) {
    val decodedString = Base64.decode(image, Base64.DEFAULT)

    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

    Glide.with(context).load(decodedByte).into(imageView)
}

class AppPreferences(context: Context) {

    private val PREFERENCES_NAME = "AppPreferences"
    private val PREFERENCE_USER_ID = "UserId"
    private val PREFERENCE_SERVER_ADDRESS = "ServerAddress"
    private val PREFERENCE_USER_AVATAR = "UserAvatar"
    private val PREFERENCE_USER_NAME = "UserName"
    private val PREFERENE_POST_RATING = "PostRating"
    private val PREFERENE_USER_RATING = "UserRating"
    private val PREFERENCE_USER_AGE = "UserAge"

    private val preference = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setUserId(id: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_ID, id)
        editor.apply()
    }

    fun setUserName(name: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_NAME, name)
        editor.apply()
    }

    fun setPostRating(rating: Float) {
        val editor = preference.edit()
        editor.putFloat(PREFERENE_POST_RATING, rating)
        editor.apply()
    }

    fun setUserRating(rating: Double) {
        val editor = preference.edit()
        editor.putFloat(PREFERENE_POST_RATING, rating.toFloat())
        editor.apply()
    }

    fun setServerAddress(address: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_SERVER_ADDRESS, address)
        editor.apply()
    }

    fun setAvatar(avatar: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_AVATAR, avatar)
        editor.apply()
    }

    fun setAge(age: Int) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_AGE, age.toString())
        editor.apply()
    }

    fun getUserAge() : String {
        return preference.getString(PREFERENCE_USER_AGE, "-1")
    }

    fun getUserId() : String {
        return preference.getString(PREFERENCE_USER_ID, "undefined")
    }

    fun getServerAddress() : String {
        return preference.getString(PREFERENCE_SERVER_ADDRESS, "undefined")
    }

    fun getAvatar() : String {
        return preference.getString(PREFERENCE_USER_AVATAR, "undefined")
    }

    fun getName() : String {
        return preference.getString(PREFERENCE_USER_NAME, "undefined")
    }
    fun getPostRating() : Float {
        return preference.getFloat(PREFERENE_POST_RATING, 0f)
    }

    fun getUserRating() : Float {
        return preference.getFloat(PREFERENE_USER_RATING, 0f)
    }
}
