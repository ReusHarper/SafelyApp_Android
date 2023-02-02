
package com.safelyapp.android.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.safelyapp.android.databinding.FragmentListAddBinding
import com.safelyapp.android.view.Database.DbContacts
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ListAddFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentListAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User
    private val db = FirebaseFirestore.getInstance()
    private val dbContacts = DbContacts()

    // ========== Elements ==========
    private lateinit var txt_email: TextInputEditText
    private lateinit var btn_send: Button
    private lateinit var btn_cancel: Button

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
            erraseEmail()
        }
    }

    // Obtencion de datos con corrutinas
    private fun getUserDataContact() {

        // Si hay un texto agregado en la barra de busqueda de emails se procede con su busqueda
        if (txt_email.text != null && txt_email.text!!.isNotEmpty()) {
            var userCurrent: User?

            lifecycleScope.launch(Dispatchers.IO) {
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
                        Toast.makeText(
                            requireContext(),
                            "Error de respuesta desde el servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .await().toObject(User::class.java)

                withContext(Dispatchers.Main) {
                    if (userCurrent != null) {
                        user = userCurrent!!
                        sendRequestContact()
                        Toast.makeText(
                            requireContext(),
                            "Invitación enviada con éxito",
                            Toast.LENGTH_SHORT
                        ).show()
                        txt_email.setText("")
                    } else {
                        user = User("Unkown", "Unkown")
                        Toast.makeText(
                            requireContext(),
                            "El usuario ingresado no existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Por favor ingrese un email.", Toast.LENGTH_SHORT).show()
        }
    }

    // Envio de invitacion
    private fun sendRequestContact() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Envio de invitacion al usuario especificado mediate su correo
            if (!user.email.equals("Unkown")) {
                val emailWithCorrectFormat = replaceFormatEmail((activity as HomeActivity).email)

                lifecycleScope.launch(Dispatchers.IO) {
                    dbContacts.addRegister("request", user.email , "email_$emailWithCorrectFormat", (activity as HomeActivity).email)
                    dbContacts.addRegister("notifications", user.email , "request_$emailWithCorrectFormat", (activity as HomeActivity).email)
                }
            }
        }
    }

    // Borrado de texto
    private fun erraseEmail() {
        txt_email.setText("")
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(txt_email.windowToken, 0)
    }

    // Cambio de caracter especial punto (.) por guion bajo (_)
    private fun replaceFormatEmail(email: String): String {
        return email.replace(".", "_")
    }

}