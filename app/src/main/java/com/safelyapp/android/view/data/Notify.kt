package com.safelyapp.android.view.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notify(
    val title: String = "",
    val message: String? = ""
) : Parcelable
