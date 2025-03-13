package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myOrganizations

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity



class MyOrganizationsAdapter(private var organizations: MutableList<OrganizationData>) :
    RecyclerView.Adapter<MyOrganizationsAdapter.MyOrganizationsViewHolder>() {

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


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, OrganizationHomePageActivity::class.java)
            intent.putExtra("organization_name", organization.organizationName)
            intent.putExtra("Source", "MyOrganizationsAdapter")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = organizations.size

    // Update the list efficiently
    fun updateData(newOrganizations: List<OrganizationData>) {
        organizations.clear()
        organizations.addAll(newOrganizations)
        notifyDataSetChanged()
    }
}
