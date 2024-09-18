package com.example.chatmessenger.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatmessenger.R
import com.example.chatmessenger.adapters.RecentAdapter
import com.example.chatmessenger.adapters.UserAdapter
import com.example.chatmessenger.databinding.FragmentHomeBinding
import com.example.chatmessenger.ui.activity.LoginActivity
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var userAdapter: UserAdapter
    private lateinit var recentAdapter: RecentAdapter
    private val homeViewModel by viewModels<HomeViewModel>()
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logOut.setOnClickListener {
            homeViewModel.logout()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.tlImage.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingFragment)
        }

        observeCurrentUser()

        setupRvUser()
        observeAllUsers()

        setupRvRecent()
        observeRecentChats()
    }

    private fun observeCurrentUser() {
        lifecycleScope.launchWhenStarted {
            homeViewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {

                        it.data?.let { user ->
                            Glide.with(requireView()).load(user.imgPath).error(R.drawable.person)
                                .into(binding.tlImage)
                        }
                    }

                    is Resource.Error -> {
                        Log.e("Error", "onViewCreated: ${it.message}")
                    }

                    else -> Unit
                }

            }
        }
    }


    private fun setupRvUser() {
        userAdapter = UserAdapter()
        binding.rvUsers.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = userAdapter
        }
    }

    private fun observeAllUsers() {
        lifecycleScope.launchWhenStarted {
            homeViewModel.users.collectLatest {
                when (it) {
                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        userAdapter.differ.submitList(listOf(it.data))
                    }

                    is Resource.Error -> {
                        Log.e("Error", "observeAllUsers: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }

        userAdapter.onItemClick = {
            val bundle = Bundle().apply { putParcelable("user", it) }
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment, bundle)
        }
    }


    private fun setupRvRecent() {
        recentAdapter = RecentAdapter()
        binding.rvRecentChats.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recentAdapter
        }
    }

    private fun observeRecentChats() {
        lifecycleScope.launchWhenStarted {
            homeViewModel.recentChats.collectLatest {
                when (it) {
                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        recentAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        Log.e("Error", "observeAllUsers: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }

        recentAdapter.onItemClick = {
            val bundle = Bundle().apply { putParcelable("recent", it) }
            findNavController().navigate(R.id.action_homeFragment_to_chatHomeFragment, bundle)
        }
    }
}