package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData


class SearchPlayerAdapter(
    private var playersList: List<UserData>,
    private val onAddPlayerClick: (UserData) -> Unit
) : RecyclerView.Adapter<SearchPlayerAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_admin_card, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val team = playersList[position]
        holder.bind(team)
    }

    override fun getItemCount(): Int = playersList.size

    // ViewHolder class
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val addPlayerButton: TextView = itemView.findViewById(R.id.addAdminButton)

        fun bind(admin: UserData) {
            adminNameTextView.text = admin.fullname
            gamerTagTextView.text = admin.gamerTag

            // Load profile picture using Glide (handles URLs and placeholders)
            Glide.with(itemView.context)
                .load(admin.profilePicture ?: R.drawable.battlegrounds_icon_background) // Use a default profile picture if null
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)
            addPlayerButton.setOnClickListener {
                onAddPlayerClick(admin)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlayersList(newUserList: List<UserData>) {
        playersList = newUserList
        notifyDataSetChanged()
    }
}
