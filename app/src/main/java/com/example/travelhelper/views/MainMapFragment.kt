package com.example.travelhelper.views

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.health.connect.datatypes.units.Length
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.example.travelhelper.R
import com.example.travelhelper.databinding.FragmentMainMapBinding
import com.example.travelhelper.views.AttractionBottomSheet
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.runtime.image.ImageProvider.fromBitmap

class MainMapFragment : Fragment() {
    private val locationPermissionRequestCode = 1000
    private val viewModel: MainMapViewModel by viewModels()

    private var _binding: FragmentMainMapBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
        // TODO: Use the ViewModel
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
        val point = Point(52.4221751, 31.0167343)
        addPlacemark(point)

        binding.testbtn.setOnClickListener {
            showBottomSheet()
        }

        checkLocationPermissions()
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

    private fun setupMap() {
        // Перемещаем камеру к нужной точке
        val targetPoint = Point(52.4171724, 30.9963954) // Gomel
        binding.mapView.map.move(
            CameraPosition(targetPoint, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        // Включаем слои
        binding.mapView.map.isRotateGesturesEnabled = true
        binding.mapView.map.isZoomGesturesEnabled = true
        binding.mapView.map.isScrollGesturesEnabled = true

        // Добавляем обработчики
        setupMapListeners()
    }

    private fun setupMapListeners() {
        binding.mapView.map.addInputListener(object : com.yandex.mapkit.map.InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                // Обработка тапа по карте
            }

            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                // Обработка долгого тапа
            }
        })
    }

    private fun addPlacemark(point: Point) {
        val marker = createBitmapFromVector(R.drawable.map_marker_svg)

        val imageProvider = fromBitmap(marker)

        val placemark = binding.mapView.map.mapObjects.addPlacemark(point)
        placemark.setIcon(imageProvider)
        placemark.opacity = 0.6f
        placemark.setText("placeholder text")
        placemark.addTapListener { _, _ ->
            Toast.makeText(requireContext(), "dfgdsfdsf", Toast.LENGTH_SHORT).show()
            showBottomSheet()
            true
        }
    }

    private fun showBottomSheet() {
        val bottomSheet = AttractionBottomSheet()
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