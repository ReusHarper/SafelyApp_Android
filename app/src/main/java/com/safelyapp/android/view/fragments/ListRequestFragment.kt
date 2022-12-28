package com.safelyapp.android.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safelyapp.android.databinding.FragmentListRequestBinding
import com.safelyapp.android.view.Database.DbContacts
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.adapter.UserRejectAdapter
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface FragmentRequestCallback {
    fun rejectUserContact(user: User, position: Int)
    fun addUserContact(user: User, position: Int)
}

class ListRequestFragment : Fragment(), FragmentRequestCallback {

    // ========== General ==========
    private var _binding: FragmentListRequestBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User
    private lateinit var userRejectAdapter: UserRejectAdapter
    private val dbContacts = DbContacts()


    // ========== Elements ==========
    private var listEmailUsers: List<String> = listOf()
    private var listDataUsers: MutableList<User> = mutableListOf()
    private lateinit var listRequestContact: RecyclerView


    // ========== Ciclo de vida ==========
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListRequestBinding.inflate(inflater, container, false)
        listRequestContact = binding.rvListRequest

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        getUsers()
    }

    // ========== Metodos propios ==========
    // Visualizacion de lista de peticion de contacto
    private fun viewListContact() {
        userRejectAdapter = UserRejectAdapter(this, requireContext(), listDataUsers)

        // Muestreo de lista de contacto
        listRequestContact.layoutManager = LinearLayoutManager(requireContext())
        listRequestContact.adapter = userRejectAdapter

        viewMessageListContact()
    }

    // Se muestra un mensaje en caso de que la lista de contactos se encuentre vacia
    private fun viewMessageListContact() {
        if(userRejectAdapter.itemCount == 0)
            binding.tvWithoutRequest.visibility = View.VISIBLE
        else
            binding.tvWithoutRequest.visibility = View.INVISIBLE
    }

    // Obtencion de todos los correos de peticiones de contacto
    private fun getUsers() {
        var listEmailUsersCurrent: List<String> = listOf()

        lifecycleScope.launch(Dispatchers.IO){
            // Consulta de todas las solicitudes de contacto almacenadas en el servidor de Firetore
            listEmailUsersCurrent = dbContacts.getListUsers(requireContext(), "request", (activity as HomeActivity).email)

            // Por cada usuario agregado a la lista de peticiones, se obtiene su informacion de contacto
            withContext(Dispatchers.Main)  {
                if (listEmailUsersCurrent.isNotEmpty()) {
                    listEmailUsers = listEmailUsersCurrent

                    for (user in listEmailUsers) {
                        Log.e("USER", user)
                        getUserData(user)
                    }
                }
                viewListContact()
            }
        }
    }

    // Obtencion de la informacion de cada contacto
    private fun getUserData(email: String) {
        userRejectAdapter = UserRejectAdapter(this, requireContext(), listDataUsers)
        var userCurrent: User?

        lifecycleScope.launch(Dispatchers.IO) {
            // Consulta de los datos de cada contacto almacenados en el servidor de Firetore
            userCurrent = dbContacts.getDataUser(requireContext(), email)

            // Agregacion y actualizacion de lista de peticion de contacto
            withContext(Dispatchers.Main) {
                if (userCurrent != null) {
                    val lastPosition = listDataUsers.size
                    Log.e("USER_ADD", "${userCurrent!!.email} position: $lastPosition")
                    listDataUsers.add(lastPosition, userCurrent!!)
                    userRejectAdapter.notifyItemInserted(lastPosition)
                }
                viewListContact()
            }
        }
    }

    // Cuando se acepta una invitacion se procede con agregar al contacto a una lista de contacto
    // aceptados y de igual forma se debe actualizar la vista actual de contactos agregados
    override fun addUserContact(userAccept: User, position: Int) {
        val emailUserAccept = "email_${replaceFormatEmail(userAccept.email)}"
        val emailUserRequest = "email_${replaceFormatEmail((activity as HomeActivity).email)}"

        lifecycleScope.launch(Dispatchers.IO) {
            // Agregacion de usuarios en listas de agregados de contacto en Firestore
            dbContacts.addRegister("contact", (activity as HomeActivity).email, emailUserAccept, userAccept.email)
            dbContacts.addRegister("contact", userAccept.email, emailUserRequest, (activity as HomeActivity).email)
            // Eliminacion de usuarios en lista de peticiones de contacto en Firestore
            dbContacts.deleteRegister("request", (activity as HomeActivity).email, emailUserAccept)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Usuario ${userAccept.email} agregado", Toast.LENGTH_SHORT).show()
            }
        }

        updateList(position)
    }

    // Cuando se elimina una invitacion de contacto se debe eliminar del archivo de peticiones almacenado
    // en Firestore y de igual forma se debe actualizar la vista actual de peticiones
    override fun rejectUserContact(userReject: User, position: Int) {
        val email = "email_${replaceFormatEmail(userReject.email)}"

        // Eliminacion de usuario en Firestore
        lifecycleScope.launch(Dispatchers.IO) {
            dbContacts.deleteRegister("request", (activity as HomeActivity).email, email)
        }

        updateList(position)
    }

    // Actualizacion de RecyclerView
    private fun updateList(position: Int) {
        listDataUsers.removeAt(position)
        userRejectAdapter.notifyItemRemoved(position)

        viewMessageListContact()
    }

    // Cambio de caracter especial punto (.) por guion bajo (_)
    private fun replaceFormatEmail(email: String): String {
        return email.replace(".", "_")
    }

}