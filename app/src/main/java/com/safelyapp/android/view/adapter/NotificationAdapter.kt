package com.safelyapp.android.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.databinding.ListNotificationBinding
import com.safelyapp.android.view.data.Notify
import com.safelyapp.android.view.data.User
import com.safelyapp.android.view.fragments.NotificationsFragment

class NotificationAdapter(
    private val listNotificationsFragment: NotificationsFragment,
    private val context: Context,
    private val users:MutableList<User>,
    private val notifications:MutableList<Notify>?
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: ListNotificationBinding): RecyclerView.ViewHolder(view.root){
        val tvNotification = view.tvNotification
        val tvMessage = view.tvMessage
        val imgNotification = view.imgType
        val btn_delete = view.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListNotificationBinding.inflate(layoutInflater)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvNotification.text = "Solicitud de contacto"
        holder.tvMessage.text = "${users[position].email} te ha enviado una invitación"

        // Eliminacion de notificacion
        holder.btn_delete.setOnClickListener {
            Log.e("BTN_DELETE_NOTIFY", users[position].email)
            listNotificationsFragment.deleteNotification(users[position], position)
        }
    }

    override fun getItemCount(): Int = users.size

}