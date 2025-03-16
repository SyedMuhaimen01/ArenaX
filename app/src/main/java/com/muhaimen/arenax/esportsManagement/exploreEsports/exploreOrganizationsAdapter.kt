package com.muhaimen.arenax.esportsManagement.exploreEsports

import android.content.Intent
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
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.userProfile.otherUserProfile

class exploreOrganizationsAdapter (private val profiles: List<OrganizationData>):
    RecyclerView.Adapter<exploreOrganizationsAdapter.OrganizationDataViewHolder>() {
    inner class OrganizationDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val locationTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val gamerRankTextView: TextView = itemView.findViewById(R.id.gamerRank)
        private val layoutCard: LinearLayout = itemView.findViewById(R.id.layoutCard)

        fun bind(profile: OrganizationData) {
            fullNameTextView.text = profile.organizationName
            locationTextView.text = profile.organizationLocation
            gamerRankTextView.text = profile.organizationType

            // Load profile picture with Glide
            Glide.with(itemView.context)
                .load(profile.organizationLogo ?: R.drawable.game_icon_foreground) // Use default if null
                .placeholder(R.drawable.game_icon_foreground)
                .error(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(profilePicture)

            layoutCard.setOnClickListener {
                Log.d("exploreOrganizationsAdapter", "Card clicked for organizationId: ${profile.organizationId}")
                val intent = Intent(itemView.context, otherUserProfile::class.java)
                intent.putExtra("organizationId", profile.organizationId)
                intent.putExtra("organization_name", profile.organizationName)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizationDataViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.explore_accounts_card, parent, false)
        return OrganizationDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganizationDataViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int = profiles.size
}