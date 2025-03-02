package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.organizationAdmins

class SearchAdminsAdapter(
    private var adminsList: List<organizationAdmins>
) : RecyclerView.Adapter<SearchAdminsAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_admin_card, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val admin = adminsList[position]
        holder.bind(admin)
    }

    override fun getItemCount(): Int = adminsList.size

    // ViewHolder class
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.adminNameTextView)

        fun bind(admin: organizationAdmins) {
            adminNameTextView.text = admin.adminName
        }
    }
}