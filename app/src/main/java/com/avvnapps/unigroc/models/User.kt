package com.avvnapps.unigroc.models

import com.google.firebase.auth.FirebaseUser

data class User(
    var uid: String? = "",
    var displayName: String? = "",
    var email: String? = "",
    var displayImageUrl: String? = "",
    var address: String? = "",
    var deviceToken: String? = ""
) {

    constructor(user: FirebaseUser) : this(
        user.uid,
        user.displayName,
        user.email,
        user.phoneNumber,
        user.photoUrl.toString()
    )

}
