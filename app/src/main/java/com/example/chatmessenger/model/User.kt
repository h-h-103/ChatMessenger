package com.example.chatmessenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class User(
    val userId: String,
    val userName: String,
    val email: String,
    val status: String,
    val imgPath: String = ""
): Parcelable {
    constructor() : this("", "", "", "", "")
}