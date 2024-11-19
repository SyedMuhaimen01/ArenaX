package com.muhaimen.arenax.explore

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.Threads.SearchUserAdapter
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager

class exploreAccounts : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var searchUserRecyclerView: RecyclerView
    private lateinit var exploreAccountsAdapter: exploreAccountsAdapter
    private val currentUserId = FirebaseManager.getCurrentUserId()
    private lateinit var searchEditText: EditText
    private lateinit var exploreRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_accounts, container, false)

        // Initialize RecyclerView for the explore accounts section
        exploreRecyclerView = view.findViewById(R.id.accounts_recyclerview)
        exploreRecyclerView.layoutManager = LinearLayoutManager(this.context)

        // Initialize search RecyclerView
        searchUserRecyclerView = view.findViewById(R.id.searchUserRecyclerView)
        searchUserRecyclerView.layoutManager = LinearLayoutManager(this.context)
        val searchUserAdapter = SearchUserAdapter(emptyList())
        searchUserRecyclerView.adapter = searchUserAdapter

        // Initialize EditText for search input
        searchEditText = view.findViewById(R.id.searchbar)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    searchUserRecyclerView.visibility = View.GONE
                    exploreRecyclerView.visibility = View.VISIBLE
                } else {
                    searchUserRecyclerView.visibility = View.VISIBLE
                    exploreRecyclerView.visibility = View.GONE
                    searchUsers(s.toString(), searchUserAdapter)
                }
            }
        })

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("userData")

        // Fetch and display random user profiles
        fetchRandomUserProfiles()

        return view
    }

    private fun fetchRandomUserProfiles() {
        val userProfiles = mutableListOf<UserData>()
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserData::class.java)
                    if (user != null && user.userId != currentUserId) {
                        userProfiles.add(user)
                    }
                }
                // Initialize adapter with fetched data
                exploreAccountsAdapter = exploreAccountsAdapter(userProfiles)
                exploreRecyclerView.adapter = exploreAccountsAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch user profiles: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ExploreAccounts", "Error: ${error.message}")
            }
        })
    }

    private fun searchUsers(query: String, adapter: SearchUserAdapter) {
        val searchResults = mutableListOf<UserData>()
        database.orderByChild("fullname").startAt(query).endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    searchResults.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(UserData::class.java)
                        if (user != null && user.userId != currentUserId) {
                            searchResults.add(user)
                        }
                    }
                    adapter.updateUserList(searchResults)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SearchUsers", "Failed to search users: ${error.message}")
                }
            })
    }
}
