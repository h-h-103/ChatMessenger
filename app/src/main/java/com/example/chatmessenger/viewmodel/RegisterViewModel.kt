package com.example.chatmessenger.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chatmessenger.model.User
import com.example.chatmessenger.util.RegisterFieldsState
import com.example.chatmessenger.util.RegisterValidation
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.util.validationEmail
import com.example.chatmessenger.util.validationPassword
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth, private val db: FirebaseFirestore) : ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithUserAndPassword(name: String, email: String, password: String) {
        if (checkValidation(email, password)) {
            runBlocking { _register.emit(Resource.Loading()) }
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = User(authResult.user!!.uid, name, email, "default", "")
                    saveUserData(user)
                }
                .addOnFailureListener {
                    _register.value = Resource.Error(it.message.toString())
                }
        } else {
            val registerFieldsState = RegisterFieldsState(
                validationEmail(email), validationPassword(password)
            )
            runBlocking {
                _validation.send(registerFieldsState)
            }
        }
    }

    private fun checkValidation(email: String, password: String): Boolean {
        val emailValidation = validationEmail(email)
        val passwordValidation = validationPassword(password)
        val shouldRegister =
            emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
        return shouldRegister
    }

    private fun saveUserData(user: User) {
        db.collection("User").document(user.userId).set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }
            .addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }
}