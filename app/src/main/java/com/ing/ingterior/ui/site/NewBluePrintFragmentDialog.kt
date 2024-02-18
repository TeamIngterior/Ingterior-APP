package com.ing.ingterior.ui.site

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import com.ing.ingterior.R
import com.ing.ingterior.model.BluePrintModel
import com.ing.ingterior.ui.IngTeriorViewModelFactory
import com.ing.ingterior.util.FileWrapper.createFileName
import com.ing.ingterior.util.FileWrapper.getImageNameFromUri
import com.ing.ingterior.util.ImageUtils
import com.ing.ui.button.VisualButton
import com.ing.ui.button.VisualImageButton
import com.ing.ui.text.label.LabelView
import kotlinx.coroutines.launch

class NewBluePrintFragmentDialog : DialogFragment() {

    companion object{
        private const val TAG = "NewBluePrintFragmentDialog"
    }

    private val vibClose:VisualImageButton by lazy { requireView().findViewById(R.id.vib_new_blue_print_close) }
    private val vibRotate:VisualImageButton by lazy { requireView().findViewById(R.id.vib_new_blue_print_rotate) }
    private val vibHorizontalInversion:VisualImageButton by lazy { requireView().findViewById(R.id.vib_new_blue_print_horizontal_inversion) }
    private val vibVerticalInversion:VisualImageButton by lazy { requireView().findViewById(R.id.vib_new_blue_print_vertical_inversion) }
    private val viewSelectImage:View by lazy { requireView().findViewById(R.id.view_new_blue_print_select_image) }
    private val labelSelectImage:LabelView by lazy { requireView().findViewById(R.id.label_new_blue_print_select_image) }
    private val ivSelectImage:ImageView by lazy { requireView().findViewById(R.id.iv_new_blue_print_select_image) }
    private val btnCommit: VisualButton by lazy { requireView().findViewById(R.id.btn_new_blue_print_commit) }


    private val viewModel : SiteViewModel by lazy { ViewModelProvider(this, IngTeriorViewModelFactory())[SiteViewModel::class.java] }

    private val getPictureResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == AppCompatActivity.RESULT_OK) {
            val photoUri = result.data?.data
            if(photoUri != null) {
                val fileName = createFileName(requireContext(), photoUri)
                val fileName2 = getImageNameFromUri(requireContext(), photoUri)

                Log.d(TAG, "onCreate: fileName＝${fileName}, fileName2＝${fileName2}")
                if(fileName!=null){
                    viewModel.bluePrintModel.postValue(BluePrintModel(0L, photoUri, fileName2, 0f))
                }
                else{
                    Toast.makeText(requireContext(), "유효하지 않은 타입의 사진입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(requireContext(), "사진을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.Theme_Custom_Dialog_Default)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_blue_print_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vibClose.setOnClickListener {
            dismiss()
        }

        vibRotate.setOnClickListener {
            if(viewModel.bluePrintModel.value ==null) {
                return@setOnClickListener
            }
            lifecycleScope.launch {
                viewModel.bluePrintModel.value?.increaseRotation()

                ivSelectImage.setImageBitmap(viewModel.reScaleBluePrintImage(requireContext()))
            }
        }

        vibHorizontalInversion.setOnClickListener {
            if(viewModel.bluePrintModel.value ==null) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                viewModel.bluePrintModel.value?.updateHorizontalInversion()

                ivSelectImage.setImageBitmap(viewModel.reScaleBluePrintImage(requireContext()))
            }
        }

        vibVerticalInversion.setOnClickListener {
            if(viewModel.bluePrintModel.value ==null) {
                return@setOnClickListener
            }

            lifecycleScope.launch {
                viewModel.bluePrintModel.value?.updateVerticalInversion()

                ivSelectImage.setImageBitmap(viewModel.reScaleBluePrintImage(requireContext()))
            }
        }

        viewSelectImage.setOnClickListener {
            getPictureResult.launch(ImageUtils.getPictureIntent())
        }

        btnCommit.setOnClickListener {
            dismiss()
        }

        viewModel.bluePrintModel.distinctUntilChanged().observe(this) { bluePrint ->
            lifecycleScope.launch {
                if(bluePrint==null) {
                    labelSelectImage.isVisible = true
                    ivSelectImage.isVisible = false
                    btnCommit.isEnabled = false
                }
                else{
                    labelSelectImage.isVisible = false
                    ivSelectImage.isVisible = true
                    btnCommit.isEnabled = true
                    ivSelectImage.setImageBitmap(viewModel.reScaleBluePrintImage(requireContext()))
                }
            }

        }

    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val displayMetrics = resources.displayMetrics
            val width = displayMetrics.widthPixels // 화면 너비의 90%
            val height = displayMetrics.heightPixels * 0.85 // 화면 높이의 75%
            dialog.window?.setLayout(width, height.toInt())
        }
    }
}