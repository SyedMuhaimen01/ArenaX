package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox.Threads

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager


data class FrequentSearchedUser(
    val fullname: String = "Unknown User",
    val profilePicture: String = "",
    val gamerTag: String = "",
    val gamerRank: String = ""
)

class SearchUserAdapter(
    private var userList: List<UserData>,
    private val organizationId: String?
) : RecyclerView.Adapter<SearchUserAdapter.SearchViewHolder>() {

    // Cache to store user data
    private val userCache = mutableMapOf<String, FrequentSearchedUser>()

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profilePicture)
        val fullnameTextView: TextView = view.findViewById(R.id.fullname)
        val gamerTagTextView: TextView = view.findViewById(R.id.gamerTag)
        val gamerRankTextView: TextView = view.findViewById(R.id.gamerRank)

        // Set the click listener for the item view here
        init {
            itemView.setOnClickListener {
                // Get the current user data based on the position
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = userList[position]
                    // Start the ViewGameAnalytics activity
                    val intent = Intent(itemView.context, organizationChatActivity::class.java).apply {
                        putExtra("userId", user.userId)
                        putExtra("fullname", user.fullname)
                        putExtra("gamerTag", user.gamerTag)
                        putExtra("profilePicture", user.profilePicture)
                        putExtra("organizationId", organizationId)
                    }
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.explore_accounts_card, parent, false)
        return SearchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val userItem = userList[position]

        // Check cache first
        val cachedUser = userCache[userItem.userId]
        if (cachedUser != null) {
            bindUserData(holder, cachedUser)
        } else {
            // Fetch and cache the user data if not available in the cache
            fetchAndCacheUserData(userItem.userId) { frequentUser ->
                bindUserData(holder, frequentUser)
            }
        }
    }

    private fun fetchAndCacheUserData(userId: String, callback: (FrequentSearchedUser) -> Unit) {
        val database = FirebaseManager.getDatabseInstance()
        val userRef = database.getReference("userData").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val fullname = dataSnapshot.child("fullname").value?.toString() ?: "Unknown User"
            val gamerTag = dataSnapshot.child("gamerTag").value?.toString() ?: ""
            val gamerRank = dataSnapshot.child("gamerRank").value?.toString() ?: "Rank: 00"
            val profileImageUrl = dataSnapshot.child("profilePicture").value?.toString() ?: ""

            // Create a new FrequentSearchedUser instance and cache it
            val frequentUser = FrequentSearchedUser(fullname, profileImageUrl, gamerTag, gamerRank)
            userCache[userId] = frequentUser

            // Trigger the callback with the fetched data
            callback(frequentUser)
        }.addOnFailureListener {
            // Use default FrequentSearchedUser in case of an error
            callback(FrequentSearchedUser())
        }
    }

    private fun bindUserData(holder: SearchViewHolder, user: FrequentSearchedUser) {
        holder.fullnameTextView.text = user.fullname
        holder.gamerTagTextView.text = user.gamerTag
        holder.gamerRankTextView.text = user.gamerRank
        Glide.with(holder.profileImage.context)
            .load(user.profilePicture)
            .placeholder(R.drawable.game_icon_foreground)
            .error(R.drawable.game_icon_foreground)
            .circleCrop()
            .into(holder.profileImage)
    }

    override fun getItemCount(): Int = userList.size

    // Update the user list, retaining cached users
    @SuppressLint("NotifyDataSetChanged")
    fun updateUserList(newUserList: List<UserData>) {
        val newUsersToFetch = newUserList.filter { !userCache.containsKey(it.userId) }
        userList = newUserList

        // Fetch data only for new users not in cache
        newUsersToFetch.forEach { user ->
            fetchAndCacheUserData(user.userId) {
                notifyDataSetChanged() // Update view once all data is fetched
            }
        }
        notifyDataSetChanged() // Refresh the whole list to apply changes
    }
}
