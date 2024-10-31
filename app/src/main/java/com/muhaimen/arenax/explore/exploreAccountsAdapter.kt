package com.muhaimen.arenax.explore

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.userProfile.otherUserProfile

class exploreAccountsAdapter(private val profiles: List<UserData>) :
    RecyclerView.Adapter<exploreAccountsAdapter.UserDataViewHolder>() {

    inner class UserDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val gamerRankTextView: TextView = itemView.findViewById(R.id.gamerRank)
        private val layoutCard: LinearLayout = itemView.findViewById(R.id.layoutCard)

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

            layoutCard.setOnClickListener {
                Log.d("exploreAccountsAdapter", "Card clicked for userId: ${profile.userId}")
                val intent = Intent(itemView.context, otherUserProfile::class.java)
                intent.putExtra("userId", profile.userId)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.explore_accounts_card, parent, false)
        return UserDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserDataViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int = profiles.size
}
