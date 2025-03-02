package com.muhaimen.arenax.esportsManagement.talentExchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Job

class SearchEmployeesAdapter (
    private var jobsList: List<Job>
) : RecyclerView.Adapter<SearchEmployeesAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_job_item, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val job = jobsList[position]
        holder.bind(job)
    }

    override fun getItemCount(): Int = jobsList.size

    // ViewHolder class
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTextView)

        fun bind(job: Job) {
            jobTitle.text = job.jobTitle
        }
    }
}
