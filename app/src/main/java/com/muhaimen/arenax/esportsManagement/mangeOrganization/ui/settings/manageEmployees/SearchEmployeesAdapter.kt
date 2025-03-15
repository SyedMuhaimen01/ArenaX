package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData

class SearchEmployeesAdapter(
    private var employeesList: List<UserData>,
    private val onAddEmployeeClick: (UserData) -> Unit // Callback for adding admin
) : RecyclerView.Adapter<SearchEmployeesAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_admin_card, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val admin = employeesList[position]
        holder.bind(admin)
    }

    override fun getItemCount(): Int = employeesList.size

    // ViewHolder class
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adminNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val addEmployeeButton: TextView = itemView.findViewById(R.id.addAdminButton)

        fun bind(employee: UserData) {
            adminNameTextView.text = employee.fullname
            gamerTagTextView.text = employee.gamerTag

            // Load profile picture using Glide (handles URLs and placeholders)
            Glide.with(itemView.context)
                .load(employee.profilePicture ?: R.drawable.battlegrounds_icon_background) // Use a default profile picture if null
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            // Handle button click to add admin
            addEmployeeButton.setOnClickListener {
                onAddEmployeeClick(employee)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateEmployeesList(newUserList: List<UserData>) {
        employeesList = newUserList
        notifyDataSetChanged() // Refresh the entire list
    }
}
