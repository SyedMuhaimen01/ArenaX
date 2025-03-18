package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.esportsNotificationData
import com.muhaimen.arenax.utils.FirebaseManager

class notificationsFragment : Fragment() {

    private lateinit var applicationsRecyclerView: RecyclerView
    private lateinit var invitesRecyclerView: RecyclerView
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private val userId=FirebaseManager.getCurrentUserId()
    private lateinit var invitesAdapter: InvitesAdapter
    private var applicationsList: MutableList<esportsNotificationData> = mutableListOf()
    private var invitesList: MutableList<esportsNotificationData> = mutableListOf()


    companion object {
        fun newInstance() = notificationsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_applications_and_invites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        applicationsRecyclerView = view.findViewById(R.id.applicationsRecyclerView)
        invitesRecyclerView = view.findViewById(R.id.invitesRecyclerView)

        // Set up RecyclerViews
        userId?.let { fetchApplications(it) }
        userId?.let { fetchInvites(it) }
        setupApplicationsRecyclerView()
        setupInvitesRecyclerView()

    }




    private fun fetchApplications(userId: String) {
        // Reference to the esportsNotifications/applications node for the organization
        val organizationNotificationsRef = FirebaseDatabase.getInstance()
            .getReference("userData")
            .child(userId)
            .child("esportsNotifications")
            .child("applications")

        // Fetch notifications
        organizationNotificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    // Iterate through the notifications and parse them into objects
                    for (notificationSnapshot in snapshot.children) {
                        val notification =
                            notificationSnapshot.getValue(esportsNotificationData::class.java)
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

    private fun fetchInvites(userId: String) {
        // Reference to the esportsNotifications/applications node for the organization
        val organizationNotificationsRef = FirebaseDatabase.getInstance()
            .getReference("userData")
            .child(userId)
            .child("esportsNotifications")
            .child("invites")

        // Fetch notifications
        organizationNotificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    // Iterate through the notifications and parse them into objects
                    for (notificationSnapshot in snapshot.children) {
                        val notification =
                            notificationSnapshot.getValue(esportsNotificationData::class.java)
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
        applicationsAdapter =
            ApplicationsAdapter(requireContext(), mutableListOf(), { notificationId ->

                deleteApplication(notificationId)
            })

        // Set up RecyclerView with LayoutManager and Adapter
        applicationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = applicationsAdapter
        }
    }

    private fun deleteInvite(notificationId: String) {

        val notificationRef = userId?.let {
            FirebaseDatabase.getInstance()
                .getReference("userData")
                .child(it)
                .child("esportsNotifications")
                .child("invites")
                .child(notificationId)
        }

        // Delete the notification
        notificationRef?.removeValue()?.addOnSuccessListener {
            // Successfully deleted the notification
            invitesAdapter.updateData(invitesList)
            println("Notification with ID $notificationId deleted successfully")
        }?.addOnFailureListener { error ->
            // Handle failure to delete the notification
            println("Failed to delete notification: ${error.message}")
        }

    }

private fun deleteApplication(notificationId: String) {

    val notificationRef = userId?.let {
        FirebaseDatabase.getInstance()
            .getReference("userData")
            .child(it)
            .child("esportsNotifications")
            .child("applications")
            .child(notificationId)
    }

    // Delete the notification
    notificationRef?.removeValue()?.addOnSuccessListener {
        // Successfully deleted the notification
        applicationsAdapter.updateData(applicationsList)
        println("Notification with ID $notificationId deleted successfully")
    }?.addOnFailureListener { error ->
        // Handle failure to delete the notification
        println("Failed to delete notification: ${error.message}")
    }

}

    private fun setupInvitesRecyclerView() {
        // Initialize the adapter (assuming you have an InvitesAdapter)
        invitesAdapter = InvitesAdapter(requireContext(), mutableListOf(), { notificationId ->

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

