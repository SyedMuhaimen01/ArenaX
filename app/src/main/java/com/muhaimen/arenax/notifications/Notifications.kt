package com.muhaimen.arenax.notifications

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.NotificationsItem
import com.muhaimen.arenax.dataClasses.UserData

class Notifications : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: NotificationsAdapter
    private lateinit var recyclerView: RecyclerView
    private val notificationList = mutableListOf<NotificationsItem>()
    private lateinit var backButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        adapter = NotificationsAdapter(
            this,
            notificationList,
            onAcceptClick = { notificationItem -> updateStatus(notificationItem.receiverId, "accepted") },
            onRejectClick = { notificationItem -> deleteNotification(notificationItem.receiverId) }
        )

        recyclerView = findViewById(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return
        val followersRef = database.child("userData").child(currentUserId).child("synerG").child("followers")

        followersRef.orderByChild("status").equalTo("pending")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notificationList.clear()
                    for (request in snapshot.children) {
                        val receiverId = request.child("followerId").getValue(String::class.java) ?: continue

                        // Fetch user data for each receiverId
                        database.child("userData").child(receiverId).get()
                            .addOnSuccessListener { userSnapshot ->
                                val user = userSnapshot.getValue(UserData::class.java)
                                if (user != null) {
                                    notificationList.add(
                                        NotificationsItem(
                                            profilePicture = user.profilePicture ?: "",
                                            username = user.fullname ?: "",
                                            receiverId = receiverId
                                        )
                                    )
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Notifications", "Error fetching user data", exception)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Notifications", "Error fetching notifications", error.toException())
                }
            })
    }

    private fun updateStatus(receiverId: String, status: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Update the status in the current user's followers list
        val notificationRef = database.child("userData").child(currentUserId).child("synerG").child("followers").child(receiverId)
        notificationRef.child("status").setValue(status).addOnSuccessListener {

            // Also update the status in the receiver's following list
            val followingRef = database.child("userData").child(receiverId).child("synerG").child("following").child(currentUserId)
            followingRef.child("status").setValue(status).addOnSuccessListener {
                notificationList.removeAll { it.receiverId == receiverId }
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { exception ->
                Log.e("Notifications", "Error updating following status", exception)
            }
        }.addOnFailureListener { exception ->
            Log.e("Notifications", "Error updating follower status", exception)
        }
    }

    private fun deleteNotification(receiverId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Remove the notification from the current user's followers list
        val notificationRef = database.child("userData").child(currentUserId).child("synerG").child("followers").child(receiverId)
        notificationRef.removeValue().addOnSuccessListener {

            // Also remove the notification from the receiver's following list
            val followingRef = database.child("userData").child(receiverId).child("synerG").child("following").child(currentUserId)
            followingRef.removeValue().addOnSuccessListener {
                notificationList.removeAll { it.receiverId == receiverId }
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { exception ->
                Log.e("Notifications", "Error removing following notification", exception)
            }
        }.addOnFailureListener { exception ->
            Log.e("Notifications", "Error removing follower notification", exception)
        }
    }
}
