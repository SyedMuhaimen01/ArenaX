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
    private var playersList: MutableList<UserData>, // Mutable list for easier updating
    private val onAddPlayerClick: (String) -> Unit // Callback with userId as the parameter
) : RecyclerView.Adapter<SearchPlayerAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_admin_card, parent, false) // Correct layout to inflate
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val player = playersList[position]
        holder.bind(player)
    }

    override fun getItemCount(): Int = playersList.size

    // ViewHolder class
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val addPlayerButton: TextView = itemView.findViewById(R.id.addAdminButton)

        fun bind(player: UserData) {
            playerNameTextView.text = player.fullname
            gamerTagTextView.text = player.gamerTag

            // Load profile picture using Glide
            Glide.with(itemView.context)
                .load(player.profilePicture ?: R.drawable.battlegrounds_icon_background) // Default image if null
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            // Set the click listener for the "Add Player" button
            addPlayerButton.setOnClickListener {
                // Pass the userId (or player ID) to the callback function when clicked
                player.userId?.let { userId ->
                    onAddPlayerClick(userId)
                }
            }
        }
    }

    // Function to update the players list
    @SuppressLint("NotifyDataSetChanged")
    fun updatePlayersList(newUserList: List<UserData>) {
        playersList.clear()
        playersList.addAll(newUserList)
        notifyDataSetChanged()
    }
}
