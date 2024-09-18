package com.example.chatmessenger.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessenger.di.BaseApp
import com.example.chatmessenger.model.User
import com.example.chatmessenger.util.RegisterValidation
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.util.validationEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltViewModel
class SettingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val fireStorage: StorageReference,
    app: Application): AndroidViewModel(app) {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _updateInfo = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val updateInfo = _updateInfo.asStateFlow()

    init {
        getUser()
    }

    private fun getUser() {
        viewModelScope.launch { _user.emit(Resource.Loading()) }
        firestore.collection("User").document(firebaseAuth.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch { _user.emit(Resource.Success(it)) }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _user.emit(Resource.Error(it.message.toString())) }
            }
    }

    fun updateUser(user: User, imageUri: Uri?) {
        val areInputsValid =
            validationEmail(user.email) is RegisterValidation.Success && user.userName.trim()
                .isNotEmpty()

        if (!areInputsValid) {
            viewModelScope.launch { _updateInfo.emit(Resource.Error("Check your inputs")) }
            return
        }

        viewModelScope.launch { _updateInfo.emit(Resource.Loading()) }

        if (imageUri == null) {
            saveUserInformation(user, true)
        } else {
            saveUserInformationWithNewImage(user, imageUri)
        }
    }

    private fun saveUserInformation(user: User, b: Boolean) {
        firestore.runTransaction { transaction ->
            val documentRef = firestore.collection("User").document(firebaseAuth.uid!!)
            if (b) {
                val currentUser = transaction.get(documentRef).toObject(User::class.java)
                val newUser = user.copy(imgPath = currentUser?.imgPath ?: "")
                transaction.set(documentRef, newUser)
            } else {
                transaction.set(documentRef, user)
            }
        }.addOnSuccessListener {
            viewModelScope.launch { _updateInfo.emit(Resource.Success(user)) }
        }.addOnFailureListener {
            viewModelScope.launch { _updateInfo.emit(Resource.Error(it.message.toString())) }
        }
    }

    private fun saveUserInformationWithNewImage(user: User, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<BaseApp>().contentResolver,
                    imageUri
                )
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                val imageDirectory =
                    fireStorage.child("profileImages/${firebaseAuth.uid!!}/${UUID.randomUUID()}")
                val result = imageDirectory.putBytes(imageByteArray).await()
                val imgUrl = result.storage.downloadUrl.await().toString()
                saveUserInformation(user.copy(imgPath = imgUrl), false)
            } catch (e: Exception) {
                viewModelScope.launch { _updateInfo.emit(Resource.Error(e.message.toString())) }
            }
        }
    }
}