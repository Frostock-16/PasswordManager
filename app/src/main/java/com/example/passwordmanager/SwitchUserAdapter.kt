package com.example.passwordmanager

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.modal.User

class SwitchUserAdapter(
    private val userList: List<User>,
    private val onUserClick:(User)->Unit
): RecyclerView.Adapter<SwitchUserAdapter.UserViewHolder>() {

    init {
        Log.d("UserAdapter", "Users: $userList")
    }

    inner class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val userIcon = itemView.findViewById<ImageView>(R.id.ivUserIcon)
        val userName = itemView.findViewById<TextView>(R.id.tvUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.userName
        holder.userIcon.setImageResource(user.icon)
        holder.itemView.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount(): Int = userList.size
}