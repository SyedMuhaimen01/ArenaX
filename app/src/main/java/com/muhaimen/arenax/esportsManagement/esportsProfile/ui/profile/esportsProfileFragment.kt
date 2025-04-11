package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.dataClasses.Team
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.esportsManagement.battlegrounds.battlegrounds
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myOrganizations.MyOrganizationsAdapter
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.myTeams.MyTeamsAdapter
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsports
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.userProfile.UserProfile
import com.muhaimen.arenax.userProfile.otherUserEsportsProfile.otherUserOrganizationsAdapter
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import org.json.JSONArray
import org.json.JSONObject

class esportsProfileFragment : Fragment() {

    private lateinit var talentExchangeButton : LinearLayout
    private lateinit var battlegroundsButton : LinearLayout
    private lateinit var switchButton : LinearLayout
    private lateinit var exploreButton : LinearLayout
    private lateinit var profileButton : LinearLayout
    private lateinit var teamsRecyclerView: RecyclerView
    private lateinit var teamsAdapter: MyTeamsAdapter
    private lateinit var organizationsRecyclerView: RecyclerView
    private lateinit var bioTextView: TextView
    private lateinit var showMoreTextView: TextView
    private lateinit var profileImage: ImageView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var organizationsAdapter: otherUserOrganizationsAdapter
    private var teamsList: MutableList<Team> = mutableListOf()
    private val organizationList = mutableListOf<OrganizationData>()
    private lateinit var currentUserId:String
    companion object {
        fun newInstance() = esportsProfileFragment()
    }

    private val viewModel: EsportsProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_esports_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // button listeners initialization
        talentExchangeButton =view.findViewById(R.id.talentExchangeButton)
        battlegroundsButton = view.findViewById(R.id.battlegroundsButton)
        switchButton = view.findViewById(R.id.esportsButton)
        exploreButton = view.findViewById(R.id.searchButton)
        profileButton = view.findViewById(R.id.profileButton)
        showMoreTextView = view.findViewById(R.id.showMore)
        bioTextView = view.findViewById(R.id.bioText)
        profileImage = view.findViewById(R.id.profilePicture)
        teamsRecyclerView = view.findViewById(R.id.teamsRecyclerView)
        teamsRecyclerView.layoutManager = LinearLayoutManager(context)
        teamsAdapter = MyTeamsAdapter(teamsList)
        teamsRecyclerView.adapter = teamsAdapter

        organizationsRecyclerView = view.findViewById(R.id.organizationsRecyclerView)
        organizationsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        organizationsAdapter = otherUserOrganizationsAdapter(organizationList)
        organizationsRecyclerView.adapter = organizationsAdapter

        currentUserId=FirebaseManager.getCurrentUserId().toString()

        val firebaseUid = FirebaseManager.getCurrentUserId().toString()
        fetchTeams(firebaseUid)
        fetchOrganizations()
        fetchUserDetailsFromFirebase(view)
        talentExchangeButton.setOnClickListener {
            val intent = Intent(context, talentExchange::class.java)
            startActivity(intent)
        }

        battlegroundsButton.setOnClickListener {
            val intent = Intent(context, battlegrounds::class.java)
            startActivity(intent)
        }

        switchButton.setOnClickListener {
            val intent = Intent(context, switchToEsports::class.java)
            intent.putExtra("loadedFromActivity", "esports")
            startActivity(intent)
        }

        exploreButton.setOnClickListener {
            val intent = Intent(context, exploreEsports::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(context, esportsProfile::class.java)
            startActivity(intent)
        }
    }

    private fun fetchTeams(firebaseUid: String) {
        val url = "${Constants.SERVER_URL}manageTeams/myTeams/$firebaseUid"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val teamsJsonArray: JSONArray = response.getJSONArray("teams")
                    val teamsList = mutableListOf<Team>()

                    // Parsing the JSON array
                    for (i in 0 until teamsJsonArray.length()) {
                        val teamJsonObject: JSONObject = teamsJsonArray.getJSONObject(i)
                        val team = Team(
                            teamName = teamJsonObject.getString("team_name"),
                            gameName = teamJsonObject.getString("game_name"),
                            teamDetails = "",  // You can modify this based on the API response
                            teamLocation = "",  // Same here
                            teamEmail = "",  // Same here
                            teamCaptain = "",  // Same here
                            teamTagLine = "",  // Same here
                            teamAchievements = "",  // Same here
                            teamLogo = teamJsonObject.getString("team_logo"),
                            teamMembers = null // Adjust based on response if needed
                        )
                        teamsList.add(team)
                    }

                    // Populating the adapter with the data
                    teamsAdapter.updateData(teamsList)

                } catch (e: Exception) {
                    Log.e("FetchTeams", "Error parsing response", e)
                }
            },
            { error ->
                Log.e("FetchTeams", "Error: ${error.message}")
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchUserDetailsFromFirebase(view: View) {
        databaseReference =
            FirebaseManager.getCurrentUserId()
                ?.let { FirebaseDatabase.getInstance().getReference("userData").child(it) }!!
        val userId = FirebaseManager.getCurrentUserId()
        userId?.let { uid ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(UserData::class.java) ?: UserData()
                        view.findViewById<TextView>(R.id.fullNameTextView).setText(userData.fullname)
                        view.findViewById<TextView>(R.id.gamerTagTextView).setText(userData.gamerTag)
                        view.findViewById<TextView>(R.id.bioText).setText(userData.bio)
                        val bio=userData.bio
                        if (bio != null) {
                            if (bio.length > 50) {
                                showMoreTextView.visibility = View.VISIBLE
                            }
                        }
                        showMoreTextView.setOnClickListener {
                            bioTextView.maxLines = Int.MAX_VALUE
                            bioTextView.ellipsize = null
                            showMoreTextView.visibility = View.GONE
                        }
                        userData.profilePicture?.let { url ->
                            Glide.with(this@esportsProfileFragment)
                                .load(url)
                                .circleCrop()
                                .into(profileImage)
                        }
                    } else {
                        Log.e("UserProfile", "No data found for user ID: $uid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfile", "Database error: ${error.message}")
                }
            })
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fetchOrganizations() {
        val url = "${Constants.SERVER_URL}registerOrganization/user/organizations"
        val requestQueue = Volley.newRequestQueue(requireContext())

        // Create JSON body
        val requestBody = JSONObject().apply {
            put("firebaseUid", currentUserId) // Send firebaseUid in the body
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response: JSONObject ->
                organizationList.clear() // Clear previous data

                val organizationsArray = response.optJSONArray("organizations") ?: JSONArray()
                for (i in 0 until organizationsArray.length()) {
                    val orgObject = organizationsArray.getJSONObject(i)
                    val organization = OrganizationData(
                        organizationId = orgObject.getString("organization_id"),
                        organizationName = orgObject.getString("organization_name"),
                        organizationLogo = orgObject.optString("organization_logo", null),
                        organizationLocation = orgObject.optString("organization_location", null)
                    )
                    organizationList.add(organization)
                }

                organizationsAdapter.notifyDataSetChanged() // Refresh RecyclerView
            },
            { error ->
                Toast.makeText(requireContext(), "Failed to fetch data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}