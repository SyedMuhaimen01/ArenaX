package com.muhaimen.arenax.esportsManagement.OtherOrganization

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.JobWithOrganization
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.OpenJobDetails
import com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs.viewJobDetails

class otherOrganizationJobsAdapter(
    private var jobWithOrgList: MutableList<JobWithOrganization>
) : RecyclerView.Adapter<otherOrganizationJobsAdapter.ViewHolder>() {

    // Method to update the dataset with new jobs
    fun updateData(newJobWithOrgList: List<JobWithOrganization>) {
        notifyDataSetChanged()
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTextView)
        val jobLocation: TextView = itemView.findViewById(R.id.locationTextView)
        val jobType: TextView = itemView.findViewById(R.id.jobTypeTextView)
        val organizationNameTextView: TextView =
            itemView.findViewById(R.id.organizationNameTextView)
        val organizationLogo: ImageView = itemView.findViewById(R.id.organizationLogo)
        val workplaceType: TextView = itemView.findViewById(R.id.workplaceTypeTextView)
        val tag1: TextView = itemView.findViewById(R.id.tag1)
        val tag2: TextView = itemView.findViewById(R.id.tag2)
        val tag3: TextView = itemView.findViewById(R.id.tag3)
        val tag4: TextView = itemView.findViewById(R.id.tag4)

        fun bind(jobWithOrg: JobWithOrganization) {
            val job = jobWithOrg.job
            val organization = jobWithOrg.organization

            // Bind job data
            jobTitle.text = job.jobTitle
            jobLocation.text = job.jobLocation
            jobType.text = job.jobType
            workplaceType.text = job.workplaceType

            // Bind organization data
            organizationNameTextView.text = organization.organizationName
            if (!organization.organizationLogo.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(organization.organizationLogo)
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
                    putExtra("JobId", job.jobId)
                    putExtra("JobTitle", job.jobTitle)
                    putExtra("JobLocation", job.jobLocation)
                    putExtra("JobType", job.jobType)
                    putExtra("WorkplaceType", job.workplaceType)
                    putExtra("JobDescription", job.jobDescription)
                    putStringArrayListExtra("JobTags", ArrayList(job.tags))
                    putExtra("OrganizationId", job.organizationId)
                    putExtra("OrganizationName", organization.organizationName)
                    putExtra("OrganizationLogo", organization.organizationLogo)
                    putExtra("OrganizationLocation", organization.organizationLocation)
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.organization_job_posting_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(jobWithOrgList[position])
    }

    override fun getItemCount(): Int {
        return jobWithOrgList.size
    }
}
