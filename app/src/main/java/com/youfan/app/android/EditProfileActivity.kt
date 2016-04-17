package com.youfan.app.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.TextView
import android.widget.ImageView
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.youfan.app.android.data.SignUpResponse
import com.youfan.app.android.service.YouFanServices
import com.facebook.login.LoginManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.youfan.app.android.data.SignUpParams
import com.youfan.app.android.util.Constants
import com.youfan.app.android.util.RoundedTransformation
import rx.Observer

class EditProfileActivity : Activity() {
    lateinit var profilePicture: ImageView
    internal var permissions = arrayOf("android.permission.WRITE_EXTERNAL_STORAGE")
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    lateinit var dir:String

    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        profilePicture = findViewById(R.id.profile_picture) as ImageView
        val firstNameTextView = findViewById(R.id.enter_first_name) as EditText
        val lastNameTextView = findViewById(R.id.enter_last_name) as EditText
        val bioTextView = findViewById(R.id.enter_bio) as EditText
        val editPicture = findViewById(R.id.edit_profile_pic) as Button
        val doneButton = findViewById(R.id.done_button) as TextView
        val logOutButton = findViewById(R.id.log_out_button) as Button

        Picasso.with(this)
                .load(Uri.parse(Db.getUser().userInfo.photoUrl ?: ""))
                .error(R.drawable.ic_action_name)
                .transform(RoundedTransformation())
                .resizeDimen(R.dimen.about_me_profile_image_size, R.dimen.about_me_profile_image_size)
                .centerCrop()
                .into(profilePicture)

        //save to file
        dir = this.cacheDir.toString()
        Picasso.with(this).load(Uri.parse(Db.getUser().userInfo.photoUrl ?: ""))
                .placeholder(R.drawable.ic_action_name)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        Db.saveBitmapFile(bitmap, dir)
                    }

                    override fun onBitmapFailed(errorDrawable: Drawable) {

                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable) {

                    }
                })

        val userInfo = Db.getUser().userInfo
        firstNameTextView.setText(userInfo.firstName, TextView.BufferType.EDITABLE)
        lastNameTextView.setText(userInfo.lastName, TextView.BufferType.EDITABLE)
        bioTextView.setText(userInfo.bio, TextView.BufferType.EDITABLE)

        logOutButton.setOnClickListener {
            LoginManager.getInstance().logOut()
            Db.logOutClear(applicationContext)
            finish()
        }

        editPicture.setOnClickListener {
            verifyStoragePermissions(this)
            val galleryIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, Constants.RESULT_LOAD_IMG);
        }

        doneButton.setOnClickListener {
            val firstName = firstNameTextView.text.toString()
            val lastName = lastNameTextView.text.toString()
            val bio = bioTextView.text.toString()

            //TODO: Change profile pic
            val signUpParams = SignUpParams(firstName, lastName, bio,
                    Db.getFacebookId(), Db.getYouFanToken(), Db.getUserPhotoFile())

            if (signUpParams.areRequiredParamsFilled()) {
                signup(signUpParams)
            } else {
                val toast = Toast.makeText(applicationContext, "名字和自我介绍不能为空哦", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            // When an Image is picked
            if (requestCode == Constants.RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK
                    && null != data) {
                // Get the Image from data
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                // Get the cursor
                val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val imgDecodableString = cursor.getString(columnIndex)
                cursor.close()

                //Set the Image in ImageView after decoding the String
                val bitmap = BitmapFactory.decodeFile(imgDecodableString)
                profilePicture.setImageBitmap(bitmap)
                Db.saveBitmapFile(bitmap, dir)
            }
        } catch (e: Exception) {
            Log.d("error", e.message)
        }
    }

    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (permsRequestCode) {
            200 ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // save file
                } else {
                    Toast.makeText(applicationContext, "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                }
        }
    }

    fun signup(signUpParams: SignUpParams) {
        YouFanServices.getInstance().editProfile(signUpParams, object : Observer<SignUpResponse> {
            override fun onCompleted() {
                finish()
            }

            override fun onError(e: Throwable) {
                Log.d("edit profile", "error")
                Toast.makeText(applicationContext, resources.getString(R.string.cannot_edit), Toast.LENGTH_SHORT).show()
            }

            override fun onNext(signUpResponse: SignUpResponse) {
                Log.d("edit profile", "success")

                //set userInfo, Token and YouFan user_id in Db
                Db.setYouFanToken(signUpResponse.token)
                Db.getUser().userInfo = signUpResponse.user
                Db.setUserId(signUpResponse.user.id)

                //save to disk: userInfo(first name, last name, user bio, photoUrl),  YouFanToken
                Db.saveUserAndYouFanTokenToDisk(applicationContext)
            }
        })
    }
}
