package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.settings.manageAdmins

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.utils.FirebaseManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.esportsManagement.mangeOrganization.OrganizationHomePageActivity
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

@Suppress("UNREACHABLE_CODE")
class manageAdmins : AppCompatActivity() {
    private lateinit var adminsRecyclerView: RecyclerView
    private lateinit var superAdminTextView: TextView
    private lateinit var superAdminProfilePicture: ImageView
    private lateinit var manageAdminsAdapter: ManageAdminsAdapter
    private lateinit var searchAdminsAdapter: SearchAdminsAdapter
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var backButton: ImageButton
    private lateinit var addAdminButton: FloatingActionButton
    private lateinit var searchbarLinearLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var organizationName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_admins)

        organizationName = intent.getStringExtra("organization_name") ?: ""

        // Set up system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        database = FirebaseDatabase.getInstance().reference.child("userData")

        // Initialize views
        adminsRecyclerView = findViewById(R.id.adminsRecyclerview)
        superAdminTextView = findViewById(R.id.superAdminTextView)
        superAdminProfilePicture = findViewById(R.id.superAdminProfilePicture)
        searchRecyclerView = findViewById(R.id.searchAdminsRecyclerView)
        searchBar = findViewById(R.id.searchbar)
        searchbarLinearLayout = findViewById(R.id.searchbarLinearLayout)
        addAdminButton = findViewById(R.id.addAdminButton)
        backButton = findViewById(R.id.backButton)

        adminsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        fetchAdminsList()
        manageAdminsAdapter = ManageAdminsAdapter(mutableListOf()) { userId ->
            removeAdmin(userId)
        }

        searchAdminsAdapter = SearchAdminsAdapter(mutableListOf()) { userId ->
            addAdmin(userId)
        }

        adminsRecyclerView.adapter = manageAdminsAdapter
        searchRecyclerView.adapter = searchAdminsAdapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {
                    searchUsers(searchText)
                } else {
                    searchAdminsAdapter.updateAdminsList(emptyList())
                }
            }
        })

        addAdminButton.setOnClickListener {
            if (searchbarLinearLayout.visibility == View.VISIBLE) {
                searchbarLinearLayout.visibility = View.GONE
                searchRecyclerView.visibility = View.GONE
                adminsRecyclerView.visibility = View.VISIBLE
            } else {
                searchbarLinearLayout.visibility = View.VISIBLE
                searchRecyclerView.visibility = View.VISIBLE
                adminsRecyclerView.visibility = View.GONE
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, OrganizationHomePageActivity::class.java)
            intent.putExtra("organization_name", organizationName)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchRecyclerView.visibility == View.VISIBLE) {
                    searchRecyclerView.visibility = View.GONE
                    adminsRecyclerView.visibility = View.VISIBLE
                    searchBar.text.clear()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        FirebaseManager.getCurrentUserId()?.let { userId ->
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserData::class.java)
                    if (user != null) {
                        superAdminTextView.text = user.fullname
                        Glide.with(this@manageAdmins)
                            .load(user.profilePicture ?: R.drawable.battlegrounds_icon_background)
                            .placeholder(R.drawable.battlegrounds_icon_background)
                            .error(R.drawable.battlegrounds_icon_background)
                            .circleCrop()
                            .into(superAdminProfilePicture)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun searchUsers(query: String) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = snapshot.children.mapNotNull { it.getValue(UserData::class.java) }
                    .filter { user ->
                        user.userId != FirebaseManager.getCurrentUserId() &&
                                (user.fullname.contains(query, ignoreCase = true) ||
                                        user.gamerTag.contains(query, ignoreCase = true))
                    }
                searchAdminsAdapter.updateAdminsList(usersList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addAdmin(userId: String) {
        val context: Context = this
        val currentUser = FirebaseManager.getCurrentUserId()
        Log.d("AddAdmin", "Adding admin: $userId, current user: $currentUser")
        val url = "${Constants.SERVER_URL}manageOrganizations/addAdmin"

        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
            put("currentUserId", currentUser) // Current user making the request
            put("adminId", userId) // Admin to be added
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("AddAdmin", "Admin added successfully")
                Toast.makeText(context, "Admin added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, manageAdmins::class.java)
                intent.putExtra("organization_name", organizationName)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let { response ->
                    val responseData = String(response.data, Charsets.UTF_8)
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val errorMessage = jsonResponse.optString("error", "An unknown error occurred")

                        val userMessage = when (errorMessage) {
                            "You do not have the authority to add an admin" -> "You don't have permission to add an admin."
                            "User is already an admin" -> "This user is already an admin."
                            "User is already registered as an employee" -> "This user is registered as an employee and cannot be an admin."
                            else -> errorMessage
                        }

                        Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show()
                        Log.e("AddAdmin", "Error: $userMessage")
                    } catch (e: Exception) {
                        Log.e("AddAdmin", "Error parsing response: ${e.message}")
                        Toast.makeText(context, "Failed to add admin. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("AddAdmin", "Unknown error: ${error.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(context).add(request)
    }


    private fun removeAdmin(userId: String) {
        val context: Context = this
        val url = "${Constants.SERVER_URL}manageOrganizations/removeAdmin"
        val currentUser = FirebaseManager.getCurrentUserId()

        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
            put("currentUserId", currentUser) // Current user making the request
            put("adminId", userId) // Admin to be removed
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("RemoveAdmin", "Admin removed successfully")
                Toast.makeText(context, "Admin removed successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(context, manageAdmins::class.java)
                intent.putExtra("organization_name", organizationName)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                error.networkResponse?.let { response ->
                    val responseData = String(response.data, Charsets.UTF_8)
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val errorMessage = jsonResponse.optString("error", "An unknown error occurred")

                        if (errorMessage == "You do not have the authority to remove an admin") {
                            Toast.makeText(context, "You don't have permission to remove an admin", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }

                        Log.e("RemoveAdmin", "Error: $errorMessage")
                    } catch (e: Exception) {
                        Log.e("RemoveAdmin", "Error parsing response: ${e.message}")
                        Toast.makeText(context, "Failed to remove admin. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Log.e("RemoveAdmin", "Unknown error: ${error.message}")
                    Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(context).add(request)

        Volley.newRequestQueue(context).add(request)
    }

    private fun fetchAdminsList() {
        val url = "${Constants.SERVER_URL}manageOrganizations/getAdmins"
        val requestBody = JSONObject().apply {
            put("organizationName", organizationName)
        }

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONObject(response).getJSONArray("admins")
                    val adminsList = mutableListOf<UserData>()

                    for (i in 0 until jsonArray.length()) {
                        val userJson = jsonArray.getJSONObject(i)
                        val user = UserData(
                            userId = userJson.getString("userId"),
                            fullname = userJson.getString("fullname"),
                            gamerTag = userJson.optString("gamerTag", ""),
                            profilePicture = userJson.optString("profilePicture", null)
                        )
                        adminsList.add(user)
                    }

                    manageAdminsAdapter.updateAdminsList(adminsList)
                } catch (e: Exception) {
                    Log.e("FetchAdmins", "Error parsing response: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("FetchAdmins", "Error fetching admins: ${error.message}")
            }
        ) {
            override fun getBody() = requestBody.toString().toByteArray(Charsets.UTF_8)
            override fun getHeaders() = mutableMapOf("Content-Type" to "application/json")
        }

        Volley.newRequestQueue(this).add(request)
    }

}
