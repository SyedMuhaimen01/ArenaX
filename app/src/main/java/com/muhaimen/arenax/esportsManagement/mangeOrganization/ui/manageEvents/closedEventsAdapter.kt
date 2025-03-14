package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.manageEvents

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

class closedEventsAdapter(private val eventsList: List<Event>) :
    RecyclerView.Adapter<closedEventsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventBanner: ImageView = itemView.findViewById(R.id.eventBanner)
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitleTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.eventLocationTextView)
        val eventMode: TextView = itemView.findViewById(R.id.eventModeTextView)
        val platform: TextView = itemView.findViewById(R.id.eventPlatformTextView)

        fun bind(event: Event) {
            eventTitle.text = event.eventName
            eventLocation.text = event.location
            eventMode.text = event.eventMode
            platform.text = event.platform
            if(event.eventBanner?.isNotEmpty() == true) {
                Glide.with(itemView.context)
                    .load(eventBanner)
                    .placeholder(R.drawable.battlegrounds_icon_background)
                    .into(eventBanner)
            } else {
                eventBanner.setImageResource(R.drawable.battlegrounds_icon_background)
            }

            itemView.setOnClickListener {
                val intent= Intent(itemView.context, viewEventDetails::class.java)
                intent.putExtra("event", event)

                itemView.context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    override fun getItemCount(): Int = eventsList.size
}
