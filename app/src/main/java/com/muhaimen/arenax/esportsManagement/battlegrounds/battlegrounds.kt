package com.muhaimen.arenax.esportsManagement.battlegrounds

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsports
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.explore.SearchUserAdapter


class battlegrounds : AppCompatActivity() {
    private lateinit var talentExchangeButton : ImageView
    private lateinit var battlegroundsButton : ImageView
    private lateinit var switchButton : ImageView
    private lateinit var exploreButton : ImageView
    private lateinit var profileButton : ImageView
    private lateinit var searchbar: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var battlegroundsAdapter: BattlegroundsAdapter
    private lateinit var searchEventsAdapter: SearchEventsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_battlegrounds)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        // button listeners initialization
        talentExchangeButton = findViewById(R.id.talentExchangeButton)
        battlegroundsButton = findViewById(R.id.battlegroundsButton)
        switchButton = findViewById(R.id.switchButton)
        exploreButton = findViewById(R.id.exploreButton)
        profileButton = findViewById(R.id.profileButton)
        eventsRecyclerView = findViewById(R.id.events_recyclerview)
        searchRecyclerView= findViewById(R.id.searchEventsRecyclerView)

        // search bar initialization
        searchbar = findViewById(R.id.searchbar)

        val eventsAdapter = BattlegroundsAdapter(emptyList())
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = eventsAdapter

        val searchUserAdapter = SearchEventsAdapter(emptyList())
        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchUserAdapter
        searchbar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                eventsRecyclerView.visibility = View.GONE
                searchRecyclerView.visibility = View.VISIBLE
            }
        }

        searchbar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.trim() ?: ""
                if (searchText.isNotEmpty()) {

                } else {

                }
            }
        })



        talentExchangeButton.setOnClickListener {
            val intent = Intent(this, talentExchange::class.java)
            startActivity(intent)
        }

        battlegroundsButton.setOnClickListener {
            val intent = Intent(this, battlegrounds::class.java)
            startActivity(intent)
        }

        switchButton.setOnClickListener {
            val intent = Intent(this, switchToEsports::class.java)
            startActivity(intent)
        }

        exploreButton.setOnClickListener {
            val intent = Intent(this, exploreEsports::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, esportsProfile::class.java)
            startActivity(intent)
        }

    }
}