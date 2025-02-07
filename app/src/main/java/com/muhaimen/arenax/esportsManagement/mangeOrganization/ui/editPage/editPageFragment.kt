package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.editPage

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muhaimen.arenax.R

class editPageFragment : Fragment() {

    companion object {
        fun newInstance() = editPageFragment()
    }

    private val viewModel: EditPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_page, container, false)
    }
}