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
