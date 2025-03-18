package com.muhaimen.arenax.userProfile.otherUserEsportsProfile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.OtherOrganization.OtherOrganization

class otherUserOrganizationsAdapter(private var organizations: MutableList<OrganizationData>) :
    RecyclerView.Adapter<otherUserOrganizationsAdapter.MyOrganizationsViewHolder>() {

    class MyOrganizationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val organizationName: TextView = view.findViewById(R.id.companyName)
        val organizationLocation: TextView = view.findViewById(R.id.organizationName)
        val organizationLogo: ImageView = view.findViewById(R.id.company_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrganizationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.organization_item, parent, false)
        return MyOrganizationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOrganizationsViewHolder, position: Int) {
        val organization = organizations[position]

        // Set organization name
        holder.organizationName.text = organization.organizationName

        // Set location (hide if empty)
        if (!organization.organizationLocation.isNullOrEmpty()) {
            holder.organizationLocation.text = organization.organizationLocation
            holder.organizationLocation.visibility = View.VISIBLE
        } else {
            holder.organizationLocation.visibility = View.GONE
        }

        // Load logo (if available)
        Glide.with(holder.itemView.context)
            .load(organization.organizationLogo)
            .placeholder(R.drawable.add_icon_foreground) // Placeholder image
            .error(R.drawable.add_icon_foreground) // Error image
            .into(holder.organizationLogo)

        // Set click listener to open organization details
        holder.itemView.setOnClickListener {
            getOrganizationDetails(organization, holder) // Pass the holder to the function
        }
    }

    override fun getItemCount(): Int = organizations.size

    // Update the list efficiently
    fun updateData(newOrganizations: List<OrganizationData>) {
        organizations.clear()
        organizations.addAll(newOrganizations)
        notifyDataSetChanged()
    }

    private fun getOrganizationDetails(organization: OrganizationData, holder: MyOrganizationsViewHolder) {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organization.organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Fetch notifications for this organization
                        if (!orgId.isNullOrEmpty()) {
                            val intent = Intent(holder.itemView.context, OtherOrganization::class.java)
                            intent.putExtra("organization_name", organization.organizationName)
                            intent.putExtra("organizationId", orgId) // Fixed typo here
                            intent.putExtra("Source", "MyOrganizationsAdapter")
                            holder.itemView.context.startActivity(intent)
                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: ${organization.organizationName}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
    }
}