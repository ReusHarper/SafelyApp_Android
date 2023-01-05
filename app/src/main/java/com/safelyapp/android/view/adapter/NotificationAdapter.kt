package com.safelyapp.android.view.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.R
import com.safelyapp.android.databinding.ListNotificationBinding
import com.safelyapp.android.view.data.Notify
import com.safelyapp.android.view.data.User
import com.safelyapp.android.view.fragments.NotificationsFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationAdapter(
    private val listNotificationsFragment: NotificationsFragment,
    private val context: Context,
    private val users: MutableList<User>,
    //private var location: Location
    private var location: ArrayList<Double>
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
        if (location.isEmpty()) {
            holder.tvNotification.text = "Solicitud de contacto"
            holder.tvMessage.layoutParams.height = 100
            holder.tvMessage.text = "${users[position].email} te ha enviado una invitación"
            holder.imgNotification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_contact_add))
        } else {
            Log.e("ALERT", "UBICACION: ${location[0]}")
            val currentTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("HH:mm")
            val currentTimeString = dateFormat.format(currentTime)

            holder.tvNotification.text = "Emergencia"
            holder.tvMessage.layoutParams.height = 200
            holder.tvMessage.text = "${users[position].email} te ha enviado una alerta de emergencia a las ${currentTimeString}. " +
                    "Estas son sus coordenas: ${location[0]}, ${location[1]}"
            holder.imgNotification.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_dangerous))
        }

        // Eliminacion de notificacion
        holder.btn_delete.setOnClickListener {
            Log.e("BTN_DELETE_NOTIFY", users[position].email)
            listNotificationsFragment.deleteNotification(users[position], position)
        }
    }

    override fun getItemCount(): Int = users.size

}