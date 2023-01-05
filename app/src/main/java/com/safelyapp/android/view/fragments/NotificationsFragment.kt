package com.safelyapp.android.view.fragments

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.GeoPoint
import com.safelyapp.android.databinding.FragmentNotificationsBinding
import com.safelyapp.android.view.Database.DbContacts
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.adapter.NotificationAdapter
import com.safelyapp.android.view.data.Notify
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface FragmentNotificationsCallback {
    fun deleteNotification(notifications: User, position: Int)
}

class NotificationsFragment : Fragment(), FragmentNotificationsCallback {

    // ========== General ==========
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notify: Notify
    private lateinit var notifyAdapter: NotificationAdapter
    private val dbContacts = DbContacts()

    // ========== Elements ==========
    private var listNotificationsRequest: List<String> = listOf()
    private var listNotificationsAlerts: List<Any> = listOf()
    private var listDataUsers: MutableList<User> = mutableListOf()
    private lateinit var listNotifications: RecyclerView

    // ========== Ciclo de vida ==========
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        listNotifications = binding.rvListNotifications

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        getNotifications()
    }

    override fun onResume() {
        super.onResume()

        // Si se regresa a la vista de notificaciones, se debera reiniciar el recycler view actual
        // para evitar notificaciones duplicadas
        if (listDataUsers.isNotEmpty()) {
            listDataUsers.clear()
            notifyAdapter.notifyDataSetChanged()
            viewMessageListNotifications()
        }
    }

    // ========== Metodos propios ==========
    // Visualizacion de lista de peticion de contacto
    private fun viewListNotifications(location: ArrayList<Double>?) {
        if (location != null)
            notifyAdapter = NotificationAdapter(this@NotificationsFragment, requireContext(), listDataUsers, location)
        else {
            var locationEmpty = arrayListOf<Double>()
            notifyAdapter = NotificationAdapter(this@NotificationsFragment, requireContext(), listDataUsers, locationEmpty)
        }

        // Muestreo de notificaciones
        listNotifications.layoutManager = LinearLayoutManager(requireContext())
        listNotifications.adapter = notifyAdapter

        viewMessageListNotifications()
    }

    // Se muestra un mensaje en caso de que la lista de contactos se encuentre vacia
    private fun viewMessageListNotifications() {
        if(notifyAdapter.itemCount == 0)
            binding.tvWithoutNotifications.visibility = View.VISIBLE
        else
            binding.tvWithoutNotifications.visibility = View.INVISIBLE
    }

    private fun getNotifications() {
        var listNotificationsRequestCurrent: MutableList<String> = mutableListOf()
        var listNotificationsAlertsCurrent: MutableList<Any> = mutableListOf()

        lifecycleScope.launch(Dispatchers.IO){

            // Filtrado de notificaciones de tipo request_
            val docRef = (activity as HomeActivity).db
                .collection("notifications").document((activity as HomeActivity).email)

            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    for (field in document.data!!.keys) {
                        if (field.startsWith("request_")) {
                            val value = document.get(field)
                            listNotificationsRequestCurrent.add(value.toString())
                        }

                        else if (field.startsWith("alert_")) {
                            val value = document.get(field) as List<Any>
                            listNotificationsAlertsCurrent.add(value)
                        }
                    }
                }
            }.await()

            // Por cada notificacion se obtiene su informacion de contacto
            withContext(Dispatchers.Main)  {
                if (listNotificationsRequestCurrent.isNotEmpty()) {
                    listNotificationsRequest = listNotificationsRequestCurrent

                    for (user in listNotificationsRequest)
                        getNotifyData(user, null)
                }

                if (listNotificationsAlertsCurrent.isNotEmpty()) {
                    listNotificationsAlerts = listNotificationsAlertsCurrent

                    for (field in listNotificationsAlerts) {
                        val data = field as ArrayList<Any>
                        val email = data[0] as String
                        val location = getLocation(data[1] as GeoPoint)

                        getNotifyData(email, location)
                    }
                }
                //viewListNotifications()
            }
        }
    }

    private fun getLocation(geoPoint: GeoPoint) : Location {
        val latitude = geoPoint.latitude
        val longitude = geoPoint.longitude
        val location = Location("")

        location.latitude = latitude
        location.longitude = longitude

        return location
    }

    // Obtencion de la informacion de cada contacto
    private fun getNotifyData(email: String, location: Location?) {

        var userCurrent: User?
        var locationArray: ArrayList<Double> = ArrayList()
        if (location != null) {
            locationArray.add(location.latitude)
            locationArray.add(location.longitude)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            notifyAdapter = NotificationAdapter(this@NotificationsFragment, requireContext(), listDataUsers, locationArray)

            // Consulta de los datos de cada contacto almacenados en el servidor de Firetore
            userCurrent = dbContacts.getDataUser(requireContext(), email)

            // Agregacion y actualizacion de lista de peticion de contacto
            withContext(Dispatchers.Main) {
                if (userCurrent != null) {
                    val lastPosition = listDataUsers.size
                    listDataUsers.add(lastPosition, userCurrent!!)
                    notifyAdapter.notifyItemInserted(lastPosition)
                }
                viewListNotifications(locationArray)
            }
        }
    }

    override fun deleteNotification(user: User, position: Int) {
        val email = "request_${replaceFormatEmail(user.email)}"

        // Eliminacion de notificacion en Firestore
        lifecycleScope.launch(Dispatchers.IO) {
            dbContacts.deleteRegister("notifications", (activity as HomeActivity).email, email)
        }

        updateList(position)
    }

    // Actualizacion de RecyclerView
    private fun updateList(position: Int) {
        listDataUsers.removeAt(position)
        notifyAdapter.notifyItemRemoved(position)

        viewMessageListNotifications()
    }

    // Cambio de caracter especial punto (.) por guion bajo (_)
    private fun replaceFormatEmail(email: String): String {
        return email.replace(".", "_")
    }

}