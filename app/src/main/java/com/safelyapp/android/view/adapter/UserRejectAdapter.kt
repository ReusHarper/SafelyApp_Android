package com.safelyapp.android.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.databinding.ListElementRequestBinding
import com.safelyapp.android.view.data.User
import com.safelyapp.android.view.fragments.ListRequestFragment

class UserRejectAdapter(
            private val listRequestFragment: ListRequestFragment,
            private val context: Context,
            private val users:MutableList<User>
        ) : RecyclerView.Adapter<UserRejectAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: ListElementRequestBinding): RecyclerView.ViewHolder(view.root){
        val tvUserName = view.tvName
        val tvEmail = view.tvEmail
        val btn_accept = view.btnAccept
        val btn_reject = view.btnReject
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListElementRequestBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvEmail.text = users[position].email
        holder.tvUserName.text = users[position].name

        // Aceptacion de invitacion de contacto
        holder.btn_accept.setOnClickListener {
            //Manejar el click
            Log.e("BTN_ACCEPT", "${users[position].email}")
            listRequestFragment.addUserContact(users[position], position)
        }

        // Descarte de invitacion de contacto
        holder.btn_reject.setOnClickListener {
            //Manejar el click
            Log.e("BTN_REJECT", "${users[position].email}")
            listRequestFragment.rejectUserContact(users[position], position)
        }
    }

    override fun getItemCount(): Int = users.size

}