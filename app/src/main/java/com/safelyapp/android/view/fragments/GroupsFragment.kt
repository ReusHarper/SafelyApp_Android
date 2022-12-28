package com.safelyapp.android.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentGroupsBinding
import com.safelyapp.android.view.adapter.TabsGroupsAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.safelyapp.android.view.Database.DbContacts
import com.safelyapp.android.view.data.User

class GroupsFragment : Fragment(R.layout.fragment_groups) {

    // ========== General ==========
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private val dbContacts = DbContacts()

    // ========== Elements ==========
    private val groupsArray = arrayOf(
        "Añadidos",
        "Agregar",
        "Solicitudes"
    )
    private lateinit var viewPager : ViewPager2
    private lateinit var tabLayout : TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // ========== Ciclo de vida ==========
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)

        // Control y manejo de los elementos
        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val adapter = TabsGroupsAdapter(parentFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = groupsArray[position]
        }.attach()
    }

    // ========== Metodos propios ==========
}