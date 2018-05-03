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
    private val PREFERENCE_POST_RATING = "PostRating"
    private val PREFERENCE_USER_RATING = "UserRating"
    private val PREFERENCE_USER_AGE = "UserAge"
    private val PREFERENCE_IS_NEW_USER = "isNewUser"
    private val PREFERENCE_POST_DESCRIPTION = "PostDescription"
    private val PREFERENCE_POST_TEXT = "PostText"
    private val PREFERENCE_POST_ID = "PostId"

    private val preference = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setUserId(id: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_ID, id)
        editor.apply()
    }

    fun setPostId(id: Int) {
        val editor = preference.edit()
        editor.putInt(PREFERENCE_POST_ID, id)
        editor.apply()
    }

    fun setUserName(name: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_USER_NAME, name)
        editor.apply()
    }

    fun setPostRating(rating: Float) {
        val editor = preference.edit()
        editor.putFloat(PREFERENCE_POST_RATING, rating)
        editor.apply()
    }

    fun setUserRating(rating: Double) {
        val editor = preference.edit()
        editor.putFloat(PREFERENCE_POST_RATING, rating.toFloat())
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

    fun setNewPost(flag: Boolean) {
        val editor = preference.edit()
        editor.putBoolean(PREFERENCE_IS_NEW_USER, flag)
        editor.apply()
    }

    fun setPostText(text: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_POST_TEXT, text)
        editor.apply()
    }

    fun setPostDescription(description: String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_POST_DESCRIPTION, description)
        editor.apply()
    }

    fun getUserAge() : String {
        return preference.getString(PREFERENCE_USER_AGE, "-1")
    }

    fun getUserId() : String {
        return preference.getString(PREFERENCE_USER_ID, "undefined")
    }

    fun getPostId() : Int {
        return preference.getInt(PREFERENCE_POST_ID, -1)
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
        return preference.getFloat(PREFERENCE_POST_RATING, 0f)
    }

    fun getUserRating() : Float {
        return preference.getFloat(PREFERENCE_USER_RATING, 0f)
    }

    fun getPostText() : String {
        return preference.getString(PREFERENCE_POST_TEXT, "")
    }

    fun getPostDescription() : String {
        return preference.getString(PREFERENCE_POST_DESCRIPTION, "")
    }

    fun isNewPost() : Boolean {
        return preference.getBoolean(PREFERENCE_IS_NEW_USER, false)
    }
}
