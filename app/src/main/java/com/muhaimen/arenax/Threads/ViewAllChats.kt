package com.muhaimen.arenax.Threads

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager

class ViewAllChats : AppCompatActivity() {

    private lateinit var chatAdapter: ViewAllChatsAdapter
    private lateinit var searchEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var searchUserRecyclerView: RecyclerView
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId = FirebaseManager.getCurrentUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_chats)

        // Chat RecyclerView setup
        chatRecyclerView = findViewById(R.id.viewChatsRecyclerView)
        chatAdapter = ViewAllChatsAdapter(emptyList())
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter
        Log.d("ViewAllChats", "Initialized chatRecyclerView with adapter")

        // Stories RecyclerView setup

        // User search setup
        searchUserRecyclerView = findViewById(R.id.searchUserRecyclerView)
        val searchUserAdapter = SearchUserAdapter(emptyList())
        searchUserRecyclerView.layoutManager = LinearLayoutManager(this)
        searchUserRecyclerView.adapter = searchUserAdapter
        chatRecyclerView.visibility = View.VISIBLE

        searchEditText = findViewById(R.id.searchbar)
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                chatRecyclerView.visibility = View.GONE
                searchUserRecyclerView.visibility = View.VISIBLE
                Log.d("ViewAllChats", "Switched to search mode")
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText, searchUserAdapter)
                } else {
                    searchUserAdapter.updateUserList(emptyList())
                }
            }
        })

        onBackPressedDispatcher.addCallback(this) {
            if (searchUserRecyclerView.visibility == View.VISIBLE) {
                searchUserRecyclerView.visibility = View.GONE
                chatRecyclerView.visibility = View.VISIBLE
                searchEditText.text.clear()
            } else {
                finish()
            }
        }

        if (currentUserId != null) {
            fetchUserChats(currentUserId)
        } else {
            Log.e("ViewAllChats", "Current user ID is null.")
        }
    }

    private fun fetchUserChats(userId: String) {
        val chatsRef = database.getReference("userData/$userId/chats")
        val uniqueChatPairs = mutableSetOf<Pair<String, String>>()
        val chatItems = mutableListOf<ChatItem>()

        // Log the start of the fetch operation
        Log.d("ViewAllChats", "Fetching chats for user: $userId")

        // Fetch all child nodes under the user's chats node
        chatsRef.get().addOnSuccessListener { userChatsSnapshot ->
            Log.d("ViewAllChats", "Fetched all chat pairs for user: ${userChatsSnapshot.childrenCount}")

            userChatsSnapshot.children.forEach { chatPairSnapshot ->
                // For each chat pair node (currentUser-otherUser)
                val otherUserId = chatPairSnapshot.key?.split("-")?.find { it != userId } ?: return@forEach

                // Fetch the latest chat item from this chat pair
                val latestChatSnapshot = chatPairSnapshot.children.maxByOrNull { it.child("timestamp").getValue(Long::class.java) ?: 0L }

                // If there's a valid latest chat, process it
                if (latestChatSnapshot != null) {
                    val chatId = latestChatSnapshot.key ?: return@forEach
                    val senderId = latestChatSnapshot.child("senderId").value?.toString() ?: ""
                    val receiverId = latestChatSnapshot.child("receiverId").value?.toString() ?: ""
                    val lastMessage = latestChatSnapshot.child("lastMessage").value?.toString() ?: ""
                    val timestamp = latestChatSnapshot.child("time").value as Long

                    // Log the details of the latest chat item fetched
                    Log.d("ViewAllChats", "Latest chat fetched: chatId=$chatId, senderId=$senderId, receiverId=$receiverId, lastMessage=$lastMessage, timestamp=$timestamp")

                    // Ensure that the chat pair is unique
                    val chatPair = getOrderedPair(senderId, receiverId)
                    if (uniqueChatPairs.add(chatPair)) {
                        val chatItem = ChatItem(chatId, senderId, receiverId, lastMessage, timestamp)
                        chatItems.add(chatItem)
                        Log.d("ViewAllChats", "Added unique chat: $chatItem")
                    }
                }
            }

            // Log the final chat list before updating the adapter
            Log.d("ViewAllChats", "All chats fetched, updating RecyclerView with ${chatItems.size} items")
            chatAdapter.updateChatList(chatItems.sortedByDescending { it.time })

        }.addOnFailureListener {
            Log.e("ViewAllChats", "Failed to fetch chats: ${it.message}")
        }
    }




    private fun getOrderedPair(id1: String, id2: String): Pair<String, String> {
        return if (id1 < id2) Pair(id1, id2) else Pair(id2, id1)
    }

    private fun searchUsers(query: String, adapter: SearchUserAdapter) {
        val usersRef = database.getReference("userData")
        usersRef.orderByChild("fullname").startAt(query).endAt(query + "\uf8ff").get().addOnSuccessListener { dataSnapshot ->
            val usersList = mutableListOf<UserData>()
            dataSnapshot.children.forEach { snapshot ->
                val user = snapshot.getValue(UserData::class.java)
                if (user != null && user.userId != currentUserId) {
                    usersList.add(user)
                }
            }
            adapter.updateUserList(usersList)
            Log.d("ViewAllChats", "Found ${usersList.size} users matching query: $query")
        }.addOnFailureListener {
            Log.e("ViewAllChats", "Failed to search users: ${it.message}")
        }
    }
}
