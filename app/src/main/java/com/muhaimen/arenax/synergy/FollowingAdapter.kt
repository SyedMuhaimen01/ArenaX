package com.muhaimen.arenax.synergy

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

class FollowingAdapter(
    private val profiles: List<UserData>,
    private val onMessageClick: (UserData) -> Unit,
    private val onRemoveClick: (UserData) -> Unit) :
    RecyclerView.Adapter<FollowingAdapter.UserDataViewHolder>() {

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
            if(profile.rank == 0) gamerRankTextView.text = "Rank: Unranked"
            else gamerRankTextView.text = "Rank: ${profile.rank}"

            // Load profile picture with Glide
            Glide.with(itemView.context)
                .load(profile.profilePicture ?: R.drawable.game_icon_foreground) // Use default if null
                .placeholder(R.drawable.game_icon_foreground)
                .error(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(profilePicture)
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
}

