package com.safelyapp.android.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.databinding.ListElementRequestBinding
import com.safelyapp.android.view.data.User

class UserAdapter(private val context: Context, private val users:MutableList<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: ListElementRequestBinding): RecyclerView.ViewHolder(view.root){
        val tvUserName = view.tvName
        val tvEmail = view.tvEmail
        //val tvUserName = view.tvTitle
        //val tvEmail = view.tvGenre
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListElementRequestBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvEmail.text = users[position].email
        holder.tvUserName.text = users[position].name
    }

    override fun getItemCount(): Int = users.size

}