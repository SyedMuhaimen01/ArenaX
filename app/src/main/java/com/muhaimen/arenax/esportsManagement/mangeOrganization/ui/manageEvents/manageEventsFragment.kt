package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents.schedulingEvent.schedulingEvent

class manageEventsFragment : Fragment() {
    private lateinit var scheduleEventButton:FloatingActionButton
    companion object {
        fun newInstance() = manageEventsFragment()
    }

    private val viewModel: ManageEventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_manage_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleEventButton=view.findViewById(R.id.postButton)
        scheduleEventButton.setOnClickListener {
            val intent= Intent(activity, schedulingEvent::class.java)
            startActivity(intent)
        }
    }
}