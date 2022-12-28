package com.safelyapp.android.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.databinding.ListElementContactBinding
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.data.User
import com.safelyapp.android.view.fragments.ListContactFragment

class UserContactAdapter(private val listContactFragment: ListContactFragment, private val context: Context, private val users:MutableList<User>) : RecyclerView.Adapter<UserContactAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: ListElementContactBinding): RecyclerView.ViewHolder(view.root){
        val tvUserName = view.tvName
        val tvEmail = view.tvEmail
        val btn_delete = view.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListElementContactBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvEmail.text = users[position].email
        holder.tvUserName.text = users[position].name

        // Eliminacion de contacto
        holder.btn_delete.setOnClickListener {
            //Manejar el click
            Log.e("BTN_DELETE", "${users[position].email}")
            /*Log.e("USER_EMAIL_DELETE", users[position].email)*/
            listContactFragment.deleteUserContact(users[position], position)
        }
    }

    override fun getItemCount(): Int = users.size

}