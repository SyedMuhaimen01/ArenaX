package com.muhaimen.arenax.esportsManagement.exploreEsports

import com.muhaimen.arenax.dataClasses.OrganizationData
import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.OtherOrganization.OtherOrganization
import com.muhaimen.arenax.utils.FirebaseManager

data class FrequentSearchedOrganization(
    val organizationName: String = "Unknown Organization",
    val organizationLogo: String = "",
    val organizationLocation: String = "",
    val organizationType: String = ""
)

class SearchOrganizationsAdapter(
    private var organizationList: List<OrganizationData>
) : RecyclerView.Adapter<SearchOrganizationsAdapter.SearchViewHolder>() {

    private val organizationCache = mutableMapOf<String, FrequentSearchedOrganization>()

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profilePicture)
        val nameTextView: TextView = view.findViewById(R.id.fullname)
        val locationTextView: TextView = view.findViewById(R.id.gamerTag)
        val typeTextView: TextView = view.findViewById(R.id.gamerRank)

        init {
            itemView.setOnClickListener {
                // Get the current user data based on the position
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val organization = organizationList[position]
                    // Start the ViewGameAnalytics activity
                    val intent = Intent(itemView.context, OtherOrganization::class.java).apply {
                        putExtra("organizationId", organization.organizationId)
                        putExtra("organization_name", organization.organizationName)
                    }
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.explore_accounts_card, parent, false)
        return SearchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val organizationItem = organizationList[position]

        // Check cache first
        val cachedOrganization= organizationCache[organizationItem.organizationId]
        if (cachedOrganization != null) {
            bindOrganizationData(holder, cachedOrganization)
        } else {
            // Fetch and cache the user data if not available in the cache
            fetchAndCacheOrganizationData(organizationItem.organizationId) { frequentUser ->
                bindOrganizationData(holder, frequentUser)
            }
        }
    }

    private fun fetchAndCacheOrganizationData(userId: String, callback: (FrequentSearchedOrganization) -> Unit) {
        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("organizationsData").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val name = dataSnapshot.child("organizationName").value?.toString() ?: "Unknown Organization"
            val location = dataSnapshot.child("organizationLocation").value?.toString() ?: ""
            val type= dataSnapshot.child("organizationType").value?.toString() ?: ""
            val logo = dataSnapshot.child("organizationLogo").value?.toString() ?: ""

            // Create a new FrequentSearchedUser instance and cache it
            val frequentOrganization = FrequentSearchedOrganization(name, logo, location, type)
            organizationCache[userId] = frequentOrganization

            // Trigger the callback with the fetched data
            callback(frequentOrganization)
        }.addOnFailureListener {
            // Use default FrequentSearchedUser in case of an error
            callback(FrequentSearchedOrganization())
        }
    }

    private fun bindOrganizationData(holder: SearchViewHolder, organization: FrequentSearchedOrganization) {
        holder.nameTextView.text = organization.organizationName
        holder.locationTextView.text = organization.organizationLocation
        holder.typeTextView.text = organization.organizationType
        Glide.with(holder.profileImage.context)
            .load(organization.organizationLogo)
            .placeholder(R.drawable.game_icon_foreground)
            .error(R.drawable.game_icon_foreground)
            .circleCrop()
            .into(holder.profileImage)
    }

    override fun getItemCount(): Int = organizationList.size

    // Update the user list, retaining cached users
    @SuppressLint("NotifyDataSetChanged")
    fun updateOrganizationsList(newOrganizationList: List<OrganizationData>) {
        val newOrganizationsToFetch = newOrganizationList.filter { !organizationCache.containsKey(it.organizationId) }
        organizationList = newOrganizationList

        // Fetch data only for new users not in cache
        newOrganizationsToFetch.forEach { organization ->
            fetchAndCacheOrganizationData(organization.organizationId) { _ ->
                notifyDataSetChanged() // Update view once all data is fetched
            }
        }
        notifyDataSetChanged() // Refresh the whole list to apply changes
    }
}
