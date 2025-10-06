package com.example.travelhelper.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.travelhelper.App
import com.example.travelhelper.MainActivity
import com.example.travelhelper.R
import com.example.travelhelper.data.db.AttractionEntity
import com.example.travelhelper.databinding.FragmentMainMapBinding
import com.example.travelhelper.utils.ExpandableTextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.launch

class MainMapFragment : Fragment(), MainActivity.MenuConfig {

    private var _binding: FragmentMainMapBinding? = null
    private val binding get() = _binding!!

    private val viewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = (requireActivity().application as App).db.getAttrationDao()
                return MainMapViewModel(dao) as T
            }
        }
    }
    private val viewModel: MainMapViewModel by viewModels { viewModelFactory }

    private val placemarkMap = mutableMapOf<PlacemarkMapObject, AttractionEntity>()
    private val mapInputListener: InputListener = MapTapListener()

    private val onAttractionTapListener = MapObjectTapListener { mapObject, _ ->
        val placemark = mapObject as? PlacemarkMapObject ?: return@MapObjectTapListener true
        val attractionEntity = placemarkMap[placemark]
        if (attractionEntity != null) {
            viewModel.selectAttraction(attractionEntity)
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocationPermissions()
        setupMapListeners()
        setupObservers()
    }

    private fun setupObservers() {
        val bottomSheetView = binding.bottomSheetLayout.root
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        val likeButton = bottomSheetView.findViewById<ImageButton>(R.id.like_button)
        val titleText = bottomSheetView.findViewById<TextView>(R.id.title_text)
        val descriptionText = bottomSheetView.findViewById<ExpandableTextView>(R.id.description_text)

        likeButton.setOnClickListener {
            viewModel.onLikeClicked()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isCurrentPlaceLiked.collect { isLiked ->
                val iconRes = if (isLiked) R.drawable.ic_circle_filled else R.drawable.ic_circle_outline
                likeButton.setImageResource(iconRes)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedAttraction.collect { attraction ->
                if (attraction != null) {
                    titleText.text = attraction.addres
                    descriptionText.text = attraction.discription
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
    }

    private fun addPlacemarksOnMap(attractions: List<AttractionEntity>) {
        if (!isAdded) return
        Log.d("MAP_DEBUG", "Начинаем добавлять метки. Всего получено: ${attractions.size}")

        val markerBitmap = createBitmapFromVector(R.drawable.map_marker_svg) ?: run {
            Log.e("MAP_DEBUG", "Критическая ошибка: Не удалось создать иконку для метки (Bitmap).")
            return
        }
        val imageProvider = ImageProvider.fromBitmap(markerBitmap)

        attractions.forEach { attraction ->
            val placemark = binding.mapView.mapWindow.map.mapObjects.addPlacemark().apply {
                geometry = Point(attraction.shirota.toDouble(), attraction.dolgota.toDouble())
                setIcon(imageProvider)
                userData = attraction
            }
            placemark.addTapListener(onAttractionTapListener)
            placemarkMap[placemark] = attraction
        }
        Log.d("MAP_DEBUG", "Добавление меток завершено. На карте должно быть ${placemarkMap.size} меток.")
    }

    private val locationPermissionRequestCode = 1000

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
        placemarkMap.keys.forEach { it.removeTapListener(onAttractionTapListener) }
        placemarkMap.clear()
        binding.mapView.mapWindow.map.removeInputListener(mapInputListener)
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

    override fun shouldShowMenuItems(menu: Menu): Boolean = true

    private fun checkLocationPermissions() {
        if (hasLocationPermissions()) {
            setupMap()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun setupMap() {
        Log.d("MAP_DEBUG", "Метод setupMap() вызван. Начинаем настройку карты и загрузку меток.")
        binding.mapView.mapWindow.map.move(
            CameraPosition(Point(52.4171724, 30.9963954), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
        binding.mapView.mapWindow.map.isRotateGesturesEnabled = true

        viewLifecycleOwner.lifecycleScope.launch {
            val allAttractions = viewModel.getAllAttractionsFromDb()
            addPlacemarksOnMap(allAttractions)
        }
    }

    private inner class MapTapListener : InputListener {
        override fun onMapTap(map: com.yandex.mapkit.map.Map, point: com.yandex.mapkit.geometry.Point) {
            viewModel.selectAttraction(null)
        }

        override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: com.yandex.mapkit.geometry.Point) {
            // Ничего не делаем
        }
    }

    private fun setupMapListeners() {
        binding.mapView.mapWindow.map.addInputListener(mapInputListener)
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        if (!isAdded) return null
        val drawable = ContextCompat.getDrawable(requireContext(), art) ?: return null
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
