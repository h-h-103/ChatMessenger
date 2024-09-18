package com.example.chatmessenger.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessenger.adapters.MessagesAdapter
import com.example.chatmessenger.databinding.FragmentChatHomeBinding
import com.example.chatmessenger.util.Resource
import com.example.chatmessenger.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class ChatHomeFragment : Fragment() {

    private val binding by lazy { FragmentChatHomeBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ChatViewModel>()
    private val navArgs by navArgs<ChatHomeFragmentArgs>()
    private lateinit var messagesAdapter: MessagesAdapter
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        setInfo()

        binding.sendBtn.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(
                    firebaseAuth.currentUser!!.uid,
                    navArgs.recent.receiver,
                    messageText,
                    navArgs.recent.name,
                    navArgs.recent.receiverImg
                )
                binding.editTextMessage.text.clear()
            }
        }
        observeMessages()

        setupRvMessages()
        viewModel.getMessages(firebaseAuth.currentUser!!.uid, navArgs.recent.receiver)
        observeGetMessages()

    }

    private fun setInfo() {
        binding.apply {
            chatUserName.text = navArgs.recent.name
            chatUserStatus.text = navArgs.recent.status
            Glide.with(requireView()).load(navArgs.recent.receiverImg).into(chatImageViewUser)
        }
    }

    private fun observeMessages() {
        lifecycleScope.launchWhenStarted {
            viewModel.messages.collectLatest {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {}
                    is Resource.Error -> {
                        Log.e("Error", "Failed to send message: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun setupRvMessages() {
        messagesAdapter = MessagesAdapter(firebaseAuth)
        binding.messagesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            (layoutManager as LinearLayoutManager).stackFromEnd = true
            adapter = messagesAdapter
            messagesAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    scrollToPosition(messagesAdapter.itemCount - 1)
                }
            })

        }
    }

    private fun observeGetMessages() {
        lifecycleScope.launchWhenStarted {
            viewModel.getMessages.collectLatest {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        messagesAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        Log.e("Error", "Failed to send message: ${it.message}")
                    }

                    else -> Unit
                }
            }
        }
    }
}