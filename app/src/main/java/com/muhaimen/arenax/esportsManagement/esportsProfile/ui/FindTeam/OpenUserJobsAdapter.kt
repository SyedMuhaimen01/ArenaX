package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.Jobs

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
import com.muhaimen.arenax.esportsManagement.esportsProfile.ui.FindTeam.ViewOpenRecruitmentAdDetails

class OpenUserJobsAdapter(
    private var jobWithUserDetailsList: MutableList<JobWithUserDetails>
) : RecyclerView.Adapter<OpenUserJobsAdapter.ViewHolder>() {

    // Method to update the dataset with new jobs
    fun updateData(newJobWithUserDetailsList: List<JobWithUserDetails>) {
        jobWithUserDetailsList.clear()
        jobWithUserDetailsList.addAll(newJobWithUserDetailsList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTextView)
        val jobLocation: TextView = itemView.findViewById(R.id.locationTextView)
        val jobType: TextView = itemView.findViewById(R.id.jobTypeTextView)
        val userNameTextView: TextView = itemView.findViewById(R.id.organizationNameTextView)
        val userProfilePicture: ImageView = itemView.findViewById(R.id.organizationLogo)
        val workplaceType: TextView = itemView.findViewById(R.id.workplaceTypeTextView)
        val tag1: TextView = itemView.findViewById(R.id.tag1)
        val tag2: TextView = itemView.findViewById(R.id.tag2)
        val tag3: TextView = itemView.findViewById(R.id.tag3)
        val tag4: TextView = itemView.findViewById(R.id.tag4)

        fun bind(jobWithUserDetails: JobWithUserDetails) {
            val job = jobWithUserDetails.job
            val user = jobWithUserDetails.user

            // Bind job data
            jobTitle.text = job.jobTitle
            jobLocation.text = job.jobLocation
            jobType.text = job.jobType
            workplaceType.text = job.workplaceType

            // Bind user data
            userNameTextView.text = user?.fullname ?: "Unknown User"
            if (!user?.profilePicture.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(user?.profilePicture)
                    .circleCrop()
                    .placeholder(R.drawable.battlegrounds_icon_background)
                    .into(userProfilePicture)
            } else {
                userProfilePicture.setImageResource(R.drawable.battlegrounds_icon_background)
            }

            // Bind tags
            val tags = job.tags
            tag1.text = tags.getOrNull(0) ?: ""
            tag2.text = tags.getOrNull(1) ?: ""
            tag3.text = tags.getOrNull(2) ?: ""
            tag4.text = tags.getOrNull(3) ?: ""

            // Set click listener for item view
            itemView.setOnClickListener {
                val intent =
                    Intent(itemView.context, ViewOpenRecruitmentAdDetails::class.java).apply {
                        // Pass Job Details
                        putExtra("JobId", job.jobId)
                        putExtra("JobTitle", job.jobTitle)
                        putExtra("JobLocation", job.jobLocation)
                        putExtra("JobType", job.jobType)
                        putExtra("WorkplaceType", job.workplaceType)
                        putExtra("JobDescription", job.jobDescription)
                        putStringArrayListExtra("JobTags", ArrayList(job.tags))
                        if(user!=null) {
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
        holder.bind(jobWithUserDetailsList[position])
    }

    override fun getItemCount(): Int {
        return jobWithUserDetailsList.size
    }
}