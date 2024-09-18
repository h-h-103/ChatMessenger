package com.example.chatmessenger.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chatmessenger.databinding.FragmentSettingBinding
import com.example.chatmessenger.model.User
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SettingFragment : Fragment() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<SettingViewModel>()
    private var imgUrl: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                it.data?.data?.let { imageUri ->
                    imgUrl = imageUri // Store image URI for saving later
                    Glide.with(this).load(imageUri).into(binding.settingUpdateImage)
                }
            }

        binding.settingBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        collectUser()
        saveUserInformation()
        updateImageUser()
        collectUpdateInfo()
    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(requireView()).load(data.imgPath).error(ColorDrawable(Color.BLACK)).into(settingUpdateImage)
            settingUpdateName.setText(data.userName)
        }
    }

    private fun collectUser() {
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        showUserInformation(it.data!!)
                    }

                    is Resource.Error -> {
                        Log.e("Error", "collectUser: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun collectUpdateInfo() {
        lifecycleScope.launchWhenStarted {
            viewModel.updateInfo.collectLatest {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        findNavController().navigateUp()
                    }

                    is Resource.Error -> {
                        Log.e("Error", "collectUser: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun saveUserInformation() {
        binding.settingUpdateButton.setOnClickListener {
            val userId = viewModel.user.value.data?.userId
            val userName = binding.settingUpdateName.text.toString()
            val userEmail = viewModel.user.value.data?.email
            val status = viewModel.user.value.data?.status
            val user = User(userId!!, userName, userEmail!!, status!!)
            viewModel.updateUser(user, imgUrl)
        }
    }

    private fun updateImageUser() {
        binding.settingUpdateImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityResultLauncher.launch(intent)
        }
    }
}