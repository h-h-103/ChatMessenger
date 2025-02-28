package com.example.chatmessenger.model

data class Messages(
    val sender: String,
    val receiver: String,
    val message: String,
    val time: String
) {
    constructor() : this("", "", "", "")
    val id: String get() = "$sender-$receiver-$message-$time"
}