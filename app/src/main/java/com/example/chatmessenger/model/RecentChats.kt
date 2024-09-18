package com.example.chatmessenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class RecentChats(
    val name: String,
    val message: String,
    val sender: String,
    val receiver: String,
    val receiverImg: String,
    val time: String,
    val person: String,
    val status: String
): Parcelable {
    constructor() : this("", "", "", "", "", "", "", "")
}