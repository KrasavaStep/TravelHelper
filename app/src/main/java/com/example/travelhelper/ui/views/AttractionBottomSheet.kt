package com.example.travelhelper.ui.views

import android.annotation.SuppressLint
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_info_bottom_sheet, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeBtn = view.findViewById<FloatingActionButton>(R.id.close_bottom_sheet_btn)

        val workingTimeTextView = view.findViewById<TextView>(R.id.working_time_txt)
        val priceTextView = view.findViewById<TextView>(R.id.price_txt)
        val nameTextView = view.findViewById<TextView>(R.id.name_txt)
        val descriptionTextView = view.findViewById<TextView>(R.id.description_txt)
        val wikiTextView = view.findViewById<TextView>(R.id.wiki_txt)

        workingTimeTextView.text = if (!userData.openingHours.isNullOrEmpty()) {
            "${getString(R.string.opening_hours)}: ${userData.openingHours}"
        } else {
            "${getString(R.string.opening_hours)}: ${getString(R.string.opening_hours_placeholder)}"
        }

        priceTextView.text = if (userData.isFee) {
            getString(R.string.attraction_yes_fee)
        } else {
            getString(R.string.attraction_no_fee)
        }

        nameTextView.text = userData.name
        descriptionTextView.text = userData.description
        if (userData.description.isNullOrEmpty()) {
            descriptionTextView.visibility = View.GONE
        }

        val siteText = userData.website ?: userData.wikipedia ?: userData.wikidata
        wikiTextView.text = "${getString(R.string.get_more_info)}: $siteText"
        if (siteText.isNullOrEmpty()) {
            wikiTextView.visibility = View.GONE
        }

        closeBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismiss()
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
}