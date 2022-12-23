
package com.safelyapp.android.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentListAddBinding

class ListAddFragment : Fragment() {

    private var _binding: FragmentListAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListAddBinding.inflate(inflater, container, false)
        return binding.root
    }

}