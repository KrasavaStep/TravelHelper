package com.example.travelhelper.ui.bottom_sheet_view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.example.travelhelper.R
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.utils.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class AttractionBottomSheet(val userData: AttractionModel) : BottomSheetDialogFragment() {

    private val viewModel by viewModel<BottomSheetViewModel>(named("bottomSheetViewModel"))
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_info_bottom_sheet, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireParentFragment())[SharedViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setDimAmount(0f)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val panelHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 140f, resources.displayMetrics
            ).toInt()
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.peekHeight = panelHeight
            behavior.isHideable = true
            behavior.skipCollapsed = false 
            it.post {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            it.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val closeBtn = view.findViewById<ImageButton>(R.id.close_bottom_sheet_btn)
        val likeBtn = view.findViewById<ImageButton>(R.id.like_btn)
        val createRouteBtn = view.findViewById<FloatingActionButton>(R.id.create_route_btn)
        val btnHome = view.findViewById<ImageButton>(R.id.btn_home)

        view.findViewById<TextView>(R.id.name_txt).text = userData.name
        view.findViewById<TextView>(R.id.working_time_txt).text = if (!userData.openingHours.isNullOrEmpty()) {
            "${getString(R.string.opening_hours)}: ${userData.openingHours}"
        } else {
            "${getString(R.string.opening_hours)}: ${getString(R.string.opening_hours_placeholder)}"
        }

        view.findViewById<TextView>(R.id.price_txt).text = if (userData.isFee) {
            getString(R.string.attraction_yes_fee)
        } else {
            getString(R.string.attraction_no_fee)
        }

        val descriptionTextView = view.findViewById<TextView>(R.id.description_txt)
        descriptionTextView.text = userData.description
        if (userData.description.isNullOrEmpty()) descriptionTextView.visibility = View.GONE

        val wikiTextView = view.findViewById<TextView>(R.id.wiki_txt)
        val siteText = if (!userData.website.isNullOrEmpty()) {
            userData.website
        } else if (!userData.wikipedia.isNullOrEmpty()) {
            "https://be.wikipedia.org/wiki/${userData.wikipedia.substring(3)}"
        } else if(!userData.wikidata.isNullOrEmpty()) {
            "https://www.wikidata.org/wiki/${userData.wikidata}"
        } else ""

        if (siteText.isEmpty()) {
            wikiTextView.visibility = View.GONE
        } else {
            wikiTextView.text = createSpannableString(siteText)
            wikiTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        closeBtn.setOnClickListener { dismiss() }

        // Кнопка ДОМОЙ закрывает карточку и возвращает на карту
        btnHome?.setOnClickListener {
            dismiss()
        }

        likeBtn.setOnClickListener {
            viewModel.addLikedAttractionToDb(userData)
            Toast.makeText(context, "Место добавлено в понравившиеся", Toast.LENGTH_SHORT).show()
        }

        createRouteBtn.setOnClickListener {
            sharedViewModel.setDialogResult(arrayOf(userData.longitude, userData.latitude))
            dismiss()
        }

        if (userData.isCustom) {
            createRouteBtn.visibility = View.GONE
            likeBtn.visibility = View.GONE
        }
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    private fun createSpannableString(link: String): SpannableString {
        val resString = "${getString(R.string.get_more_info)}: $link"
        val spannableString = SpannableString(resString)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
                ds.isUnderlineText = true
            }
        }
        spannableString.setSpan(clickableSpan, 0, resString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
}
