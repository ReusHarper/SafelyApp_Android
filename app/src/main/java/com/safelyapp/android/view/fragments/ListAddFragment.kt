
package com.safelyapp.android.view.fragments

import android.app.DownloadManager.Request
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.safelyapp.android.databinding.FragmentListAddBinding
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ListAddFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentListAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User

    // ========== Elements ==========
    private lateinit var txt_email: TextInputEditText
    private lateinit var btn_send: Button
    private lateinit var btn_cancel: Button
    private val db = FirebaseFirestore.getInstance()

    // ========== Ciclo de vida ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListAddBinding.inflate(inflater, container, false)

        // Asignacion de elementos mediante binding
        txt_email = binding.tietEmail
        btn_send = binding.btnSend
        btn_cancel = binding.btnCancel

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Observador de acciones de usuario
        observer()
    }

    // ========== Metodos propios ==========
    // Observador de acciones de usuario
    private fun observer(){
        btn_send.setOnClickListener {
            getUserDataContact()
        }

        btn_cancel.setOnClickListener {
            returnHome()
        }
    }

    // Obtencion de datos con corrutinas
    private fun getUserDataContact() {
        var userCurrent: User?

        lifecycleScope.launch(Dispatchers.IO){
            // Consulta de usuario en el servidor de firestore, mientras se emplee el metodo await
            // el objeto userCurrent no sera inicializado y por lo tanto no se continuara con el
            // siguiente bloque de codigo
            userCurrent = db.collection("users").document(txt_email.text.toString()).get()
                .addOnSuccessListener { value ->
                    if (value != null) {
                        (value.get("email") as? String?)
                        (value.get("name") as? String?)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error de respuesta desde el servidor", Toast.LENGTH_SHORT).show()
                }
                .await().toObject(User::class.java)

            withContext(Dispatchers.Main)  {
                if (userCurrent != null) {
                    user = userCurrent!!
                    sendResquestContact()
                    Toast.makeText(requireContext(), "Invitación enviada con éxito", Toast.LENGTH_SHORT).show()
                }
                else {
                    user = User("Unkown", "Unkown")
                    Toast.makeText(requireContext(), "El usuario ingresado no existe", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Envio de invitacion
    private fun sendResquestContact() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Envio de invitacion al usuario especificado mediate su correo
            if (!user.email.equals("Unkown")) {
                db.collection("request").document(user.email)
                    .set(
                        mapOf(
                            "email_${(activity as HomeActivity).email}" to (activity as HomeActivity).email
                        ), SetOptions.merge()
                    )
            }
        }
    }

    // Regreso a fragmento de inicio (MapsFragment)
    private fun returnHome() {
        parentFragmentManager.popBackStack()
        (activity as HomeActivity).nav_menu_bottom.visibility = View.VISIBLE
    }

}