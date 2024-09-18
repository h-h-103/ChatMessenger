package com.example.chatmessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessenger.model.Messages
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.util.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _messages = MutableStateFlow<Resource<List<Messages>>>(Resource.Unspecified())
    val messages = _messages.asStateFlow()

    private val _getMessages = MutableStateFlow<Resource<List<Messages>>>(Resource.Unspecified())
    val getMessages = _getMessages.asStateFlow()

    fun sendMessage(
        senderId: String,
        receiverId: String,
        messageText: String,
        receiverName: String,
        receiverImg: String
    ) {
        viewModelScope.launch {
            _messages.emit(Resource.Loading())
            val message = Messages(senderId, receiverId, messageText, Utils().getTime())

            val messageMap = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to message.message,
                "timestamp" to message.time
            )
            val uid = listOf(senderId, receiverId).sorted().joinToString("")

            try {
                // Save the message in the "Chats" sub-collection
                firestore.collection("Messages")
                    .document(uid)
                    .collection("Chats")
                    .document(message.time)
                    .set(messageMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val messageRecent = hashMapOf(
                                "senderId" to auth.currentUser!!.uid,
                                "receiverId" to receiverId,
                                "message" to message.message,
                                "timestamp" to message.time,
                                "receiverName" to receiverName,
                                "receiverImg" to receiverImg,
                                "person" to "You",
                                "status" to "default"
                            )

                            // Update the recent conversation for both users
                            firestore.collection("Conversation${auth.currentUser!!.uid}")
                                .document(receiverId)
                                .set(messageRecent)

                            firestore.collection("Conversation${receiverId}")
                                .document(auth.currentUser!!.uid)
                                .update("message", message.message)

                            viewModelScope.launch {
                                _messages.emit(Resource.Success(listOf(message)))
                            }
                        } else {
                            viewModelScope.launch {
                                _messages.emit(Resource.Error("Failed to send message"))
                            }
                        }
                    }
            } catch (e: Exception) {
                _messages.emit(Resource.Error(e.message.toString()))
            }
        }
    }


    fun getMessages(senderId: String, receiverId: String) {
        viewModelScope.launch {
            _getMessages.emit(Resource.Loading())
            val uid = listOf(senderId, receiverId).sorted().joinToString("")

            try {
                firestore.collection("Messages")
                    .document(uid)
                    .collection("Chats")
                    .orderBy("timestamp", Query.Direction.ASCENDING) // Retrieve in ascending order
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            viewModelScope.launch { _getMessages.emit(Resource.Error(e.message.toString())) }
                            return@addSnapshotListener
                        }

                        val messageList = snapshots?.documents?.map { document ->
                            Messages(
                                sender = document.getString("senderId") ?: "",
                                receiver = document.getString("receiverId") ?: "",
                                message = document.getString("message") ?: "",
                                time = document.getString("timestamp") ?: ""
                            )
                        } ?: emptyList()

                        viewModelScope.launch {
                            _getMessages.emit(Resource.Success(messageList))
                        }
                    }
            } catch (e: Exception) {
                _getMessages.emit(Resource.Error(e.message.toString()))
            }
        }
    }
}