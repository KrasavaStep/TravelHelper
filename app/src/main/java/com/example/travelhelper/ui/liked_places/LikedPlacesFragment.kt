package com.example.travelhelper.ui.liked_places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelhelper.MainActivity
import com.example.travelhelper.databinding.FragmentLikedPlacesBinding

class LikedPlacesFragment : Fragment(), MainActivity.MenuConfig {

    private var _binding: FragmentLikedPlacesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val likedPlacesViewModel =
            ViewModelProvider(this).get(LikedPlacesViewModel::class.java)

        _binding = FragmentLikedPlacesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        likedPlacesViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setMenuConfig(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setMenuConfig(null)
    }

    override fun shouldShowMenuItems(menu: Menu): Boolean = false
}