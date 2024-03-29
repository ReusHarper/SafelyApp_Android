package com.safelyapp.android.view.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentAccountBinding
import com.safelyapp.android.view.activities.HomeActivity
import com.squareup.picasso.Picasso
import java.util.*


class AccountFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private var bundle: Bundle? = null
    private val db = FirebaseFirestore.getInstance()
    private var imgStorageReference = FirebaseStorage.getInstance().reference.child("users/img_profile/")
    private var URI_image: String? = null
    private var dataInput: Boolean = false

    // ========== Multimedia & Peripheral ==========
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Imagen seleccionada
                img_profile?.setImageURI(uri)
                URI_image = uri.toString()
                //Toast.makeText(requireContext(), "img: ${URI_image}", Toast.LENGTH_SHORT).show()
                uploadImageToFirebase(uri)
            } else {
                // Imagen no encontrada
                Toast.makeText(
                    requireContext(),
                    "No se selecciono alguna imagen, por favor intente de nuevo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // ========== Elements ==========
    private lateinit var txt_name: TextInputEditText
    private lateinit var txt_direction: TextInputEditText
    private lateinit var txt_phone: TextInputEditText
    private lateinit var btn_edit: Button
    private lateinit var btn_cancel: Button
    private lateinit var btn_camera: Button
    private var url_img_profile: String? = null
    private var img_profile: ImageView? = null

    // ========== Ciclo de vida ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        bundle = savedInstanceState

        // Asignacion de elementos mediante binding
        txt_name = binding.tietName
        txt_direction = binding.tietDirection
        txt_phone = binding.tietPhone
        img_profile = binding.imgProfile
        btn_edit = binding.btnEdit
        btn_cancel = binding.btnCancel
        btn_camera = binding.btnCamera

        // Obtencion de datos del servidor de Firestore
        getData()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Observador de acciones de usuario
        observer()
    }

    // ========== Metodos propios ==========
    // Observador de acciones de usuario
    private fun observer() {
        getMultimedia()
        editInformation()
        cancel()
    }

    // Obtencion de contenido multimedia (imagen o fotografia)
    private fun getMultimedia() {
        btn_camera.setOnClickListener {
            showDialog()
        }
    }

    // Captura de fotografia
    private fun getPhoto() {
        //val intent = Intent(requireContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    // Subida de imagen al servidor de Firestore
    private fun uploadImageToFirebase(fileUri: Uri) {
        if (fileUri != null) {
            val fileName = UUID.randomUUID().toString() +".jpg"
            val storageReference = FirebaseStorage.getInstance().reference.child("images/$fileName")

            storageReference.putFile(fileUri).addOnSuccessListener(
                OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        db.collection("users").document((activity as HomeActivity).email).set(
                            mapOf("img_profile" to imageUrl), SetOptions.merge()
                        )
                    }
                })
                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                })
        }
    }

    // Edicion y confirmacion de cambios de los datos del usuario retornados hacia el servidor de Firestore
    private fun editInformation() {
        btn_edit.setOnClickListener {
            dataSetOrUpdateServer()
            Toast.makeText(requireContext(), "Información editada con éxito", Toast.LENGTH_SHORT).show()
            returnHome()
        }
    }

    // Consulta de informacion del usuario desde el servidor de Firestore
    private fun getData() {
        db.collection("users").document((activity as HomeActivity).email).get()
            .addOnSuccessListener { value ->
                txt_name.setText(value.get("name") as? String?:"")
                txt_direction.setText(value.get("address") as? String?:"")
                txt_phone.setText(value.get("phone") as? String?:"")
                setImageProfile(value.get("img_profile") as String?)
            }
    }

    private fun setData() {
        db.collection("users").document((activity as HomeActivity).email).set(
            mapOf(
                "email" to (activity as HomeActivity).email,
                "provider" to (activity as HomeActivity).providerType,
                "name" to txt_name.text.toString(),
                "address" to txt_direction.text.toString(),
                "phone" to txt_phone.text.toString()
            ), SetOptions.merge()
        )
    }

    private fun updateData() {
        db.collection("users").document((activity as HomeActivity).email).update(
            mapOf(
                "provider" to (activity as HomeActivity).providerType,
                "name" to txt_name.text.toString(),
                "address" to txt_direction.text.toString(),
                "phone" to txt_phone.text.toString()
            )
        )
    }

    // Comprobacion de informacion almacenada en el dispositivo
    private fun dataSetOrUpdateServer() {
        if (isEmptyDataLocal())
            setData()
        else
            updateData()
    }

    private fun cancel() {
        btn_cancel.setOnClickListener {
            returnHome()
        }
    }

    // Regreso a fragmento de inicio (MapsFragment)
    private fun returnHome() {
        parentFragmentManager.popBackStack()
        (activity as HomeActivity).nav_menu_bottom.visibility = View.VISIBLE
    }

    private fun setImageProfile(url: String?) {
        if (url != null)
            Picasso.get().load(url).into(img_profile)
    }

    // Generacion de menu emergente
    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet)

        val btn_gallery: Button = dialog.findViewById(R.id.btn_layout_gallery)
        val btt_camera: Button = dialog.findViewById(R.id.btn_layout_camera)

        // Obtencion de imagen desde la galeria del dispositivo
        btn_gallery.setOnClickListener {
            dialog.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Obtencion de fotografia empleando para ello la camara frontal del dispositivo
        btt_camera.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Foto", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
        dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation
        dialog.getWindow()?.setGravity(Gravity.BOTTOM)
    }

    // Comprobacion de imagen almacenada en el servidor de Firestore
    private fun isEmptyDataLocal() : Boolean{
        // Si todos los campos estan vacios entonces el usuario no ha ingresado nada de informacion
        if ((txt_name.text!!.isEmpty() && txt_direction.text!!.isEmpty() && txt_phone.text!!.isEmpty()) || !dataInput){
            dataInput = true
            return true
        }
        return false
    }

}