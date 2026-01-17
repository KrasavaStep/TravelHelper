package com.example.travelhelper.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainMapBinding.bind(view)
        thisView = view
        
        // ВАЖНО: используем ту же область видимости, что и в BottomSheet
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        
        setupMap()
        setupObservers(view)

        mainMapviewModel.getBelarusBorder()
        mainMapviewModel.borderLiveData.observe(viewLifecycleOwner) { borderList ->
            borderList.forEach { drawDetailedBelarusBorder(it) }
        }

        if (args.route != null) {
            mainMapviewModel.getCustomPoints(args.id)
            setupCustomPointObserver()
        }

        setupRouteObservers()

        // Поиск
        val activitySharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        activitySharedViewModel.selectedAttraction.observe(viewLifecycleOwner) { attraction ->
            if (attraction != null) {
                showBottomSheet(attraction)
                binding.mapView.mapWindow.map.move(
                    CameraPosition(Point(attraction.latitude, attraction.longitude), 16.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
            }
        }

        // Построение маршрута (слушаем локальный SharedViewModel для BottomSheet)
        sharedViewModel.dialogResult.observe(viewLifecycleOwner) { it ->
            binding.routeInfoView.visibility = View.GONE
            removeRoute()
            calculateRoute(LatLng(it[1], it[0]))
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

    override fun shouldShowMenuItems(menu: Menu): Boolean = true

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

    private fun setupObservers(view: View) {
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
                    when (state) {
                        is MainMapViewModel.RouteUiState.Success -> {
                            displayRoute(state.route)
                            showRouteInfo(state.route)
                            binding.loadingView.visibility = View.GONE
                        }
                        is MainMapViewModel.RouteUiState.Error -> {
                            Snackbar.make(thisView, state.exception.message.toString(), Snackbar.LENGTH_LONG).show()
                            binding.loadingView.visibility = View.GONE
                        }
                        is MainMapViewModel.RouteUiState.Loading -> {
                            binding.loadingView.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                        }
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
        currentRoute = binding.mapView.mapWindow.map.mapObjects.addPolyline(Polyline(decodedPath))
        currentRoute?.apply {
            strokeWidth = 5f
            setStrokeColor(ContextCompat.getColor(requireContext(), R.color.blue))
            outlineWidth = 1f
            outlineColor = ContextCompat.getColor(requireContext(), R.color.black)
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
            }
            placemark.addTapListener(onAttractionTapListener)
            customPlaceMarks.add(placemark)
        }
    }

    private fun drawDetailedBelarusBorder(border: BorderData) {
        val outerRing = LinearRing(border.points)
        val polygon = Polygon(outerRing, emptyList())
        binding.mapView.mapWindow.map.mapObjects.addPolygon(polygon).apply {
            strokeColor = resources.getColor(R.color.border_fill_color)
            strokeWidth = 4.0f
            fillColor = resources.getColor(R.color.transparent)
        }
    }

    fun setCurrentLocationPoint() {
        val lat = requireContext().getFromPrefs("lat", 52.4171724f).toDouble()
        val lon = requireContext().getFromPrefs("lon", 30.9963954f).toDouble()
        val marker = createBitmapFromVector(R.drawable.current_location)
        val imageProvider = fromBitmap(marker)

        val placemark = binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = Point(lat, lon)
            setIcon(imageProvider)
            opacity = 0.6f
            setText(getString(R.string.current_location))
        }
        customPlaceMarks.add(placemark)
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
