package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageEmployees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.organizationEmployee

class ManageEmployeesAdapter(
    private var employeesList: List<organizationEmployee>
) : RecyclerView.Adapter<ManageEmployeesAdapter.SearchViewHolder>() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("userData") // Reference to userData node

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_item, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val admin = employeesList[position]
        holder.bind(admin)
    }

    override fun getItemCount(): Int = employeesList.size

    // ViewHolder class
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val employeeNameTextView: TextView = itemView.findViewById(R.id.adminNameTextView)

        fun bind(admin: organizationEmployee) {
            val employeeId = admin.employeeId

            // Query Firebase to get the full name
            databaseReference.child(employeeId).child("fullname").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val fullName = snapshot.getValue(String::class.java) ?: "Unknown"
                        employeeNameTextView.text = fullName
                    } else {
                        employeeNameTextView.text = "User not found"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    employeeNameTextView.text = "Error loading"
                }
            })
        }
    }
}
