package com.safelyapp.android.view.fragments

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.databinding.FragmentListContactBinding
import com.safelyapp.android.view.Database.DbContacts
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.adapter.UserContactAdapter
import com.safelyapp.android.view.adapter.UserRejectAdapter
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface FragmentContactCallback {
    fun deleteUserContact(user: User, position: Int)
}

class ListContactFragment : Fragment(), FragmentContactCallback {

    // ========== General ==========
    private var _binding: FragmentListContactBinding? = null
    private val binding get() = _binding!!
    private val dbContacts = DbContacts()
    private lateinit var userContactAdapter: UserContactAdapter
    //private var listener: FragmentContactCallback? = null

    // ========== Elements ==========
    private var listEmailUsers: List<String> = listOf()
    private var listDataUsers: MutableList<User> = mutableListOf()
    private lateinit var listAddContact: RecyclerView

    // ========== Ciclo de vida ==========
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListContactBinding.inflate(inflater, container, false)
        listAddContact = binding.rvListContacts

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        getUsers()
    }

    // ========== Metodos propios ==========
    // Visualizacion de lista de peticion de contacto
    private fun viewListContact() {
        userContactAdapter = UserContactAdapter(this, requireContext(), listDataUsers)

        // Muestreo de lista de contacto
        listAddContact.layoutManager = LinearLayoutManager(requireContext())
        listAddContact.adapter = userContactAdapter

        viewMessageListContact()
    }

    // Obtencion de todos los correos de peticiones de contacto
    private fun getUsers() {
        var listEmailUsersCurrent: List<String> = listOf()

        lifecycleScope.launch(Dispatchers.IO){
            // Consulta de todos los contactos almacenados del usuario en el servidor de Firetore
            listEmailUsersCurrent = dbContacts.getListUsers(requireContext(), "contact", (activity as HomeActivity).email)

            // Por cada usuario agregado a la lista de peticiones, se obtiene su informacion de contacto
            withContext(Dispatchers.Main)  {
                if (listEmailUsersCurrent.isNotEmpty()) {
                    listEmailUsers = listEmailUsersCurrent

                    for (user in listEmailUsersCurrent)
                        getUserData(user)
                }
                viewListContact()
            }
        }
    }

    // Obtencion de la informacion de cada contacto
    private fun getUserData(email: String) {
        userContactAdapter = UserContactAdapter(this, requireContext(), listDataUsers)
        var userCurrent: User?

        lifecycleScope.launch(Dispatchers.IO) {
            // Consulta de los datos de cada contacto almacenados en el servidor de Firetore
            userCurrent = dbContacts.getDataUser(requireContext(), email)

            // Agregacion y actualizacion de lista de peticion de contacto
            withContext(Dispatchers.Main) {
                if (userCurrent != null) {
                    val lastPosition = listDataUsers.size
                    listDataUsers.add(lastPosition, userCurrent!!)
                    userContactAdapter.notifyItemInserted(lastPosition)
                }
                viewListContact()
            }
        }
    }

    // Cuando se elimina una invitacion de contacto se debe eliminar del archivo de peticiones almacenado
    // en Firestore y de igual forma se debe actualizar la vista actual de peticiones
    override fun deleteUserContact(userContactDelete: User, position: Int) {
        val emailCurrentlyUser = "email_${replaceFormatEmail((activity as HomeActivity).email)}"
        val emailContactUser = "email_${replaceFormatEmail(userContactDelete.email)}"

        // Eliminacion de usuario en Firestore
        lifecycleScope.launch(Dispatchers.IO) {
            dbContacts.deleteRegister("contact", (activity as HomeActivity).email, emailContactUser)
            dbContacts.deleteRegister("contact", userContactDelete.email, emailCurrentlyUser)
        }

        updateList(position)
    }

    // Se muestra un mensaje en caso de que la lista de contactos se encuentre vacia
    private fun viewMessageListContact() {
        if(userContactAdapter.itemCount == 0)
            binding.tvWithoutContacts.visibility = View.VISIBLE
        else
            binding.tvWithoutContacts.visibility = View.INVISIBLE
    }

    // Cambio de caracter especial punto (.) por guion bajo (_)
    private fun replaceFormatEmail(email: String): String {
        return email.replace(".", "_")
    }

    // Actualizacion de RecyclerView
    private fun updateList(position: Int) {
        listDataUsers.removeAt(position)
        userContactAdapter.notifyItemRemoved(position)

        viewMessageListContact()
    }

}