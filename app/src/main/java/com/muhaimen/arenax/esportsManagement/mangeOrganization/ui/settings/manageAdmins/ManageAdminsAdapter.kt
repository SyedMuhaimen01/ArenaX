package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.organizationAdmins

class ManageAdminsAdapter(
    private var adminsList: List<organizationAdmins>
) : RecyclerView.Adapter<ManageAdminsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val admin = adminsList[position]
        holder.bind(admin)
    }

    override fun getItemCount(): Int = adminsList.size

    // ViewHolder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // FIXED: Changed class name
        private val adminNameTextView: TextView = itemView.findViewById(R.id.adminNameTextView)

        fun bind(admin: organizationAdmins) {
            adminNameTextView.text = admin.adminName
        }
    }
}
