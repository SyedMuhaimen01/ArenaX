package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

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

class SearchEmployeesAdapter(
    private var employeesList: MutableList<UserData>,
    private val onAddEmployeeClick: (String) -> Unit // Pass only userId
) : RecyclerView.Adapter<SearchEmployeesAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_admin_card, parent, false) // Make sure the layout is correct
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val employee = employeesList[position]
        holder.bind(employee)
    }

    override fun getItemCount(): Int = employeesList.size

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val employeeNameTextView: TextView = itemView.findViewById(R.id.fullname)
        private val gamerTagTextView: TextView = itemView.findViewById(R.id.gamerTag)
        private val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        private val addEmployeeButton: TextView = itemView.findViewById(R.id.addAdminButton) // Ensure correct ID

        fun bind(employee: UserData) {
            employeeNameTextView.text = employee.fullname
            gamerTagTextView.text = employee.gamerTag

            Glide.with(itemView.context)
                .load(employee.profilePicture ?: R.drawable.battlegrounds_icon_background)
                .placeholder(R.drawable.battlegrounds_icon_background)
                .error(R.drawable.battlegrounds_icon_background)
                .circleCrop()
                .into(profilePicture)

            addEmployeeButton.setOnClickListener {
                Log.d("SearchEmployeesAdapter", "Button clicked for userId: ${employee.userId}")
                employee.userId?.let { userId ->
                    onAddEmployeeClick(userId)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateEmployeesList(newUserList: List<UserData>) {
        employeesList.clear()
        employeesList.addAll(newUserList)
        notifyDataSetChanged()
    }
}
