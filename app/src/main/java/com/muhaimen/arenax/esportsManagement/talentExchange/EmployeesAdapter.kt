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
import com.muhaimen.arenax.dataClasses.JobWithUserDetails
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.recruitmentAdPosting.viewRecruitmentDetails

class EmployeesAdapter(private var jobsList: MutableList<JobWithUserDetails>,
                       private var organization: OrganizationData?) :
    RecyclerView.Adapter<EmployeesAdapter.ViewHolder>() {

    // Function to update job list and notify adapter
    fun updateJobList(newJobs: List<JobWithUserDetails>) {
        notifyDataSetChanged() // Notify RecyclerView of data changes
    }

    // Function to update the organization and refresh the adapter
    fun updateOrganization(newOrganization: OrganizationData?) {
        organization = newOrganization
        notifyDataSetChanged() // Refresh the adapter to reflect the new organization
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Job Details Views
        val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTextView)
        val jobLocation: TextView = itemView.findViewById(R.id.locationTextView)
        val jobType: TextView = itemView.findViewById(R.id.jobTypeTextView)
        val workPlace:TextView = itemView.findViewById(R.id.workplaceTypeTextView)
        // User Details Views
        val employeeProfilePicture: ImageView = itemView.findViewById(R.id.organizationLogo)
        val fullName: TextView = itemView.findViewById(R.id.organizationNameTextView)

        // Tags Views
        val tag1: TextView = itemView.findViewById(R.id.tag1)
        val tag2: TextView = itemView.findViewById(R.id.tag2)
        val tag3: TextView = itemView.findViewById(R.id.tag3)
        val tag4: TextView = itemView.findViewById(R.id.tag4)

        // Bind data to views
        fun bind(jobWithUserDetails: JobWithUserDetails) {
            val job = jobWithUserDetails.job
            val user = jobWithUserDetails.user

            // Bind Job Details
            jobTitle.text = job.jobTitle
            jobLocation.text = job.jobLocation
            jobType.text = job.jobType
            workPlace.text = job.workplaceType
            // Bind User Details
            if (user != null) {
                fullName.text = user.fullname

                Glide.with(itemView.context)
                    .load(user.profilePicture) // Load profile picture
                    .circleCrop()
                    .placeholder(R.drawable.battlegrounds_icon_background) // Placeholder image
                    .into(employeeProfilePicture)

                // Bind Tags
                val tags = job.tags
                tag1.text = tags.getOrNull(0) ?: ""
                tag2.text = tags.getOrNull(1) ?: ""
                tag3.text = tags.getOrNull(2) ?: ""
                tag4.text = tags.getOrNull(3) ?: ""

                // Set Click Listener to Open Job Details
                itemView.setOnClickListener {
                    val intent =
                        Intent(itemView.context, viewRecruitmentDetails::class.java).apply {
                            // Pass Job Details
                            putExtra("JobId", job.jobId)
                            putExtra("JobTitle", job.jobTitle)
                            putExtra("JobLocation", job.jobLocation)
                            putExtra("JobType", job.jobType)
                            putExtra("WorkplaceType", job.workplaceType)
                            putExtra("JobDescription", job.jobDescription)
                            putStringArrayListExtra("JobTags", ArrayList(job.tags))

                            organization?.let { org ->
                                putExtra("OrganizationId", org.organizationId)
                                putExtra("OrganizationName", org.organizationName)
                                putExtra("OrganizationLogo", org.organizationLogo)
                                putExtra("OrganizationLocation", org.organizationLocation)
                            }

                            // Pass User Details
                            putExtra("UserId", user.userId)
                            putExtra("FullName", user.fullname)
                            putExtra("GamerTag", user.gamerTag)
                            putExtra("ProfilePictureUrl", user.profilePicture)
                            putExtra("Gender", user.gender.toString())
                            putExtra("Bio", user.bio)
                            putExtra("Location", user.location)
                            putExtra("AccountVerified", user.accountVerified)
                            putExtra("PlayerId", user.playerId)
                            putExtra("Rank", user.rank)
                        }
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    // Inflate the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.organization_job_posting_item, parent, false)
        return ViewHolder(view)
    }

    // Bind data to each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(jobsList[position])
    }

    // Return the total number of items
    override fun getItemCount(): Int {
        return jobsList.size
    }
}