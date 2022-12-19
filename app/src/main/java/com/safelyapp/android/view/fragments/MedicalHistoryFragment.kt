package com.safelyapp.android.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentMedicalHistoryBinding

class MedicalHistoryFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentMedicalHistoryBinding? = null
    private val binding get() = _binding!!
    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicalHistoryBinding.inflate(inflater, container, false)
        bundle = savedInstanceState

        return binding.root
    }

}