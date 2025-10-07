package com.example.travelhelper.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.travelhelper.MainActivity
import com.example.travelhelper.R
import com.example.travelhelper.data.network.OSMPlace
import com.example.travelhelper.databinding.FragmentMainMapBinding
import com.example.travelhelper.ui.views.AttractionBottomSheet
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.runtime.image.ImageProvider.fromBitmap
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class MainMapFragment : Fragment(), MainActivity.MenuConfig {
    private val locationPermissionRequestCode = 1000

    private val mainMapviewModel by viewModel<MainMapViewModel>(named("mainMapViewModel"))

    private val placemarks = mutableListOf<PlacemarkMapObject>()

    private var _binding: FragmentMainMapBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainMapBinding.bind(view)

        checkLocationPermissions()
        setupObservers(view)

        binding.reloadImg.setOnClickListener {
            setupObservers(view)
        }
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

    override fun onDestroy() {
        placemarks.forEach { it.removeTapListener(onAttractionTapListener) }
        placemarks.clear()
        binding.mapView.mapWindow.map.removeInputListener(mapInputListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setMenuConfig(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setMenuConfig(null)
    }

    override fun shouldShowMenuItems(menu: Menu): Boolean = true

    private fun checkLocationPermissions() {
        if (hasLocationPermissions()) {
            setupMap()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionRequestCode
            )
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap()
            }
        }
    }

    private fun setupObservers(view: View) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainMapviewModel.loadAttractions("Гомель")
                mainMapviewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is MainMapViewModel.AttractionsUiState.Error -> {
                            binding.reloadAttractions.visibility = View.VISIBLE
                            binding.errorMsg.text = uiState.exception.message
                            binding.changeMapLayout.visibility = View.GONE
                            binding.showLocation.visibility = View.GONE
                        }
                        is MainMapViewModel.AttractionsUiState.Loading -> {
                            if (uiState.isLoading) {
                                binding.loadingView.visibility = View.VISIBLE
                                binding.reloadAttractions.visibility = View.GONE
                                binding.changeMapLayout.visibility = View.GONE
                                binding.showLocation.visibility = View.GONE
                            } else {
                                binding.loadingView.visibility = View.GONE
                            }
                        }
                        is MainMapViewModel.AttractionsUiState.Success -> {
                            binding.reloadAttractions.visibility = View.GONE
                            binding.changeMapLayout.visibility = View.VISIBLE
                            binding.showLocation.visibility = View.VISIBLE
                            addPlacemark(uiState.attractions)
                        }
                    }
                }
            }
        }
    }

    private fun setupMap() {
        // Перемещаем камеру к нужной точке
        val targetPoint = Point(52.4171724, 30.9963954) // Gomel
        binding.mapView.mapWindow.map.move(
            CameraPosition(targetPoint, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        // Включаем слои
        binding.mapView.mapWindow.map.isRotateGesturesEnabled = true
        binding.mapView.mapWindow.map.isZoomGesturesEnabled = true
        binding.mapView.mapWindow.map.isScrollGesturesEnabled = true

        // Добавляем обработчики
        setupMapListeners()
    }

    private val mapInputListener = object: InputListener {
        override fun onMapTap(p0: Map, p1: Point) {

        }

        override fun onMapLongTap(
            p0: Map,
            p1: Point
        ) {

        }

    }
    private fun setupMapListeners() {
        binding.mapView.mapWindow.map.addInputListener(mapInputListener)
    }

    private val onAttractionTapListener = MapObjectTapListener { mapObject, point ->
        requireActivity().runOnUiThread {
            showBottomSheet(mapObject.userData as OSMPlace)
        }
        true
    }
    private fun addPlacemark(points: List<OSMPlace>) {
        val marker = createBitmapFromVector(R.drawable.map_marker_svg)

        val imageProvider = fromBitmap(marker)
        points.forEachIndexed { index, point ->
            val placemark = binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
                geometry = Point(point.latitude, point.longitude)
                setIcon(imageProvider)
                opacity = 0.6f
                setText(point.name)
                userData = point
            }
            placemark.addTapListener(onAttractionTapListener)
            placemarks.add(placemark)
        }
    }

    private fun showBottomSheet(userData: OSMPlace) {
        val bottomSheet = AttractionBottomSheet(userData)
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(requireContext(), art) ?: return null
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        fun mainMapFragmentInstance() = MainMapFragment()
    }
}