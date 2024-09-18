package com.example.chatmessenger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessenger.R
import com.example.chatmessenger.databinding.UserItemBinding
import com.example.chatmessenger.model.User

class UserAdapter: RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    var onItemClick: ((User) -> Unit)? = null

    class UserViewHolder(private val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                userName.text = user.userName.split("\\s".toRegex())[0]
                Glide.with(root).load(user.imgPath).into(imageViewUser)
                if (user.status == "online") {
                    statusOnline.setImageResource(R.drawable.onlinestatus)
                } else {
                    statusOnline.setImageResource(R.drawable.offlinestatus)
                }
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.bind(user)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(user)
        }
    }
}