package com.example.chatmessenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessenger.databinding.ChatLeftItemBinding
import com.example.chatmessenger.databinding.ChatRightItemBinding
import com.example.chatmessenger.model.Messages
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(private val auth: FirebaseAuth) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Messages) -> Unit)? = null
    private val left = 0
    private val right = 1

    class LeftMessagesViewHolder(private val binding: ChatLeftItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messages: Messages) {
            binding.apply {
                showMessage.visibility = if (messages.message.isEmpty()) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
                timeView.visibility = if (messages.time.isEmpty()) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
                showMessage.text = messages.message
                timeView.text = messages.time.substring(0, 5)
            }
        }
    }

    class RightMessagesViewHolder(private val binding: ChatRightItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messages: Messages) {
            binding.apply {
                showMessage.visibility = if (messages.message.isEmpty()) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
                timeView.visibility = if (messages.time.isEmpty()) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
                showMessage.text = messages.message
                timeView.text = messages.time.substring(0, 5)
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<Messages>() {
        override fun areItemsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == left) {
            val binding =
                ChatLeftItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LeftMessagesViewHolder(binding)
        } else {
            val binding =
                ChatRightItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            RightMessagesViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messages = differ.currentList[position]
        if (holder is LeftMessagesViewHolder) {
            holder.bind(messages)
        } else if (holder is RightMessagesViewHolder) {
            holder.bind(messages)
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(messages)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = differ.currentList[position]
        return if (auth.currentUser?.uid == messages.sender) right else left
    }
}