package com.muhaimen.arenax.esportsManagement.battlegrounds

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
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting.viewRecruitmentDetails

class BattlegroundsAdapter (private val eventsList: List<Event>) :
    RecyclerView.Adapter<BattlegroundsAdapter.ViewHolder>() {



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitleTextView)
        val eventLocation: TextView = itemView.findViewById(R.id.locationTextView)
        val eventBanner: ImageView = itemView.findViewById(R.id.eventBanner)
        val eventMode: TextView = itemView.findViewById(R.id.eventModeTextView)
        val platform: TextView = itemView.findViewById(R.id.eventPlatformTextView)

        fun bind(event: Event) {
            eventTitle.text = event.eventName
            eventLocation.text = event.location
            eventMode.text = event.eventMode
            platform.text = event.platform


            // ✅ Load Organization Logo Only If URL Exists
            if (!event.eventBanner.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(event.eventBanner)
                    .circleCrop()
                    .placeholder(R.drawable.battlegrounds_icon_background)
                    .into(eventBanner)
            } else {
                eventBanner.setImageResource(R.drawable.battlegrounds_icon_background)
            }



            itemView.setOnClickListener {
                val intent = Intent(itemView.context, viewRecruitmentDetails::class.java).apply {
                    putExtra("eventName", event.eventName)
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
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recruitment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventsList[position])
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }
}
