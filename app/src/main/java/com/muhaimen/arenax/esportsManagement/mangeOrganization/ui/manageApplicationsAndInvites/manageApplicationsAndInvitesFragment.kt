package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageApplicationsAndInvites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.esportsNotificationData

class manageApplicationsAndInvitesFragment : Fragment() {

    private lateinit var applicationsRecyclerView: RecyclerView
    private lateinit var invitesRecyclerView: RecyclerView
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private lateinit var invitesAdapter: InvitesAdapter // Assuming you have an InvitesAdapter
    private  var applicationsList: MutableList<esportsNotificationData> = mutableListOf()
    private  var invitesList: MutableList<esportsNotificationData> = mutableListOf()
    private lateinit var organizationName: String
    private val viewModel: ManageApplicationsAndInvitesViewModel by lazy {
        ViewModelProvider(this)[ManageApplicationsAndInvitesViewModel::class.java]
    }

    companion object {
        fun newInstance() = manageApplicationsAndInvitesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        organizationName = arguments?.getString("organization_name") ?: ""
        return inflater.inflate(R.layout.fragment_manage_applications_and_invites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        applicationsRecyclerView = view.findViewById(R.id.applicationsRecyclerView)
        invitesRecyclerView = view.findViewById(R.id.invitesRecyclerView)

        // Set up RecyclerViews
        getNotificationDetails()
        setupApplicationsRecyclerView()
        setupInvitesRecyclerView()

    }

    private fun getNotificationDetails() {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Fetch notifications for this organization
                        if (!orgId.isNullOrEmpty()) {
                            fetchApplications(orgId)
                            fetchInvites(orgId)

                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
    }

    private fun fetchApplications(orgId: String) {
        // Reference to the esportsNotifications/applications node for the organization
        val organizationNotificationsRef = FirebaseDatabase.getInstance()
            .getReference("organizationsData")
            .child(orgId)
            .child("esportsNotifications")
            .child("applications")

        // Fetch notifications
        organizationNotificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    // Iterate through the notifications and parse them into objects
                    for (notificationSnapshot in snapshot.children) {
                        val notification = notificationSnapshot.getValue(esportsNotificationData::class.java)
                        notification?.let {
                            applicationsList.add(it)
                        }
                    }
                }

                // Update the adapter's list with the fetched notifications
                applicationsAdapter.updateData(applicationsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching notifications
                println("Error fetching notifications: ${error.message}")
            }
        })
    }

    private fun fetchInvites(orgId: String) {
        // Reference to the esportsNotifications/applications node for the organization
        val organizationNotificationsRef = FirebaseDatabase.getInstance()
            .getReference("organizationsData")
            .child(orgId)
            .child("esportsNotifications")
            .child("invites")

        // Fetch notifications
        organizationNotificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    // Iterate through the notifications and parse them into objects
                    for (notificationSnapshot in snapshot.children) {
                        val notification = notificationSnapshot.getValue(esportsNotificationData::class.java)
                        notification?.let {
                            invitesList.add(it)
                        }
                    }
                }

                // Update the adapter's list with the fetched notifications
                invitesAdapter.updateData(invitesList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching notifications
                println("Error fetching notifications: ${error.message}")
            }
        })
    }


    private fun setupApplicationsRecyclerView() {
        // Initialize the adapter
        applicationsAdapter = ApplicationsAdapter(requireContext(), mutableListOf(),{notificationId ->

            deleteApplication(notificationId)
        })

        // Set up RecyclerView with LayoutManager and Adapter
        applicationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = applicationsAdapter
        }
    }

    private fun deleteApplication(notificationId: String) {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name (assuming `organizationName` is available)
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Reference to the specific notification in the applications node
                        if (!orgId.isNullOrEmpty()) {
                            val notificationRef = FirebaseDatabase.getInstance()
                                .getReference("organizationsData")
                                .child(orgId)
                                .child("esportsNotifications")
                                .child("applications")
                                .child(notificationId)

                            // Delete the notification
                            notificationRef.removeValue()
                                .addOnSuccessListener {
                                    // Successfully deleted the notification
                                    applicationsAdapter.updateData(applicationsList)
                                    println("Notification with ID $notificationId deleted successfully")
                                }
                                .addOnFailureListener { error ->
                                    // Handle failure to delete the notification
                                    println("Failed to delete notification: ${error.message}")
                                }
                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
    }

    private fun deleteInvite(notificationId: String) {
        // Reference to the organizationsData node in Firebase
        val organizationsRef = FirebaseDatabase.getInstance().getReference("organizationsData")

        // Query to find the organization by its name (assuming `organizationName` is available)
        val orgQuery = organizationsRef.orderByChild("organizationName").equalTo(organizationName)

        // Execute the query to find the organization
        orgQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results (though there should only be one match)
                    for (orgSnapshot in snapshot.children) {
                        // Retrieve the organization ID
                        val orgId = orgSnapshot.key

                        // Reference to the specific notification in the applications node
                        if (!orgId.isNullOrEmpty()) {
                            val notificationRef = FirebaseDatabase.getInstance()
                                .getReference("organizationsData")
                                .child(orgId)
                                .child("esportsNotifications")
                                .child("invites")
                                .child(notificationId)

                            // Delete the notification
                            notificationRef.removeValue()
                                .addOnSuccessListener {
                                    // Successfully deleted the notification
                                    invitesAdapter.updateData(invitesList)
                                    println("Notification with ID $notificationId deleted successfully")
                                }
                                .addOnFailureListener { error ->
                                    // Handle failure to delete the notification
                                    println("Failed to delete notification: ${error.message}")
                                }
                        } else {
                            println("Organization ID is null or empty")
                        }
                    }
                } else {
                    // Handle the case where no organization is found with the given name
                    println("No organization found with the name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors when fetching data
                println("Database error: ${error.message}")
            }
        })
    }

    private fun setupInvitesRecyclerView() {
        // Initialize the adapter (assuming you have an InvitesAdapter)
        invitesAdapter = InvitesAdapter(requireContext(), mutableListOf(),{notificationId ->

            deleteInvite(notificationId)
        })

        // Set up RecyclerView with LayoutManager and Adapter
        invitesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = invitesAdapter
        }

        // Set up RecyclerView with LayoutManager and Adapter

    }

}