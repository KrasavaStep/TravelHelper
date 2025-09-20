package com.example.travelhelper.views

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.travelhelper.R
import com.example.travelhelper.databinding.FragmentMainMapBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AttractionBottomSheet : BottomSheetDialogFragment() {

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

        /*val editText = view.findViewById<EditText>(R.id.editText)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        btnSubmit.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
                listener?.onDataSubmitted(text)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Введите текст", Toast.LENGTH_SHORT).show()
            }
        }*/

        closeBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismiss()
    }

    override fun getTheme(): Int {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog
    }
}