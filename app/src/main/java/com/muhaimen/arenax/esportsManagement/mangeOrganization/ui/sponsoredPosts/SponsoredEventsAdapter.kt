package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.sponsoredPosts

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Event
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents.viewEventDetails

class SponsoredEventsAdapter(private var eventsList: MutableList<Event>) :
    RecyclerView.Adapter<SponsoredEventsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventBanner: ImageView = itemView.findViewById(R.id.eventBanner)
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitleTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val eventMode: TextView = itemView.findViewById(R.id.eventModeTextView)
        val platform: TextView = itemView.findViewById(R.id.eventPlatformTextView)
        val gameName:TextView = itemView.findViewById(R.id.eventGameTextView)

        fun bind(event: Event) {
            eventTitle.text = event.eventName
            eventLocation.text = event.location ?: "Location Not Provided"
            eventMode.text = event.eventMode
            platform.text = event.platform
            gameName.text=event.gameName

            // Load event banner using Glide
            if (!event.eventBanner.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(event.eventBanner) // Corrected to load actual event banner
                    .placeholder(R.drawable.battlegrounds_icon_background)
                    .into(eventBanner)
            } else {
                eventBanner.setImageResource(R.drawable.battlegrounds_icon_background)
            }

            // Open event details on click
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, viewEventDetails::class.java).apply {
                    putExtra("eventName", event.eventName)
                    putExtra("gameName", event.gameName)
                    putExtra("eventLocation", event.location)
                    putExtra("eventMode", event.eventMode)
                    putExtra("eventBanner", event.eventBanner)
                    putExtra("eventPlatform", event.platform)
                    putExtra("eventDetails", event.eventDescription)
                    putExtra("startTime", event.startTime)
                    putExtra("endTime", event.endTime)
                    putExtra("startDate", event.startDate)
                    putExtra("endDate", event.endDate)
                    putExtra("eventID", event.eventId)
                    putExtra("organizationId", event.organizationId)
                    putExtra("eventLink", event.eventLink)
                    putExtra("loadedFrom", "ownOrganization")
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sponsor_event_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    override fun getItemCount(): Int = eventsList.size

    // Function to update event list dynamically
    fun updateData(newEvents: List<Event>) {
        eventsList.clear()
        eventsList.addAll(newEvents)
        notifyDataSetChanged()
    }
}
