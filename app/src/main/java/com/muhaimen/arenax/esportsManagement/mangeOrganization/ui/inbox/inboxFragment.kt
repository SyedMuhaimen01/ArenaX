package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox.Threads.SearchUserAdapter

class inboxFragment : Fragment() {

    private lateinit var organizationName: String
    private lateinit var searchEditText: EditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var searchUserRecyclerView: RecyclerView
    private lateinit var chatAdapter: inboxAdapter
    private lateinit var searchUserAdapter: SearchUserAdapter
    private lateinit var database: FirebaseDatabase
    private var organizationId: String? = null

    companion object {
        fun newInstance() = inboxFragment()
    }

    private val viewModel: InboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        organizationName = arguments?.getString("organization_name") ?: ""
        database = FirebaseDatabase.getInstance()

        fetchOrganizationId()
    }

    private fun fetchOrganizationId() {
        val databaseRef = database.getReference("organizationsData")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundOrganizationId: String? = null

                for (organizationSnapshot in snapshot.children) {
                    val name = organizationSnapshot.child("organizationName").getValue(String::class.java)
                    if (name == organizationName) {
                        foundOrganizationId = organizationSnapshot.key
                        break
                    }
                }

                if (foundOrganizationId != null) {
                    organizationId = foundOrganizationId
                    Log.d("InboxFragment", "Fetched Organization ID: $organizationId")
                    setupChatRecyclerView(organizationId!!)
                    setupSearchUserRecyclerView(organizationId!!)
                    fetchUserChats(organizationId!!)
                } else {
                    Log.e("InboxFragment", "No organization found with name: $organizationName")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InboxFragment", "Database error: ${error.message}")
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        searchEditText = view.findViewById(R.id.searchbar)
        chatRecyclerView = view.findViewById(R.id.viewChatsRecyclerView)
        searchUserRecyclerView = view.findViewById(R.id.searchUserRecyclerView)



        setupSearchFunctionality()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (searchUserRecyclerView.visibility == View.VISIBLE) {
                toggleRecyclerViewVisibility(showSearch = false)
                searchEditText.text.clear()
            } else {
                requireActivity().finish()
            }
        }

        return view
    }

    private fun setupChatRecyclerView(id:String) {
        chatAdapter = inboxAdapter(mutableListOf(), id)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = chatAdapter
    }

    private fun setupSearchUserRecyclerView(id:String) {
        searchUserAdapter = SearchUserAdapter(mutableListOf(), id)
        searchUserRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchUserRecyclerView.adapter = searchUserAdapter
    }

    private fun setupSearchFunctionality() {
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) toggleRecyclerViewVisibility(showSearch = true)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText)
                } else {
                    searchUserAdapter.updateUserList(emptyList())
                }
            }
        })
    }

    private fun toggleRecyclerViewVisibility(showSearch: Boolean) {
        chatRecyclerView.visibility = if (showSearch) View.GONE else View.VISIBLE
        searchUserRecyclerView.visibility = if (showSearch) View.VISIBLE else View.GONE
    }

    private fun fetchUserChats(organizationId: String) {
        val chatsRef = database.getReference("organizationsData/$organizationId/chats")
        val chatItems = mutableListOf<ChatItem>()

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatItems.clear()
                snapshot.children.forEach { chatPairSnapshot ->
                    val latestChatSnapshot = chatPairSnapshot.children.maxByOrNull {
                        it.child("timestamp").getValue(Long::class.java) ?: 0L
                    }

                    latestChatSnapshot?.let {
                        val chatId = chatPairSnapshot.key.orEmpty()
                        val senderId = it.child("senderId").value?.toString().orEmpty()
                        val receiverId = it.child("receiverId").value?.toString().orEmpty()
                        val lastMessage = it.child("lastMessage").value?.toString().orEmpty()
                        val timestamp = it.child("timestamp").getValue(Long::class.java) ?: 0L

                        chatItems.add(ChatItem(chatId, senderId, receiverId, lastMessage, timestamp))
                    }
                }
                chatAdapter.updateChatList(chatItems.sortedByDescending { it.time })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InboxFragment", "Failed to fetch chats: ${error.message}")
            }
        })
    }

    private fun searchUsers(query: String) {
        val usersRef = database.getReference("userData")
        val searchQuery = query.lowercase()
        val usersList = mutableListOf<UserData>()

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != organizationId) {
                        val fullname = user.fullname?.lowercase().orEmpty()
                        val gamerTag = user.gamerTag?.lowercase().orEmpty()

                        if (fullname.contains(searchQuery) || gamerTag.contains(searchQuery)) {
                            usersList.add(user)
                        }
                    }
                }
                searchUserAdapter.updateUserList(usersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InboxFragment", "Search error: ${error.message}")
            }
        })
    }
}
