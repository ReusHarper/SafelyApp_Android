package com.safelyapp.android.view.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.safelyapp.android.databinding.FragmentSignupBinding


class SignupActivity : AppCompatActivity() {

    private lateinit var binding : FragmentSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflacion de vista
        binding = FragmentSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Establecimiento parametros de UI
        binding.titleSignup.typeface = Typeface.createFromAsset(assets, "Fonts/Catamaran-Medium.ttf")

        // Ejemplo de establecimiento de la UI de cada EditText:
        //setUIEditText(binding.inputEmail, "Ingresa tu email", "ic_email_primary", null)
    }

    // Colocacion de parametros iniciales de la UI de los EditText
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUIEditText(editText: EditText, placeholder: String, icon: String?, color: String?) {
        editText.hint = placeholder
        if (!icon!!.isEmpty()){
            print("drawable: " + icon)
            val mDrawable: Drawable? = getDrawableByFileName(this, icon)
            editText.setCompoundDrawables(mDrawable, null, null, null)
        }
    }

    // Obtencion del icono dependiendo del nombre asignado
    fun getDrawableByFileName(context: Context, fileName: String): Drawable? {
        return ContextCompat.getDrawable(context, context.resources.getIdentifier(fileName, "drawable", context.packageName))
    }

    /*
    // Ciclo de vida
    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
     */

}

