package com.muhaimen.arenax.esportsManagement.esportsProfile.ui.profile

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.battlegrounds.battlegrounds
import com.muhaimen.arenax.esportsManagement.esportsProfile.esportsProfile
import com.muhaimen.arenax.esportsManagement.exploreEsports.exploreEsports
import com.muhaimen.arenax.esportsManagement.switchToEsports.switchToEsports
import com.muhaimen.arenax.esportsManagement.talentExchange.talentExchange
import com.muhaimen.arenax.userProfile.UserProfile

class esportsProfileFragment : Fragment() {

    private lateinit var talentExhangeButton : ImageView
    private lateinit var battlegroundsButton : ImageView
    private lateinit var switchButton : ImageView
    private lateinit var exploreButton : ImageView
    private lateinit var profileButton : ImageView

    companion object {
        fun newInstance() = esportsProfileFragment()
    }

    private val viewModel: EsportsProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
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
        talentExhangeButton =view.findViewById(R.id.talentExchangeButton)
        battlegroundsButton = view.findViewById(R.id.battlegroundsButton)
        switchButton = view.findViewById(R.id.switchButton)
        exploreButton = view.findViewById(R.id.exploreButton)
        profileButton = view.findViewById(R.id.profileButton)

        talentExhangeButton.setOnClickListener {
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
}