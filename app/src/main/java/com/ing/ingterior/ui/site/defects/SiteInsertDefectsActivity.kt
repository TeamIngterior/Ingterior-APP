package com.ing.ingterior.ui.site.defects

import android.content.Intent
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.ing.ingterior.EXTRA_SITE
import com.ing.ingterior.R
import com.ing.ingterior.db.Site
import com.ing.ingterior.model.ImageModel
import com.ing.ingterior.ui.GridSpacingItemDecoration
import com.ing.ingterior.ui.IngTeriorViewModelFactory
import com.ing.ingterior.ui.viewmodel.SiteViewModel
import com.ing.ingterior.util.FileWrapper
import com.ing.ingterior.util.ImageUtils
import com.ing.ingterior.util.getDisplayPixelSize
import com.ing.ingterior.util.getParcelableCompat
import com.ing.ingterior.util.hideKeyboard
import com.ing.ui.button.VisualButton
import com.ing.ui.button.VisualDefaultButton
import com.ing.ui.text.edit.InputTextLayout

class SiteInsertDefectsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SiteInsertDefectsFragment"
        private const val MAX_DEFECT_IMAGE_SIZE = 8
    }

    private lateinit var siteViewModel: SiteViewModel
    private lateinit var siteDefectImagesAdapter: SiteDefectImagesAdapter

    private lateinit var frameImageLayout: FrameLayout
    private lateinit var photoBluePrintView: PhotoView
    private lateinit var markImageView: ImageView
    private lateinit var itvInsertName: InputTextLayout
    private lateinit var itvInsertDescription: InputTextLayout
    private lateinit var vdbInsertImage: VisualDefaultButton
    private lateinit var rvDefectsImages: RecyclerView
    private lateinit var vbCommit: VisualButton
    private lateinit var getPictureResult: ActivityResultLauncher<Intent>


    private var gridSpacingItemDecoration: GridSpacingItemDecoration? = null

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var tempX = 0f
    private var tempY = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_site_insert_defects)
        
        siteViewModel = ViewModelProvider(this, IngTeriorViewModelFactory())[SiteViewModel::class.java]
        siteViewModel.site = intent.getParcelableCompat<Site>(EXTRA_SITE)

        getPictureResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK) {
                if(siteViewModel.defectImages.size >= MAX_DEFECT_IMAGE_SIZE) {
                    Toast.makeText(this, "최대 8장 까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val photoUri = result.data?.data
                if(photoUri != null) {
                    val fileSize: Long = if(FileWrapper.isFileUri(photoUri)) FileWrapper.getFileSize(this, photoUri)
                    else ImageUtils.getMediaFileSize(this, photoUri)
                    Log.d(TAG, "getPictureResult: photoUri=$photoUri")
                    Log.d(TAG, "getPictureResult: fileSize=$fileSize")
                    if(fileSize > 5* FileWrapper.MB){
                        Toast.makeText(this, "이미지 파일의 크기가 너무 큽니다.", Toast.LENGTH_SHORT).show()
                        return@registerForActivityResult
                    }

                    var fileName: String? = FileWrapper.getImageNameFromUri(this, photoUri)
                    if(fileName?.lastIndexOf(".")!! < 0){
                        fileName = FileWrapper.createFileName(this, photoUri)
                    }

                    Log.d(TAG, "getPictureResult: fileName=$fileName")
                    if(fileName!=null){
                        siteViewModel.addDefectImage(this, ImageModel(0L, photoUri, null, fileName)).apply {
                            Log.d(TAG, "getPictureResult: defectImages=${siteViewModel.defectImages}")
                            siteDefectImagesAdapter.notifyDataSetChanged()
                        }
                    }
                    else{
                        Toast.makeText(this, "유효 하지 않은 타입의 사진 입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this, "사진을 불러 오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        initBindingView()

        initBluePrintView()

        initMarkView()

        initDefectsImageView()
    }

    private fun initBindingView(){
        itvInsertName = findViewById(R.id.itv_site_insert_defects_name_layout)
        itvInsertDescription = findViewById(R.id.itv_site_insert_defects_description_layout)
        vbCommit = findViewById(R.id.vb_site_insert_defects_commit)
    }

    private fun initMarkView(){
        markImageView = ImageView(this)
        markImageView.apply {
            layoutParams = FrameLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.mark_size), resources.getDimensionPixelSize(R.dimen.mark_size)) // 마크의 크기 설정
            setImageResource(R.drawable.ic_mark) // 마크 이미지 설정
            x = resources.getDimensionPixelSize(R.dimen.popup_item_width).toFloat() // 초기 X 위치 설정
            y = resources.getDimensionPixelSize(R.dimen.popup_item_width).toFloat() // 초기 Y 위치 설정
            lastTouchX = x
            lastTouchY = y
        }
        frameImageLayout.addView(markImageView)
        markImageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempX = event.x
                    tempY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    // 드래그 이동 로직
                    val dx: Float = event.x - tempX
                    val dy: Float = event.y - tempY
                    view.x += dx
                    view.y += dy
                    lastTouchX = view.x
                    lastTouchY = view.y
                    Log.d("TestActivity", "onCreate: markImageView x=${markImageView.x}, markImageView y=${markImageView.y}")
                }
            }
            true
        }
    }

    private fun initBluePrintView(){
        frameImageLayout = findViewById(R.id.frame_site_insert_defects_image_layout)
        photoBluePrintView = findViewById(R.id.photo_site_insert_defects_blueprint)
        Glide.with(this).load(siteViewModel.site?.imageLocation).into(photoBluePrintView)

//        photoBluePrintView.isZoomable = false
        photoBluePrintView.setOnMatrixChangeListener {
            markImageView.scaleX = photoBluePrintView.scale
            markImageView.scaleY = photoBluePrintView.scale

            // PhotoView의 변환 행렬을 가져옴
            val matrix = Matrix()
            photoBluePrintView.attacher.getSuppMatrix(matrix)

            // 초기 위치를 변환 행렬에 적용하여 실제 위치를 계산
            val tempPoint = floatArrayOf(lastTouchX, lastTouchY)
//            Log.d("TestActivity", "onCreate: transformedPoint=${tempPoint.contentToString()}")
            matrix.mapPoints(tempPoint)

            // 이미지뷰의 위치 조정
            markImageView.x = tempPoint[0]
            markImageView.y = tempPoint[1]
//            Log.d("TestActivity", "onCreate: markImageView x=${markImageView.x}, markImageView y=${markImageView.y}")
        }


        photoBluePrintView.setOnPhotoTapListener { view, x, y ->
            // x와 y는 탭된 위치의 상대적인 비율을 나타냅니다. (0 ~ 1 사이의 값)
            // 예를 들어, 이미지의 중앙을 탭하면 x와 y는 대략 0.5, 0.5가 됩니다.

            // 여기에서 마커와 관련된 로직을 구현할 수 있습니다.
            // 예: 마커가 있는 영역을 탭했는지 확인하고, 해당하는 동작을 수행
//            Log.d("TestActivity", "setOnPhotoTapListener: x=${x}, y=${y}")
            val rect = photoBluePrintView.attacher.displayRect

            // 상대적인 위치(x, y)를 실제 이미지 상의 절대 위치로 변환
            val absoluteX = rect.left + x * rect.width()
            val absoluteY = rect.top + y * rect.height()

            // 마커 뷰의 위치를 업데이트합니다.
            // 마커의 크기를 고려하여 위치 조정이 필요할 수 있습니다.
            lastTouchX = absoluteX - (markImageView.width / 2)
            lastTouchY = absoluteY - (markImageView.height / 2)

            val matrix = Matrix()
            photoBluePrintView.attacher.getSuppMatrix(matrix)
            val tempPoint = floatArrayOf(lastTouchX, lastTouchY)


            markImageView.x = tempPoint[0]
            markImageView.y = tempPoint[1]

            lastTouchX = absoluteX
            lastTouchY = absoluteY
        }

    }

    private fun initDefectsImageView(){
        vdbInsertImage = findViewById(R.id.vdb_site_insert_defects_add_image)
        rvDefectsImages = findViewById(R.id.rv_site_insert_defects_list)

        vdbInsertImage.setOnClickListener {
            itvInsertName.getTextView().hideKeyboard(this)
            itvInsertDescription.getTextView().hideKeyboard(this)
            if(siteViewModel.defectImages.size >= MAX_DEFECT_IMAGE_SIZE) {
                Toast.makeText(this, "최대 8장 까지 첨부할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
            else{
                getPictureResult.launch(ImageUtils.getPictureIntent())
            }
        }

        val displaySize = this.getDisplayPixelSize()
        val screenWidth = displaySize.width
        val columnCount = when {
            screenWidth <= resources.getInteger(R.integer.smallest_device) -> 3 // 가장 작은 디바이스
            screenWidth <= resources.getInteger(R.integer.medium_device) -> 4 // 중간 크기 디바이스
            screenWidth <= resources.getInteger(R.integer.expanded_device) -> 5 // 확장된 크기 디바이스
            screenWidth <= resources.getInteger(R.integer.biggest_device) -> 6 // 가장 큰 디바이스
            else -> 8 // 기본값
        }

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_item_margin) // 각 아이템 사이의 간격
        val availableWidth = screenWidth - (spacing * (columnCount + 1))
        val itemSize = availableWidth / columnCount
        siteDefectImagesAdapter = SiteDefectImagesAdapter(siteViewModel, itemSize)
        rvDefectsImages.apply {
            layoutManager = GridLayoutManager(context, columnCount)
            if (gridSpacingItemDecoration != null) removeItemDecoration(gridSpacingItemDecoration!!)
            gridSpacingItemDecoration = GridSpacingItemDecoration(columnCount, spacing, false)
            addItemDecoration(gridSpacingItemDecoration!!)
            adapter = siteDefectImagesAdapter
        }
    }

}