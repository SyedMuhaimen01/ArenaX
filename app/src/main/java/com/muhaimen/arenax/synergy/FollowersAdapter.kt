package com.muhaimen.arenax.synergy

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.userProfile.otherUserProfile

class FollowersAdapter(
    private var profiles: List<UserData>, // Change to a mutable list
    private val onMessageClick: (UserData) -> Unit,
    private val onRemoveClick: (UserData) -> Unit
) : RecyclerView.Adapter<FollowersAdapter.UserDataViewHolder>() {

    // ViewHolder class for binding individual user data
    inner class UserDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val gamerRankTextView: TextView = itemView.findViewById(R.id.gamerRank)
        val messageButton: Button = itemView.findViewById(R.id.messageButton)
        val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(profile: UserData) {
            fullNameTextView.text = profile.fullname
            gamerTagTextView.text = profile.gamerTag
            gamerRankTextView.text = if (profile.rank == "Unranked") "Rank: Unranked" else "Rank: ${profile.rank}"

            // Load profile picture with Glide, using default if null
            Glide.with(itemView.context)
                .load(profile.profilePicture ?: R.drawable.game_icon_foreground)
                .placeholder(R.drawable.game_icon_foreground)
                .error(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(profilePicture)

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, otherUserProfile::class.java)
                intent.putExtra("userId", profile.userId) // Pass the firebaseUid
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.synergy_card, parent, false)
        return UserDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserDataViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)

        holder.messageButton.setOnClickListener {
            onMessageClick(profile)
        }

        holder.removeButton.setOnClickListener {
            onRemoveClick(profile)
        }
    }

    override fun getItemCount(): Int = profiles.size

    // Method to update the profiles list and notify the adapter
    fun updateProfiles(newProfiles: List<UserData>) {
        profiles = newProfiles
        notifyDataSetChanged() // Refresh the adapter with the new list
    }
}
