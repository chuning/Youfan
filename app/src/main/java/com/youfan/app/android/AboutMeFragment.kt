package com.youfan.app.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.youfan.app.android.data.CheckUserExistResponse
import com.youfan.app.android.service.YouFanServices
import com.squareup.picasso.Picasso
import com.youfan.app.android.util.AppUtil
import com.youfan.app.android.util.Constants
import com.youfan.app.android.util.RoundedTransformation
import rx.Observer

class AboutMeFragment : Fragment() {
    lateinit var overlay: FrameLayout
    lateinit var profilePictureView: ImageView
    lateinit var userNameTextView: TextView
    lateinit var userBioTextView: TextView

    companion object {
        @JvmStatic fun newInstance(): AboutMeFragment = AboutMeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.about_me, container, false) as View
        profilePictureView = view.findViewById(R.id.user_profile_picture) as ImageView
        userNameTextView = view.findViewById(R.id.user_name) as TextView
        userBioTextView = view.findViewById(R.id.user_bio) as TextView

        val eventCreated = view.findViewById(R.id.party_created_cardview) as CardView
        val eventJoined = view.findViewById(R.id.party_joined_cardview) as CardView
        val doneButton = view.findViewById(R.id.done_button) as TextView

        overlay = view.findViewById(R.id.overlay) as FrameLayout
        overlay.setOnClickListener { l ->
            startLoginActivity()
        }

        updateUI()

        eventCreated.setOnClickListener { l ->
            val intent = Intent(this.activity, MyEventActivity::class.java)
            intent.putExtra(Constants.MY_EVENT, Constants.EVENT_CREATED_REQUEST_CODE)
            startActivity(intent)
        }

        eventJoined.setOnClickListener {
            val intent = Intent(this.activity, MyEventActivity::class.java)
            intent.putExtra(Constants.MY_EVENT, Constants.EVENT_JOINED_REQUEST_CODE)
            startActivity(intent)
        }

        doneButton.setOnClickListener {
            val intent = Intent(this.activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    fun isLogin(): Boolean {
        return AppUtil.isLogin()
    }

    override fun onResume() {
        updateUI()
        super.onResume()
    }

    fun startLoginActivity() {
        val intent = Intent(this.activity, LoginActivity::class.java)
        startActivityForResult(intent, Constants.CHECK_USER_EXIST_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.CHECK_USER_EXIST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                updateUI()
            }
        }
    }

    fun updateUI() {
        overlay.visibility = if (isLogin()) View.GONE else View.VISIBLE

        val user = Db.getUser()?.userInfo

        setUserInfoVisible(user != null)
        if (user != null) {
            Picasso.with(context)
                    .load(Uri.parse(user.photoUrl ?: ""))
                    .error(R.drawable.ic_action_name)
                    .transform(RoundedTransformation())
                    .resizeDimen(R.dimen.about_me_profile_image_size, R.dimen.about_me_profile_image_size)
                    .centerCrop()
                    .into(profilePictureView)
            userNameTextView.text = user.firstName + " " + user.lastName
            userBioTextView.text = user.bio
        }
    }

    fun setUserInfoVisible(setVisible: Boolean) {
        if (setVisible) {
            profilePictureView.visibility = View.VISIBLE
            userNameTextView.visibility = View.VISIBLE
            userBioTextView.visibility = View.VISIBLE
        } else {
            profilePictureView.visibility = View.GONE
            userNameTextView.visibility = View.GONE
            userBioTextView.visibility = View.GONE
        }
    }
}