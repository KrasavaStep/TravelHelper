package com.example.travelhelper.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.travelhelper.MainActivity
import com.example.travelhelper.R
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.network.overpass_api.OSMPlace
import com.example.travelhelper.data.network.routes_api.LatLng
import com.example.travelhelper.data.network.routes_api.Route
import com.example.travelhelper.databinding.FragmentMainMapBinding
import com.example.travelhelper.ui.bottom_sheet_view.AttractionBottomSheet
import com.example.travelhelper.utils.SharedViewModel
import com.example.travelhelper.utils.Utils.getFromPrefs
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.*
import com.yandex.runtime.image.ImageProvider.fromBitmap
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import com.example.travelhelper.utils.BorderData

class MainMapFragment : Fragment(), MainActivity.MenuConfig {
    private val mainMapviewModel by viewModel<MainMapViewModel>(named("mainMapViewModel"))
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var thisView: View
    private var currentRoute: PolylineMapObject? = null
    private var placemarksCollection: MapObjectCollection? = null
    private val customPlaceMarks = mutableListOf<PlacemarkMapObject>()
    private val args: MainMapFragmentArgs by navArgs()

    private var _binding: FragmentMainMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thisView = view
        // ВАЖНО: используем одну общую SharedViewModel на уровне Activity
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        
        setupMap()
        setupObservers()
        setupClickListeners()

        mainMapviewModel.getBelarusBorder()
        mainMapviewModel.borderLiveData.observe(viewLifecycleOwner) { borderList ->
            borderList.forEach { drawDetailedBelarusBorder(it) }
        }

        if (args.route != null) {
            mainMapviewModel.getCustomPoints(args.id)
            setupCustomPointObserver()
        }

        setupRouteObservers()

