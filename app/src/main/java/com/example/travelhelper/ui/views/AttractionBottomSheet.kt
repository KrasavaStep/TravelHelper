package com.example.travelhelper.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.travelhelper.R
import com.example.travelhelper.data.network.OSMPlace
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AttractionBottomSheet(val userData: OSMPlace) : BottomSheetDialogFragment() {

    /*interface BottomSheetListener {
        fun onDataSubmitted(data: String)
    }

    private var listener: BottomSheetListener? = null

    fun setListener(listener: BottomSheetListener) {
        this.listener = listener
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_info_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeBtn = view.findViewById<FloatingActionButton>(R.id.close_bottom_sheet_btn)

        val workingTimeTextView = view.findViewById<TextView>(R.id.working_time_txt)
        val priceTextView = view.findViewById<TextView>(R.id.price_txt)
        val addressTextView = view.findViewById<TextView>(R.id.address_txt)
        val descriptionTextView = view.findViewById<TextView>(R.id.description_txt)
        val wikiTextView = view.findViewById<TextView>(R.id.wiki_txt)

        workingTimeTextView.text = userData.category
        priceTextView.text = userData.type
        addressTextView.text = userData.name
        descriptionTextView.text = userData.description ?: userData.website ?: ""
        wikiTextView.text = userData.wikipedia ?: userData.wikidata?: ""

        closeBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismiss()
    }

    override fun getTheme(): Int {
        return R.style.Base_Theme_TravelHelper
    }
}