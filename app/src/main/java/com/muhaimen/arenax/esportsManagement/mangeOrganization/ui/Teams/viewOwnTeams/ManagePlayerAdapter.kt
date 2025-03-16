package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData

class ManagePlayerAdapter(
    private var teamMembersData: MutableList<UserData>, // List of team members
    private val onRemovePlayerClick: (String) -> Unit // Callback for remove player action
) : RecyclerView.Adapter<ManagePlayerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item, parent, false) // Use the appropriate layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = teamMembersData[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = teamMembersData.size

    // This function updates the list of team members when new data is fetched
    fun updateTeamMembers(newMembersList: List<UserData>) {
        teamMembersData.clear()
        teamMembersData.addAll(newMembersList)
        notifyDataSetChanged() // Refresh the adapter with new data
    }

    // ViewHolder class to bind the player data to the UI
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val removeAdminButton: TextView = itemView.findViewById(R.id.removeAdminButton)

        fun bind(user: UserData) {
            adminNameTextView.text = user.fullname
            gamerTagTextView.text = user.gamerTag ?: "N/A"
            Glide.with(itemView.context)
                .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            // When the remove button is clicked, call the callback function
            removeAdminButton.setOnClickListener {
                onRemovePlayerClick(user.userId)
            }
        }
    }

}
