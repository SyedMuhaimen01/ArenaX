package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams

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
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams.viewOwnTeam

class TeamsAdapter(private val teams: List<Team>) : RecyclerView.Adapter<TeamsAdapter.TeamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.organization_item, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]
        holder.teamName.text = team.teamName // Populate with team name
        holder.gameName.text = team.gameName // Populate with game name

        // Load the team logo into the ImageView using Glide
        Glide.with(holder.itemView.context)
            .load(team.teamLogo)
            .into(holder.logoImageView)

        // Set the onClick listener to navigate to OwnTeamActivity
        holder.cardView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, viewOwnTeam::class.java)
            intent.putExtra("organizationName", team.teamLocation) // Pass the team location or organization name
            intent.putExtra("teamName", team.teamName) // Pass the team name
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return teams.size
    }

    class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val teamName: TextView = itemView.findViewById(R.id.companyName) // Company Name
        val gameName: TextView = itemView.findViewById(R.id.organizationName) // Organization Name (Game Name)
        val logoImageView: ImageView = itemView.findViewById(R.id.company_logo) // Company Logo (Team Logo)
        val cardView: CardView = itemView.findViewById(R.id.analytics_card_view) // CardView
    }
}
