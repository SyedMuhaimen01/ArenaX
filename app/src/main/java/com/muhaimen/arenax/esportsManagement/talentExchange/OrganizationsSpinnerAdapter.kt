package com.muhaimen.arenax.esportsManagement.talentExchange

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData

class OrganizationSpinnerAdapter(
    private val context: Context,
    private val organizationList: List<OrganizationData>
) : BaseAdapter() {

    override fun getCount(): Int {
        return organizationList.size
    }

    override fun getItem(position: Int): OrganizationData {
        return organizationList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the custom layout for the Spinner item
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)

        // Get references to the views in the layout
        val orgLogo = view.findViewById<ImageView>(R.id.orgLogo)
        val orgName = view.findViewById<TextView>(R.id.orgName)

        // Get the current organization
        val organization = organizationList[position]

        // Set the organization name
        orgName.text = organization.organizationName

        // Load the organization logo using a library like Glide or Picasso
        if (organization.organizationLogo != null && organization.organizationLogo!!.isNotEmpty()) {
            Glide.with(context)
                .load(organization.organizationLogo)
                .circleCrop()
                .placeholder(R.drawable.battlegrounds_icon_background) // Placeholder image
                .error(R.drawable.battlegrounds_icon_background) // Error image
                .into(orgLogo)
        } else {
            orgLogo.setImageResource(R.drawable.battlegrounds_icon_background) // Default image
        }

        return view
    }
}