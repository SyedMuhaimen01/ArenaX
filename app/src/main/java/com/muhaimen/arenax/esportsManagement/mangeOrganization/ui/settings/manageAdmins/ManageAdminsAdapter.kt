package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.organizationAdmins
import com.muhaimen.arenax.dataClasses.UserData

class ManageAdminsAdapter(
    private var adminsList: List<organizationAdmins>,
    private val onRemoveAdminClick: (String) -> Unit
) : RecyclerView.Adapter<ManageAdminsAdapter.ViewHolder>() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("userData")

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
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val removeAdminButton: TextView = itemView.findViewById(R.id.removeAdminButton)

        fun bind(admin: organizationAdmins) {
            adminNameTextView.text = admin.adminName

            // Fetch user details from Firebase
            databaseRef.child(admin.adminId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserData::class.java)
                    if (user != null) {
                        gamerTagTextView.text = user.gamerTag ?: "N/A"
                        Glide.with(itemView.context)
                            .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                            .placeholder(R.drawable.battlegrounds_icon_background)
                            .error(R.drawable.battlegrounds_icon_background)
                            .circleCrop()
                            .into(profilePicture)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary
                }
            })

            // Handle remove admin button click
            removeAdminButton.setOnClickListener {
                onRemoveAdminClick(admin.adminId)
            }
        }
    }
}
