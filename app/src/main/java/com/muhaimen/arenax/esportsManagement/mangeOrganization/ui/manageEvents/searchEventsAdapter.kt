package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event

class searchEventsAdapter(private var eventsList: List<Event>) :
    RecyclerView.Adapter<searchEventsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_job_item, parent, false) // Ensure this layout exists
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventsList[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = eventsList.size

    // ViewHolder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitle: TextView = itemView.findViewById(R.id.jobTitleTextView)

        fun bind(event: Event) {
            eventTitle.text = event.eventName
        }
    }
}
