@file:Suppress("DEPRECATION")

package com.example.chatmessenger.ui.activity

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.chatmessenger.R
import com.example.chatmessenger.databinding.ActivityRegisterBinding
import com.example.chatmessenger.util.RegisterValidation
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<RegisterViewModel>()
    private lateinit var progressDialog: ProgressDialog
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainR)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        colorStatusBar()
        binding.signUpTextToSignIn.setOnClickListener {
            goLogin()
        }

        setOnClickRegister()
        stateRegisterButton()
        stateValidation()
    }

    private fun colorStatusBar() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_color)
        setStatusBarIconColor(false)
    }

    // Set the status bar icon color based on the current theme
    private fun setStatusBarIconColor(isDarkIcons: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above
            window.insetsController?.setSystemBarsAppearance(
                if (isDarkIcons) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // For older versions
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isDarkIcons) {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }

    private fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // When click on login button
    private fun setOnClickRegister() {
        binding.apply {
            signUpBtn.setOnClickListener {
                val name = signUpEtName.text.toString()
                val email = signUpEmail.text.toString()
                val password = signUpPassword.text.toString()
                viewModel.createAccountWithUserAndPassword(name, email, password)
            }
        }
    }

    // State click button
    private fun stateRegisterButton() {
        lifecycleScope.launchWhenStarted {
            viewModel.register.collect {
                when (it) {
                    is Resource.Loading -> {
                        progressDialog = ProgressDialog(this@RegisterActivity)
                        progressDialog.setTitle("Creating Account")
                        progressDialog.setCanceledOnTouchOutside(false)
                        progressDialog.show()
                    }

                    is Resource.Success -> {
                        progressDialog.dismiss()
                        goLogin()
                        Log.d(TAG, "onSuccess: ${it.data}")
                    }

                    is Resource.Error -> {
                        progressDialog.dismiss()
                        Log.d(TAG, "onError: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    // State validation
    private fun stateValidation() {
        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect { validation ->
                if (validation.email is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.signUpEmail.apply {
                            requestFocus()
                            error = validation.email.message
                        }
                    }
                }
                if (validation.password is RegisterValidation.Failed) {
                    withContext(Dispatchers.Main) {
                        binding.signUpPassword.apply {
                            requestFocus()
                            error = validation.password.message
                        }
                    }
                }
            }
        }
    }
}