package com.example.chatmessenger.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessenger.databinding.RecentChatItemBinding
import com.example.chatmessenger.model.RecentChats

@SuppressLint("SetTextI18n")
class RecentAdapter: RecyclerView.Adapter<RecentAdapter.RecentViewHolder>() {

    var onItemClick: ((RecentChats) -> Unit)? = null

    class RecentViewHolder(private val binding: RecentChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recent: RecentChats) {
            Glide.with(itemView).load(recent.receiverImg).into(binding.recentChatImageView)
            binding.recentChatTextName.text = recent.name
            val lastMessage = recent.message.take(15)
            if (recent.person == "You") {
                binding.recentChatTextLastMessage.text = "Last Message: $lastMessage"
            } else {
                binding.recentChatTextLastMessage.text = "${recent.name}: $lastMessage"
            }
            binding.recentChatTextTime.text = recent.time.substring(0, 5)
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<RecentChats>() {
        override fun areItemsTheSame(oldItem: RecentChats, newItem: RecentChats): Boolean {
            return oldItem.sender == newItem.sender
        }

        override fun areContentsTheSame(oldItem: RecentChats, newItem: RecentChats): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        return RecentViewHolder(
            RecentChatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        val recent = differ.currentList[position]
        holder.bind(recent)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(recent)
        }
    }
}