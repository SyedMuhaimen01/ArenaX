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
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager
import com.muhaimen.arenax.explore.SearchUserAdapter

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

        // Initialize RecyclerViews and adapters
        setupChatRecyclerView()
        setupSearchUserRecyclerView()

        searchEditText = findViewById(R.id.searchbar)
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toggleRecyclerViewVisibility(showSearch = true)
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText, searchUserRecyclerView.adapter as SearchUserAdapter)
                } else {
                    (searchUserRecyclerView.adapter as SearchUserAdapter).updateUserList(emptyList())
                }
            }
        })

        onBackPressedDispatcher.addCallback(this) {
            if (searchUserRecyclerView.visibility == View.VISIBLE) {
                toggleRecyclerViewVisibility(showSearch = false)
                searchEditText.text.clear()
            } else {
                finish()
            }
        }

        currentUserId?.let { fetchUserChats(it) } ?: Log.e("ViewAllChats", "Current user ID is null.")
    }

    private fun setupChatRecyclerView() {
        chatRecyclerView = findViewById(R.id.viewChatsRecyclerView)
        chatAdapter = ViewAllChatsAdapter(emptyList())
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter
    }

    private fun setupSearchUserRecyclerView() {
        searchUserRecyclerView = findViewById(R.id.searchUserRecyclerView)
        val searchUserAdapter = SearchUserAdapter(emptyList())
        searchUserRecyclerView.layoutManager = LinearLayoutManager(this)
        searchUserRecyclerView.adapter = searchUserAdapter
    }

    private fun toggleRecyclerViewVisibility(showSearch: Boolean) {
        chatRecyclerView.visibility = if (showSearch) View.GONE else View.VISIBLE
        searchUserRecyclerView.visibility = if (showSearch) View.VISIBLE else View.GONE
    }

    private fun fetchUserChats(userId: String) {
        val chatsRef = database.getReference("userData/$userId/chats")
        val uniqueChatPairs = mutableSetOf<Pair<String, String>>()
        val chatItems = mutableListOf<ChatItem>()

        Log.d("ViewAllChats", "Fetching chats for user: $userId")

        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { chatPairSnapshot ->
                    val otherUserId = chatPairSnapshot.key?.split("-")?.find { it != userId } ?: return@forEach
                    val latestChatSnapshot = chatPairSnapshot.children.maxByOrNull {
                        it.child("timestamp").getValue(Long::class.java) ?: 0L
                    }

                    latestChatSnapshot?.let {
                        val chatId = it.key ?: return@forEach
                        val senderId = it.child("senderId").value?.toString().orEmpty()
                        val receiverId = it.child("receiverId").value?.toString().orEmpty()
                        val lastMessage = it.child("lastMessage").value?.toString().orEmpty()
                        val timestamp = it.child("timestamp").getValue(Long::class.java) ?: 0L

                        val chatPair = getOrderedPair(senderId, receiverId)
                        if (uniqueChatPairs.add(chatPair)) {
                            chatItems.add(ChatItem(chatId, senderId, receiverId, lastMessage, timestamp))
                        }
                    }
                }

                chatAdapter.updateChatList(chatItems.sortedByDescending { it.time })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewAllChats", "Failed to fetch chats: ${error.message}")
            }
        })
    }

    private fun getOrderedPair(id1: String, id2: String): Pair<String, String> {
        return if (id1 < id2) Pair(id1, id2) else Pair(id2, id1)
    }

    private fun searchUsers(query: String, adapter: SearchUserAdapter) {
        val usersRef = database.getReference("userData")
        val searchQuery = query.lowercase() // Case-insensitive search
        val usersList = mutableListOf<UserData>()

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != currentUserId) {
                        val fullname = user.fullname?.lowercase().orEmpty()
                        val gamerTag = user.gamerTag?.lowercase().orEmpty()

                        if (fullname.contains(searchQuery) || gamerTag.contains(searchQuery)) {
                            usersList.add(user)
                        }
                    }
                }
                adapter.updateUserList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewAllChats", "Search error: ${error.message}")
            }
        })
    }
}
