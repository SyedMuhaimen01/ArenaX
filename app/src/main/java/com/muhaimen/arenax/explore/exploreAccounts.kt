package com.muhaimen.arenax.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class exploreAccounts : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_accounts, container, false)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.accounts_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up the adapter with dummy data
        val dummyProfiles = generateDummyProfiles()
        val adapter = exploreAccountsAdapter(dummyProfiles)
        recyclerView.adapter = adapter

        return view
    }
    fun generateDummyProfiles(): List<UserProfile> {
        return listOf(
            UserProfile("Alex Smith", "AceWarrior", "Rank: 10", "https://example.com/profile1.jpg"),
            UserProfile("Jordan Lee", "SniperPro", "Rank: 20", "https://example.com/profile2.jpg"),
            UserProfile("Mia Brown", "SpeedDemon", "Rank: 15", "https://example.com/profile3.jpg"),
            UserProfile("Liam Wilson", "StealthNinja", "Rank: 5", "https://example.com/profile4.jpg"),
            UserProfile("Sophia Turner", "BladeMaster", "Rank: 7", "https://example.com/profile5.jpg"),
            UserProfile("Ethan White", "RogueKnight", "Rank: 3", "https://example.com/profile6.jpg")
        )
    }
}
