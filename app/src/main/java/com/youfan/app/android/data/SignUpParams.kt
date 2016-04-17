package com.youfan.app.android.data

import java.io.File
import java.util.HashMap

public data class SignUpParams(val firstName: String?, val lastName: String?, val bio: String?, val facebookId: String, val facebookAccessToken: String, val userPhoto: File?){

    public fun areRequiredParamsFilled(): Boolean {
        return isFirstNameFilled() && isLastNameFilled() && isBioFilled() && hasUserPhoto()
    }

    public fun isFirstNameFilled(): Boolean {
        return !firstName.isNullOrEmpty()
    }

    public fun isLastNameFilled(): Boolean {
        return !lastName.isNullOrEmpty()
    }

    public fun isBioFilled(): Boolean {
        return !bio.isNullOrEmpty()
    }

    public fun hasUserPhoto(): Boolean {
        return userPhoto != null
    }
}