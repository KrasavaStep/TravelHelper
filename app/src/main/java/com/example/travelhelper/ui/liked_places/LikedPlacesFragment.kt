package com.example.travelhelper.ui.liked_places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelhelper.MainActivity
import com.example.travelhelper.R
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.data_model.RouteModel
import com.example.travelhelper.databinding.FragmentLikedPlacesBinding
import com.example.travelhelper.utils.StripedBackgroundDecoration
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class LikedPlacesFragment : Fragment(R.layout.fragment_liked_places), MainActivity.MenuConfig {

    private var _binding: FragmentLikedPlacesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LikedPlacesListAdapter
    private lateinit var routesAdapter: RoutesAdapter

    private val likedViewModel by viewModel<LikedPlacesViewModel>(named("likedViewModel"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLikedPlacesBinding.bind(view)

        setupAdapters()
        setupBottomNavigation()
        setupObservers()

        likedViewModel.getLikedAttractions()
        likedViewModel.getRoutes()
    }

    private fun setupAdapters() {
        adapter = LikedPlacesListAdapter(object : LikedPlacesListAdapter.ItemClickListener {
            override fun onAttractionClicked(item: AttractionModel) {
                // Логика клика по достопримечательности
            }
        })

        routesAdapter = RoutesAdapter(object : RoutesAdapter.ItemClickListener {
            override fun onRouteClicked(item: RouteModel) {
                val action = LikedPlacesFragmentDirections.actionNavLikedPlacesToNavMainMap(
                    id = item.id,
                    route = item
                )
                findNavController().navigate(action)
            }
        })

        val decorator = StripedBackgroundDecoration(
            evenColor = requireContext().getColor(R.color.white),
            oddColor = requireContext().getColor(R.color.gray)
        )

        binding.recyclerLiked.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLiked.adapter = adapter
        binding.recyclerLiked.addItemDecoration(decorator)

        binding.recyclerRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRoutes.adapter = routesAdapter
        binding.recyclerRoutes.addItemDecoration(decorator)
    }

    private fun setupBottomNavigation() {
        // Логика для кнопки ДОМОЙ в нижней панели
        view?.findViewById<View>(R.id.btn_home)?.setOnClickListener {
            findNavController().navigate(R.id.nav_main_map)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                likedViewModel.likedAttractionState.collect { state ->
                    when (state) {
                        is LikedPlacesViewModel.LikedAttractionsUiState.Empty -> {
                            binding.emptyAlert.text = state.emptyAlert
                            binding.emptyAlert.visibility = View.VISIBLE
                        }
                        is LikedPlacesViewModel.LikedAttractionsUiState.Success -> {
                            binding.emptyAlert.visibility = View.GONE
                            adapter.setData(state.attractions)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                likedViewModel.routesState.collect { state ->
                    if (state is LikedPlacesViewModel.RoutesUiState.Success) {
                        routesAdapter.setData(state.routes)
                    }
                }
            }
        }
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
