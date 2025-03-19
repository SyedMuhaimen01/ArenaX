package com.muhaimen.arenax.esportsManagement.OtherOrganization

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOtherTeam.viewOtherTeam

class otherTeamsAdapter(
    private val teams: List<Team>,
    private val organizationName: String? // Add the organizationName parameter
) : RecyclerView.Adapter<otherTeamsAdapter.TeamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.organization_item, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]
        holder.teamName.text = team.teamName
        holder.gameName.text = team.gameName

        // Load the team logo into the ImageView using Glide
        Glide.with(holder.itemView.context)
            .load(team.teamLogo)
            .into(holder.logoImageView)

        // Set the onClick listener to navigate to OwnTeamActivity
        holder.cardView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, viewOtherTeam::class.java)
            intent.putExtra("organizationName", organizationName) // Pass the organization name
            intent.putExtra("teamName", team.teamName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return teams.size
    }

    class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val teamName: TextView = itemView.findViewById(R.id.companyName) // Team Name
        val gameName: TextView = itemView.findViewById(R.id.organizationName) // Game Name (Organization)
        val logoImageView: ImageView = itemView.findViewById(R.id.company_logo) // Team Logo
        val cardView: CardView = itemView.findViewById(R.id.analytics_card_view) // CardView
    }
}
