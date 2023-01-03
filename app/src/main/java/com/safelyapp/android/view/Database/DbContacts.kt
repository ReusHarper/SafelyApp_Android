package com.safelyapp.android.view.Database

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.safelyapp.android.view.data.User
import kotlinx.coroutines.tasks.await

open class DbContacts {

    private val db = FirebaseFirestore.getInstance()

    // ========== Metodos propios ==========
    suspend fun setUserInitialData(email: String) {
        db.collection("users").document(email).set(
            mapOf(
                "email" to email,
                "provider" to "",
                "name" to "",
                "address" to "",
                "phone" to ""
            ), SetOptions.merge()
        ).await()
    }

    suspend fun addRegister(path: String, document: String, key: String, value: String) {
        db.collection(path).document(document).set(mapOf(key to value), SetOptions.merge()).await()
    }

    // Envio de localizacion actual del usuario hacia el contacto especificado mediante su email como notificacion en Firetore
    suspend fun addLocationRegister(emailOwner: String, emailContact: String, field: String, location: Location) {

        val geopoint = GeoPoint(location.latitude, location.longitude)
        val elements = listOf<Any>(emailOwner, geopoint)
        val data = hashMapOf(field to elements)

        db.collection("notifications").document(emailContact).set(data, SetOptions.merge()).await()
    }

    suspend fun deleteRegister(path: String, document: String, key: String) {
        // Se establece la eliminacion del campo especificado en un Hash mediante una actualizacion
        val updates = hashMapOf<String, Any>(
            key to FieldValue.delete()
        )

        db.collection(path).document(document).update(updates).await()
    }

    suspend fun getDataUser(context: Context, email: String): User? {
        val user = db.collection("users").document(email).get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    (value.get("email") as? String?)
                    (value.get("name") as? String?)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error de respuesta desde el servidor", Toast.LENGTH_SHORT).show()
            }
            .await().toObject(User::class.java)

        return user
    }

    suspend fun getListUsers(context: Context, path: String, document: String): List<String>{
        var listCurrent: MutableList<String> = mutableListOf()

        db.collection(path).document(document).get()
            .addOnSuccessListener { listItems ->
                if (listItems.data != null)
                    listCurrent = listItems.data!!.map  { it.value as String }.toMutableList()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error de respuesta desde el servidor", Toast.LENGTH_SHORT).show()
            }
            .await()

        return listCurrent.toList()
    }
}