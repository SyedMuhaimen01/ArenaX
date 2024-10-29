package com.muhaimen.arenax.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R

data class UserProfile(
    val fullName: String,
    val gamerTag: String,
    val gamerRank: String,
    val profilePictureUrl: String
)
class exploreAccountsAdapter(private val profiles: List<UserProfile>) :
    RecyclerView.Adapter<exploreAccountsAdapter.UserProfileViewHolder>() {

    inner class UserProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val gamerRankTextView: TextView = itemView.findViewById(R.id.gamerRank)

        fun bind(profile: UserProfile) {
            fullNameTextView.text = profile.fullName
            gamerTagTextView.text = profile.gamerTag
            gamerRankTextView.text = profile.gamerRank

            // Load profile picture with Glide
            Glide.with(itemView.context)
                .load(profile.profilePictureUrl)
                .placeholder(R.drawable.game_icon_foreground) // Optional placeholder image
                .error(R.drawable.game_icon_foreground)       // Error image
                .circleCrop()                                  // To apply circle cropping
                .into(profilePicture)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.explore_accounts_card, parent, false)
        return UserProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int = profiles.size
}