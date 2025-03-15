package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Teams.viewOwnTeams

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.dataClasses.UserData

class ManagePlayerAdapter(
    private var teamList: List<Team>,
    private val onRemovePlayerClick: (String) -> Unit
) : RecyclerView.Adapter<ManagePlayerAdapter.ViewHolder>() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("userData")
    private val teamMembersData = mutableListOf<UserData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = teamMembersData[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = teamMembersData.size

    fun updateTeamMembers(team: Team) {
        teamMembersData.clear()
        team.teamMembers?.forEach { memberId ->
            databaseRef.child(memberId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserData::class.java)
                    user?.let {
                        teamMembersData.add(it)
                        notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    // ViewHolder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val removeAdminButton: TextView = itemView.findViewById(R.id.removeAdminButton)

        fun bind(user: UserData) {
            adminNameTextView.text = user.fullname
            gamerTagTextView.text = user.gamerTag ?: "N/A"
            Glide.with(itemView.context)
                .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            removeAdminButton.setOnClickListener {
                onRemovePlayerClick(user.userId)
            }
        }
    }
}
