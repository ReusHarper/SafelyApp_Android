package com.safelyapp.android.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.safelyapp.android.R
import com.safelyapp.android.view.activities.HomeActivity
import com.safelyapp.android.view.fragments.GroupsFragment
import com.safelyapp.android.view.fragments.ListAddFragment
import com.safelyapp.android.view.fragments.ListContactFragment
import com.safelyapp.android.view.fragments.ListRequestFragment

class TabsGroupsAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    // ========== General ==========
    private val NUM_TABS = 3

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    // Visualizacion de ventana seleccionada
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return ListContactFragment()
            1 -> return ListAddFragment()
        }
        return ListRequestFragment()
    }

}