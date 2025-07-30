package com.example.easychat.model

import com.google.firebase.Timestamp

class UserModel {
    var phone: String? = null
    var username: String? = null
    var createdTimestamp: Timestamp? = null
    var userId: String? = null
    var fcmToken: String? = null

    constructor()

    constructor(phone: String?, username: String?, createdTimestamp: Timestamp?, userId: String?) {
        this.phone = phone
        this.username = username
        this.createdTimestamp = createdTimestamp
        this.userId = userId
    }
}