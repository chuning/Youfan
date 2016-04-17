package com.youfan.app.android

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.youfan.app.android.data.UserInfo
import com.squareup.picasso.Picasso
import com.youfan.app.android.util.RoundedTransformation
import java.math.RoundingMode

class EventAttendantView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {
    lateinit var attendantNameView: TextView
    lateinit var attendantPictureView: ImageView

    override fun onFinishInflate() {
        super.onFinishInflate()
        attendantNameView = findViewById(R.id.attendant_name) as TextView
        attendantPictureView = findViewById(R.id.attendant_profile_picture) as ImageView
    }

    fun bind(person: UserInfo) {
        attendantNameView.text = person.firstName
        Picasso.with(context)
                .load(Uri.parse(person.photoUrl))
                .transform(RoundedTransformation())
                .resizeDimen(R.dimen.attendant_image_size, R.dimen.attendant_image_size)
                .centerCrop()
                .into(attendantPictureView)
    }
}