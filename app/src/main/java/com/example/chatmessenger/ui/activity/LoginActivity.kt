@file:Suppress("DEPRECATION")

package com.example.chatmessenger.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.example.chatmessenger.databinding.ActivityLoginBinding
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var progressDialog: ProgressDialog
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainL)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        colorStatusBar()
        binding.signInTextToSignUp.setOnClickListener {
            goSignUp()
        }
        setOnClickLogin()
        stateRegisterButton()
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

    private fun goSignUp() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    // When click on login button
    private fun setOnClickLogin() {
        binding.apply {
            loginButton.setOnClickListener {
                val email = loginetemail.text.toString().trim()
                val password = loginetpassword.text.toString()
                viewModel.loginAccountWithEmailAndPassword(email, password)
            }
        }
    }

    // State click button
    private fun stateRegisterButton() {
        lifecycleScope.launchWhenStarted {
            viewModel.login.collect {
                when (it) {
                    is Resource.Loading -> {
                        progressDialog = ProgressDialog(this@LoginActivity)
                        progressDialog.setTitle("Login")
                        progressDialog.setCanceledOnTouchOutside(false)
                        progressDialog.show()
                    }

                    is Resource.Success -> {
                        progressDialog.dismiss()
                        val intent = Intent(this@LoginActivity, ChatActivity::class.java)
                        startActivity(intent)
                    }

                    is Resource.Error -> {
                        Snackbar.make(
                            binding.loginButton,
                            "Error: ${it.message}",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("OK") {}
                            .show()
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val mainIntent = Intent(this, ChatActivity::class.java)
            startActivity(mainIntent)
        }
    }
}