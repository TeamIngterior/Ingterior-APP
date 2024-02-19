package com.ing.ui.button

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ing.ui.R
import com.ing.ui.text.label.LabelView

class VisualButton2 : LinearLayoutCompat {

    private var iconRec: Int? = null
    private var actionString: String = ""

    private val imageView: ImageView by lazy {
        ImageView(context).apply {
            val size = context.resources.getDimensionPixelSize(R.dimen.login_icon_size)
            layoutParams = LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
        }
    }

    private val labelView: LabelView by lazy {
        LabelView(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                val startMargin = context.resources.getDimensionPixelSize(R.dimen.login_label_start_margin)
                setMargins(startMargin,0,0,0)
                gravity = Gravity.CENTER_VERTICAL
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        isClickable = true
        isFocusable = true

        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        background = ContextCompat.getDrawable(context, R.drawable.bg_default_button)


        addView(imageView)
        addView(labelView)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VisualButton2, defStyleAttr, 0)
            iconRec = (typedArray.getResourceId(R.styleable.VisualButton2_visualSrc, -1))
            imageView.isVisible = iconRec != -1
            if(iconRec!=null) imageView.setImageResource(iconRec!!)

            actionString = (typedArray.getString(R.styleable.VisualButton2_visualText)) ?: ""
            labelView.text = actionString

            val size = (typedArray.getDimensionPixelSize(R.styleable.VisualButton2_visualIconSize, 0))
            if(size != 0) {
                val iconLayoutParams = LayoutParams(size, size)
                iconLayoutParams.gravity = Gravity.CENTER_VERTICAL
                imageView.layoutParams = iconLayoutParams
            }

            typedArray.recycle()
        }
    }
}