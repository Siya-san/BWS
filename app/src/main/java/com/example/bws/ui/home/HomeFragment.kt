package com.example.bws.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.bws.MainActivity
import com.example.myapplication2.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root
binding.create.setOnClickListener {
    val mainActivity = activity as MainActivity
    mainActivity.goToSightingCapture()}

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}