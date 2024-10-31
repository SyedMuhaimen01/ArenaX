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

class ViewAllChats : AppCompatActivity() {

    private lateinit var chatAdapter: ViewAllChatsAdapter
    private lateinit var searchEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var searchUserRecyclerView: RecyclerView
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_chats)

        chatRecyclerView = findViewById(R.id.viewChatsRecyclerView)
        chatAdapter = ViewAllChatsAdapter(emptyList())
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter
        Log.d("ViewAllChats", "Initialized chatRecyclerView with adapter")

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
        val chatsRef = database.getReference("chats")
        val uniqueChatPairs = mutableSetOf<Pair<String, String>>()
        val chatItems = mutableListOf<ChatItem>()

        chatsRef.orderByChild("senderId").equalTo(userId).get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { snapshot ->
                val chatId = snapshot.key ?: return@forEach
                val receiverId = snapshot.child("receiverId").value?.toString() ?: ""
                val lastMessage = snapshot.child("lastMessage").value?.toString() ?: ""
                val timestamp = snapshot.child("timestamp").value as? Long ?: 0

                val chatPair = getOrderedPair(userId, receiverId)
                if (uniqueChatPairs.add(chatPair)) {
                    val chatItem = ChatItem(chatId, userId, receiverId, lastMessage, timestamp)
                    chatItems.add(chatItem)
                    Log.d("ViewAllChats", "Fetched unique chat: $chatItem")
                }
            }

            chatsRef.orderByChild("receiverId").equalTo(userId).get().addOnSuccessListener { receiverSnapshot ->
                receiverSnapshot.children.forEach { snapshot ->
                    val chatId = snapshot.key ?: return@forEach
                    val senderId = snapshot.child("senderId").value?.toString() ?: ""
                    val lastMessage = snapshot.child("lastMessage").value?.toString() ?: ""
                    val timestamp = snapshot.child("timestamp").value as? Long ?: 0

                    val chatPair = getOrderedPair(senderId, userId)
                    if (uniqueChatPairs.add(chatPair)) {
                        val chatItem = ChatItem(chatId, senderId, userId, lastMessage, timestamp)
                        chatItems.add(chatItem)
                        Log.d("ViewAllChats", "Fetched unique chat: $chatItem")
                    }
                }

                chatAdapter.updateChatList(chatItems)
                Log.d("ViewAllChats", "Total unique chats fetched: ${chatItems.size}")
            }.addOnFailureListener {
                Log.e("ViewAllChats", "Error fetching receiver chats", it)
            }
        }.addOnFailureListener {
            Log.e("ViewAllChats", "Error fetching sender chats", it)
        }
    }

    private fun getOrderedPair(id1: String, id2: String): Pair<String, String> {
        return if (id1 < id2) id1 to id2 else id2 to id1
    }

    private fun searchUsers(query: String, searchUserAdapter: SearchUserAdapter) {
        val userRef = database.getReference("userData")

        userRef.orderByChild("fullname").startAt(query).endAt("$query\uf8ff")
            .get().addOnSuccessListener { dataSnapshot ->
                val users = mutableListOf<UserData>()

                dataSnapshot.children.forEach { snapshot ->
                    val userId = snapshot.key ?: return@forEach
                    val fullname = snapshot.child("fullname").value?.toString() ?: "Unknown User"
                    val gamerTag = snapshot.child("gamerTag").value?.toString() ?: "Unknown User"
                    val profilePicture = snapshot.child("profilePicture").value?.toString()

                    if (fullname.contains(query, ignoreCase = true)) {
                        val userData = UserData(
                            userId = userId,
                            fullname = fullname,
                            gamerTag = gamerTag,
                            profilePicture = profilePicture
                        )
                        users.add(userData)
                        Log.d("SearchUsers", "Matching user found: $fullname (ID: $userId)")
                    }
                }

                searchUserAdapter.updateUserList(users)
                Log.d("SearchUsers", "Total matching users: ${users.size}")
            }.addOnFailureListener {
                Log.e("SearchUsers", "Error retrieving users", it)
            }
    }
}
