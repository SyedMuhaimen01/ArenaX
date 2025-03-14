package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.muhaimen.arenax.R

class TeamsFragment : Fragment() {
    private lateinit var registerTeamButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve organization name from arguments
        val organizationName = arguments?.getString("organization_name")

        registerTeamButton = view.findViewById(R.id.registerButton)
        registerTeamButton.setOnClickListener {
            val intent = Intent(activity, registerTeam::class.java).apply {
                putExtra("organization_name", organizationName) // Pass organization name
            }
            startActivity(intent)
        }
    }
}
