package com.safelyapp.android.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.safelyapp.android.databinding.FragmentListRequestBinding
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.adapter.UserAdapter
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ListRequestFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentListRequestBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var listEmailUsers: List<String> = listOf()
    private var listDataUsers: MutableList<User> = mutableListOf()
    private lateinit var user: User
    private lateinit var listRequestContact: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

    // Obtencion de todos los correos de peticiones de contacto
    private fun getUsers() {
        var listEmailUsersCurrent: List<String> = listOf()

        lifecycleScope.launch(Dispatchers.IO){
            // Consulta de todas las solicitudes de contacto almacenadas en el servidor de Firetore
            db.collection("request").document((activity as HomeActivity).email).get()
                .addOnSuccessListener { listEmails ->
                    listEmailUsersCurrent = listEmails.data!!.map { it.value as String }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error de respuesta desde el servidor", Toast.LENGTH_SHORT).show()
                }
                .await()

            withContext(Dispatchers.Main)  {
                if (listEmailUsersCurrent.isNotEmpty()) {
                    listEmailUsers = listEmailUsersCurrent

                   for (user in listEmailUsers)
                        getUserData(user)
                }
                else {
                    Toast.makeText(requireContext(), "Lista vacia", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    // Obtencion de la informacion de cada contacto
    private fun getUserData(email: String) {
        var userCurrent: User?

        lifecycleScope.launch(Dispatchers.IO) {

            // Consulta de los datos de cada contacto almacenados en el servidor de Firetore
            userCurrent = db.collection("users").document(email).get()
                .addOnSuccessListener { value ->
                    (value.get("email") as? String?)
                    (value.get("name") as? String?)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error de respuesta desde el servidor", Toast.LENGTH_SHORT).show()
                }
                .await().toObject(User::class.java)

            withContext(Dispatchers.Main) {
                if (userCurrent != null)
                    listDataUsers.add(userCurrent!!)
                viewListContact()
            }
        }
    }

    private fun viewListContact() {
        val userAdapter = UserAdapter(requireContext(), listDataUsers)

        // Muestreo de lista de contacto
        listRequestContact.layoutManager = LinearLayoutManager(requireContext())
        listRequestContact.adapter = userAdapter

        // Se muestra un mensaje en caso de que la lista de contactos se encuentre vacia
        /*if(listRequestContact.size == 0)
            binding.tvWithoutContacts.visibility = View.VISIBLE
        else
            binding.tvWithoutContacts.visibility = View.INVISIBLE*/
    }

}