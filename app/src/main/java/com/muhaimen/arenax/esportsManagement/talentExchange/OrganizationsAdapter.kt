package com.muhaimen.arenax.esportsManagement.talentExchange

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.viewJobDetails

class OrganizationsAdapter(private var jobsList: MutableList<Job>) :
    RecyclerView.Adapter<OrganizationsAdapter.ViewHolder>() {

    private var organizationName: String? = null
    private var organizationLogoUrl: String? = null

    // Method to update organization data
    fun setOrganizationData(name: String, logoUrl: String) {
        organizationName = name
        organizationLogoUrl = logoUrl
        notifyDataSetChanged()  // Ensure UI updates when organization data is set
    }

    // ViewHolder class for binding job data to views
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTextView)
        val jobLocation: TextView = itemView.findViewById(R.id.locationTextView)
        val jobType: TextView = itemView.findViewById(R.id.jobTypeTextView)
        val organizationNameTextView: TextView = itemView.findViewById(R.id.organizationNameTextView)
        val organizationLogo: ImageView = itemView.findViewById(R.id.organizationLogo)
        val workplaceType: TextView = itemView.findViewById(R.id.workplaceTypeTextView)
        val tag1: TextView = itemView.findViewById(R.id.tag1)
        val tag2: TextView = itemView.findViewById(R.id.tag2)
        val tag3: TextView = itemView.findViewById(R.id.tag3)
        val tag4: TextView = itemView.findViewById(R.id.tag4)

        fun bind(job: Job) {
            jobTitle.text = job.jobTitle
            jobLocation.text = job.jobLocation
            jobType.text = job.jobType
            organizationNameTextView.text = organizationName ?: "Unknown Organization"
            workplaceType.text = job.workplaceType

            // Load Organization Logo Only If URL Exists
            if (!organizationLogoUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(organizationLogoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.battlegrounds_icon_background)
                    .into(organizationLogo)
            } else {
                organizationLogo.setImageResource(R.drawable.battlegrounds_icon_background)
            }

            val tags = job.tags
            tag1.text = tags.getOrNull(0) ?: ""
            tag2.text = tags.getOrNull(1) ?: ""
            tag3.text = tags.getOrNull(2) ?: ""
            tag4.text = tags.getOrNull(3) ?: ""

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, viewJobDetails::class.java).apply {
                    putExtra("JobTitle", job.jobTitle)
                    putExtra("JobLocation", job.jobLocation)
                    putExtra("JobType", job.jobType)
                    putExtra("OrganizationName", organizationName)
                    putExtra("OrganizationLogoUrl", organizationLogoUrl)
                    putExtra("WorkplaceType", job.workplaceType)
                    putExtra("JobDescription", job.jobDescription)
                    putStringArrayListExtra("JobTags", ArrayList(job.tags))
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    // Creates new ViewHolder instances for the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.organization_job_posting_item, parent, false)
        return ViewHolder(view)
    }

    // Binds job data to each ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(jobsList[position])
    }

    // Returns the number of job items in the list
    override fun getItemCount(): Int {
        return jobsList.size
    }

    // Method to update the list of jobs when new data is fetched
    fun updateJobsList(newJobsList: MutableList<Job>) {
        jobsList = newJobsList
        notifyDataSetChanged()
    }
}
