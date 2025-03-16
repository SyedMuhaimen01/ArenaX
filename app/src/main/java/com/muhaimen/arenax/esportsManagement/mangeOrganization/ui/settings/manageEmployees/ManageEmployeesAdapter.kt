package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData

class ManageEmployeesAdapter(
    private var employeesList: MutableList<UserData>,
    private val onRemoveEmployeeClick: (String) -> Unit
) : RecyclerView.Adapter<ManageEmployeesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item, parent, false) // Ensure correct layout reference
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val employee = employeesList[position]
        holder.bind(employee)
    }

    override fun getItemCount(): Int = employeesList.size

    // Function to update the list with data from fetchEmployeesList()
    fun updateEmployeesList(newEmployeesList: List<UserData>) {
        employeesList.clear()
        employeesList.addAll(newEmployeesList)
        notifyDataSetChanged()
    }

    // Function to remove an employee from the list
    fun removeEmployee(userId: String) {
        val indexToRemove = employeesList.indexOfFirst { it.userId == userId }
        if (indexToRemove != -1) {
            employeesList.removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }

    // ViewHolder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val employeeNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val removeEmployeeButton: TextView = itemView.findViewById(R.id.removeAdminButton) // Rename in XML if necessary

        fun bind(employee: UserData) {
            employeeNameTextView.text = employee.fullname
            gamerTagTextView.text = employee.gamerTag.ifEmpty { "N/A" }

            Glide.with(itemView.context)
                .load(employee.profilePicture ?: R.drawable.battlegrounds_icon_background)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            // Handle remove employee button click
            removeEmployeeButton.setOnClickListener {
                onRemoveEmployeeClick(employee.userId)
            }
        }
    }
}
