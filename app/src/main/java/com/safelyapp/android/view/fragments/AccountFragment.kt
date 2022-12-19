package com.safelyapp.android.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.safelyapp.android.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        bundle = savedInstanceState

        return binding.root
    }

}