        // Слушаем выбор из поиска или карточки
        sharedViewModel.selectedAttraction.observe(viewLifecycleOwner) { attraction ->
            if (attraction != null) {
                showBottomSheet(attraction)
                binding.mapView.mapWindow.map.move(
                    CameraPosition(Point(attraction.latitude, attraction.longitude), 16.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
            }
        }

        // Слушаем команду на построение маршрута (из любой вкладки)
        sharedViewModel.dialogResult.observe(viewLifecycleOwner) { coords ->
            if (coords != null) {
                binding.routeInfoView.visibility = View.GONE
                removeRoute()
                calculateRoute(LatLng(coords[1], coords[0]))
            }
        }

        setupSearch()
    }

    fun resetMapState() {
        if (_binding == null) return
        binding.searchResultsRecycler.visibility = View.GONE
        binding.routeInfoView.visibility = View.GONE
        binding.mapSearchView.setQuery("", false)
        binding.mapSearchView.clearFocus()
        hideKeyboard()
        removeRoute()
        
        val lat = requireContext().getFromPrefs("lat", 52.4171724f).toDouble()
        val lon = requireContext().getFromPrefs("lon", 30.9963954f).toDouble()
        binding.mapView.mapWindow.map.move(
            CameraPosition(Point(lat, lon), 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun setupClickListeners() {
        binding.btnHome.setOnClickListener { resetMapState() }

        binding.fabLocationCustom.setOnClickListener {
            val lat = requireContext().getFromPrefs("lat", 52.4171724f).toDouble()
            val lon = requireContext().getFromPrefs("lon", 30.9963954f).toDouble()
            val currentPoint = Point(lat, lon)
            binding.mapView.mapWindow.map.move(
                CameraPosition(currentPoint, 18.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1.5f),
                null
            )
        }

        binding.btnWorld.setOnClickListener { findNavController().navigate(R.id.nav_liked_places) }
        binding.btnSettings.setOnClickListener { findNavController().navigate(R.id.nav_settings) }
        binding.btnProfile.setOnClickListener {
            val drawer = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer?.openDrawer(GravityCompat.START)
        }

        binding.reloadImg.setOnClickListener {
            mainMapviewModel.loadAttractions(CITY)
            binding.loadingView.visibility = View.VISIBLE
            binding.reloadAttractions.visibility = View.GONE
        }

        binding.closeRouteInfo.setOnClickListener {
            binding.routeInfoView.visibility = View.GONE
            removeRoute()
            if (args.route != null) removeCustomPoints()
        }
    }

    private fun setupSearch() {
        searchAdapter = SearchAdapter { attraction ->
            showBottomSheet(attraction)
            binding.searchResultsRecycler.visibility = View.GONE
            binding.mapSearchView.setQuery("", false)
            binding.mapSearchView.clearFocus()
            hideKeyboard()

            val point = Point(attraction.latitude, attraction.longitude)
            binding.mapView.mapWindow.map.move(
                CameraPosition(point, 16.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
        binding.searchResultsRecycler.adapter = searchAdapter

        binding.mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { mainMapviewModel.searchAttractions(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { mainMapviewModel.searchAttractions(it) }
                return true
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainMapviewModel.searchState.collect { results ->
                    if (results.isNotEmpty()) {
                        searchAdapter.submitList(results)
                        binding.searchResultsRecycler.visibility = View.VISIBLE
                    } else {
                        binding.searchResultsRecycler.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun shouldShowMenuItems(menu: Menu): Boolean = false

    private fun setupMap() {
        val currentPoint = Point(
            requireContext().getFromPrefs("lat", 52.4171724f).toDouble(),
            requireContext().getFromPrefs("lon", 30.9963954f).toDouble()
        )
        binding.mapView.mapWindow.map.move(
            CameraPosition(currentPoint, 18.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
        binding.mapView.mapWindow.map.isRotateGesturesEnabled = true
        binding.mapView.mapWindow.map.isZoomGesturesEnabled = true
        binding.mapView.mapWindow.map.isScrollGesturesEnabled = true
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainMapviewModel.loadAttractions(CITY)
                mainMapviewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MainMapViewModel.AttractionsUiState.Success -> {
                            binding.reloadAttractions.visibility = View.GONE
                            binding.loadingView.visibility = View.GONE
                            addPlacemark(uiState.attractions)
                            setCurrentLocationPoint()
                            if (args.route != null) placemarksCollection?.isVisible = false
                        }
                        is MainMapViewModel.AttractionsUiState.Loading -> {
                            binding.loadingView.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                        }
                        is MainMapViewModel.AttractionsUiState.Error -> {
                            binding.reloadAttractions.visibility = View.VISIBLE
                            binding.errorMsg.text = uiState.exception.message
                        }
                    }
                }
            }
        }
    }

    private fun setupRouteObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainMapviewModel.routeState.collect { state ->
                    if (state is MainMapViewModel.RouteUiState.Success) {
                        displayRoute(state.route)
                        showRouteInfo(state.route)
                    }
                }
            }
        }
    }

    private fun calculateRoute(destination: LatLng, origin: LatLng? = null, intermediates: List<LatLng>? = null) {
        val startPoint = origin ?: LatLng(
            requireContext().getFromPrefs("lat", 52.4171724f).toDouble(),
            requireContext().getFromPrefs("lon", 30.9963954f).toDouble()
        )
        mainMapviewModel.calculateRouteResponse(startPoint, destination, intermediates)
    }

    private fun displayRoute(route: Route) {
        val decodedPath = mainMapviewModel.decodePolyline(route)
        val polyline = Polyline(decodedPath)
        currentRoute = binding.mapView.mapWindow.map.mapObjects.addPolyline(polyline)
        currentRoute?.apply {
            strokeWidth = 5f
            setStrokeColor(ContextCompat.getColor(requireContext(), R.color.blue))
            outlineWidth = 1f
            outlineColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        if (decodedPath.isNotEmpty()) {
            val cameraPosition = binding.mapView.mapWindow.map.cameraPosition(Geometry.fromPolyline(polyline))
            binding.mapView.mapWindow.map.move(
                CameraPosition(cameraPosition.target, cameraPosition.zoom - 0.8f, cameraPosition.azimuth, cameraPosition.tilt),
                Animation(Animation.Type.SMOOTH, 1.5f),
                null
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showRouteInfo(route: Route) {
        val routeInfo = mainMapviewModel.getRouteInfo(route)
        binding.routeInfoView.visibility = View.VISIBLE
        binding.routeLengthText.text = "${getString(R.string.route_length)}: ${routeInfo[1]}"
        binding.routeTimeText.text = "${getString(R.string.route_time)}: ${routeInfo[0]}"
    }

    private val onAttractionTapListener = MapObjectTapListener { mapObject, _ ->
        showBottomSheet(mapObject.userData as AttractionModel)
        true
    }

    private fun showBottomSheet(userData: AttractionModel) {
        val bottomSheet = AttractionBottomSheet(userData)
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }

    private fun addPlacemark(points: List<OSMPlace>) {
        placemarksCollection = binding.mapView.mapWindow.map.mapObjects.addCollection()
        val marker = createBitmapFromVector(R.drawable.map_marker_svg)
        val imageProvider = fromBitmap(marker)
        
        points.map { it.convertToAttractionModel(it, false) }.forEach { point ->
            placemarksCollection?.addPlacemark()?.apply {
                geometry = Point(point.latitude, point.longitude)
                setIcon(imageProvider)
                opacity = 0.6f
                setText(point.name)
                userData = point
                addTapListener(onAttractionTapListener)
            }
        }
    }

    fun removeRoute() {
        currentRoute?.let { if (it.isValid) binding.mapView.mapWindow.map.mapObjects.remove(it) }
        currentRoute = null
        placemarksCollection?.isVisible = true
    }

    fun removeCustomPoints() {
        customPlaceMarks.forEach { binding.mapView.mapWindow.map.mapObjects.remove(it) }
        customPlaceMarks.clear()
        placemarksCollection?.isVisible = true
    }

    private fun setupCustomPointObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainMapviewModel.customPointState.collect { state ->
                    if (state is MainMapViewModel.CustomPointUiState.Success) {
                        binding.mapView.mapWindow.map.mapObjects.clear()
                        placemarksCollection?.isVisible = false
                        addCustomPlacemark(state.attractions)
                        calculateRoute(
                            LatLng(state.attractions.last().latitude, state.attractions.last().longitude),
                            LatLng(state.attractions.first().latitude, state.attractions.first().longitude),
                            state.attractions.map { LatLng(it.latitude, it.longitude) }
                        )
                    }
                }
            }
        }
    }

    private fun addCustomPlacemark(points: List<AttractionModel>) {
        val marker = createBitmapFromVector(R.drawable.map_marker_svg)
        val imageProvider = fromBitmap(marker)
        points.forEach { point ->
            val placemark = binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
                geometry = Point(point.latitude, point.longitude)
                setIcon(imageProvider)
                setText(point.name)
                userData = point.copy(isCustom = true)
                customPlaceMarks.add(this)
            }
            placemark.addTapListener(onAttractionTapListener)
        }
    }

    private fun drawDetailedBelarusBorder(border: BorderData) {
        val polygon = Polygon(LinearRing(border.points), emptyList())
        binding.mapView.mapWindow.map.mapObjects.addPolygon(polygon).apply {
            strokeColor = ContextCompat.getColor(requireContext(), R.color.border_fill_color)
            strokeWidth = 4.0f
            fillColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        }
    }

    fun setCurrentLocationPoint() {
        val lat = requireContext().getFromPrefs("lat", 52.4171724f).toDouble()
        val lon = requireContext().getFromPrefs("lon", 30.9963954f).toDouble()
        val marker = createBitmapFromVector(R.drawable.current_location)
        val imageProvider = fromBitmap(marker)

        binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = Point(lat, lon)
            setIcon(imageProvider)
            opacity = 0.6f
            setText(getString(R.string.current_location))
            customPlaceMarks.add(this)
        }
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(requireContext(), art) ?: return null
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val CITY = "Гомель"
    }
}
