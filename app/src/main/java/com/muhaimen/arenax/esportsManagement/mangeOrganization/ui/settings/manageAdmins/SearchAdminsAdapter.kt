package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData

class SearchAdminsAdapter(
    private var adminsList: MutableList<UserData>,
    private val onAddAdminClick: (String) -> Unit // Pass only userId
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

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val addAdminButton: TextView = itemView.findViewById(R.id.addAdminButton)

        fun bind(admin: UserData) {
            adminNameTextView.text = admin.fullname
            gamerTagTextView.text = admin.gamerTag

            Glide.with(itemView.context)
                .load(admin.profilePicture ?: R.drawable.battlegrounds_icon_background)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            addAdminButton.setOnClickListener {
                Log.d("SearchAdminsAdapter", "Button clicked for userId: ${admin.userId}")
                admin.userId?.let { userId ->
                    onAddAdminClick(userId)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdminsList(newUserList: List<UserData>) {
        adminsList.clear()
        adminsList.addAll(newUserList)
        notifyDataSetChanged()
    }
}
