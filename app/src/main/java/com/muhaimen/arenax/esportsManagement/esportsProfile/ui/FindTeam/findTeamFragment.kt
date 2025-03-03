package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting.recruitmentAdPosting

class findTeamFragment : Fragment() {
    private lateinit var postAdButton: FloatingActionButton
    companion object {
        fun newInstance() = findTeamFragment()
    }

    private val viewModel: FindTeamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_find_team, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postAdButton = view.findViewById(R.id.postButton)
        postAdButton.setOnClickListener {
            val intent=Intent(context, recruitmentAdPosting::class.java)
            startActivity(intent)
        }
    }
}