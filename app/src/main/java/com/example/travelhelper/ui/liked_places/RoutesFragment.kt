package com.example.travelhelper.ui.liked_places

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelhelper.MainActivity
import com.example.travelhelper.R
import com.example.travelhelper.data.data_model.RouteModel
import com.example.travelhelper.databinding.FragmentRoutesBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class RoutesFragment : Fragment(R.layout.fragment_routes) {

    private var _binding: FragmentRoutesBinding? = null
    private val binding get() = _binding!!
    private lateinit var routesAdapter: RoutesAdapter
    private val likedViewModel by viewModel<LikedPlacesViewModel>(named("likedViewModel"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoutesBinding.bind(view)

        setupAdapter()
        setupClickListeners()
        setupObservers()

        likedViewModel.getRoutes()
    }

    private fun setupAdapter() {
        routesAdapter = RoutesAdapter(object : RoutesAdapter.ItemClickListener {
            override fun onRouteClicked(item: RouteModel) {
                val action = RoutesFragmentDirections.actionNavRoutesToNavMainMap(
                    id = item.id,
                    route = item
                )
                findNavController().navigate(action)
            }
        })

        binding.recyclerRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRoutes.adapter = routesAdapter
    }

    private fun setupClickListeners() {
        // Кнопка возврата к карте
        binding.btnBackToMap.setOnClickListener {
            findNavController().navigate(R.id.nav_main_map)
        }
    }

    private fun setupObservers() {
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
}
