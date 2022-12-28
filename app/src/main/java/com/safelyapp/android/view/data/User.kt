package com.safelyapp.android.view.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val email: String = "",
    val name: String? = ""
) : Parcelable
