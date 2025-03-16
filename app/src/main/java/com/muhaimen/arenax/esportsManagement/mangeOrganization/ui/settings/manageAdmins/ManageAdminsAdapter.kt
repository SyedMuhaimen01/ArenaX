package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData

class ManageAdminsAdapter(
    private var adminsList: MutableList<UserData>,
    private val onRemoveAdminClick: (String) -> Unit
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

    // Function to update the list with data from fetchAdminsList()
    fun updateAdminsList(newAdminsList: List<UserData>) {
        adminsList.clear()
        adminsList.addAll(newAdminsList)
        notifyDataSetChanged()
    }

    // Function to remove an admin from the list
    fun removeAdmin(userId: String) {
        val indexToRemove = adminsList.indexOfFirst { it.userId == userId }
        if (indexToRemove != -1) {
            adminsList.removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }

    // ViewHolder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val removeAdminButton: TextView = itemView.findViewById(R.id.removeAdminButton)

        fun bind(admin: UserData) {
            adminNameTextView.text = admin.fullname
            gamerTagTextView.text = admin.gamerTag ?: "N/A"

            val profileUrl = admin.profilePicture?.takeIf { it.isNotEmpty() }
                ?: R.drawable.battlegrounds_icon_background

            Glide.with(itemView.context)
                .load(profileUrl)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            // Handle remove admin button click
            removeAdminButton.setOnClickListener {
                onRemoveAdminClick(admin.userId)
            }
        }
    }
}
