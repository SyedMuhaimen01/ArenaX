package com.muhaimen.arenax.userProfile.otherUserEsportsProfile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myTeams.teamDetails


class otherUserTeamsAdapter(private var teams: MutableList<Team>) :
    RecyclerView.Adapter<otherUserTeamsAdapter.MyTeamsViewHolder>() {

    class MyTeamsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val teamName: TextView = view.findViewById(R.id.companyName)
        val gameName: TextView = view.findViewById(R.id.organizationName)
        val teamLogo: ImageView = view.findViewById(R.id.company_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTeamsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.organization_item, parent, false)
        return MyTeamsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyTeamsViewHolder, position: Int) {
        val team = teams[position]

        // Set team name
        holder.teamName.text = team.teamName

        // Set game name
        holder.gameName.text = team.gameName

        // Load logo (if available)
        Glide.with(holder.itemView.context)
            .load(team.teamLogo)
            .placeholder(R.drawable.add_icon_foreground)
            .error(R.drawable.add_icon_foreground)
            .into(holder.teamLogo)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, teamDetails::class.java)
            intent.putExtra("teamName", team.teamName)
            intent.putExtra("gameName", team.gameName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = teams.size

    // Update the list efficiently
    fun updateData(newTeams: List<Team>) {
        teams.clear()
        teams.addAll(newTeams)
        notifyDataSetChanged()
    }
}