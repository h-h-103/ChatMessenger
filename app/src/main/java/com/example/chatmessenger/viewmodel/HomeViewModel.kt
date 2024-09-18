package com.example.chatmessenger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessenger.model.RecentChats
import com.example.chatmessenger.model.User
import com.example.chatmessenger.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _users = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val users: Flow<Resource<User>> = _users

    private val _recentChats = MutableStateFlow<Resource<List<RecentChats>>>(Resource.Unspecified())
    val recentChats = _recentChats.asStateFlow()


    init {
        getCurrentUser()
        getAllUsers()
        getRecentChats()
    }

    private fun getCurrentUser() {
        viewModelScope.launch { _user.emit(Resource.Loading()) }
        firestore.collection("User").document(auth.uid!!).addSnapshotListener { value, error ->
            if (error != null) {
                viewModelScope.launch { _user.emit(Resource.Error(error.message.toString())) }
            } else {
                val user = value?.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch { _user.emit(Resource.Success(user)) }
                }
            }
        }
    }

    private fun getAllUsers() {
        viewModelScope.launch { _users.emit(Resource.Loading()) }
        firestore.collection("User").addSnapshotListener { value, error ->
            if (error != null) {
                viewModelScope.launch { _users.emit(Resource.Error(error.message.toString())) }
            } else {
                value?.documents?.forEach {
                    val user = it.toObject(User::class.java)
                    if (user?.userId != auth.currentUser?.uid) {
                        viewModelScope.launch { _users.emit(Resource.Success(user!!)) }
                    }
                }
            }
        }
    }

    private fun getRecentChats() {
        viewModelScope.launch {
            _recentChats.emit(Resource.Loading())
            try {
                firestore.collection("Conversation${auth.currentUser!!.uid}")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            viewModelScope.launch { _recentChats.emit(Resource.Error(error.message.toString())) }
                            return@addSnapshotListener
                        }
                        val recentList = value?.documents?.map { document ->
                            RecentChats(
                                name = document.getString("receiverName") ?: "",
                                message = document.getString("message") ?: "",
                                sender = document.getString("senderId") ?: "",
                                receiver = document.getString("receiverId") ?: "",
                                receiverImg = document.getString("receiverImg") ?: "",
                                time = document.getString("timestamp") ?: "",
                                person = document.getString("person") ?: "",
                                status = document.getString("status") ?: ""
                            )
                        } ?: emptyList()
                        viewModelScope.launch { _recentChats.emit(Resource.Success(recentList)) }
                    }
            } catch (e: Exception) {
                _recentChats.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}