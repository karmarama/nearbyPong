package agency.nice.nearbypong.widgets

import agency.nice.nearbypong.R
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.view_button.view.*


/**
 * Created by ferranribell on 07/09/2017.
 */

class Button @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        var LEFT = 1
        var RIGHT = 2
        var TOP = 3
        var BOTTOM = 4
    }

    var borders = 0
    var text = ""
    private var startColor = R.color.colorAccent
    private var endColor = R.color.colorPrimary
    private var transparentColor = R.color.transparent

    init {
        var customView = View.inflate(context, R.layout.view_button, null) as ConstraintLayout
        addView(customView)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs,
                    R.styleable.Button, 0, 0)
            borders = a.getInteger(R.styleable.Button_borderSides, LEFT)
            text = a.getString(R.styleable.Button_text)
            a.recycle()
            setBorders(startColor, endColor, transparentColor)
            buttonText.text = text
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                setOpositeBorders(endColor, startColor, transparentColor)
                buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.button_text_press_size))
                this.invalidate()
            }
            MotionEvent.ACTION_UP -> {
                setBorders(startColor, endColor, transparentColor)
                buttonText.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.button_text_size))
                this.invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setOpositeBorders(startColor: Int, endColor: Int, transparentColor: Int) {
        barLeft.background = null
        barRight.background = null
        barTop.background = null
        barBottom.background = null
        if (containsFlag(borders, LEFT)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, transparentColor)))
            barRight.background = gradientDrawable
        }

        if (containsFlag(borders, RIGHT)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, transparentColor)))
            barLeft.background = gradientDrawable
        }

        if (containsFlag(borders, BOTTOM)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.RIGHT_LEFT,
                    intArrayOf(ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, transparentColor)))
            barBottom.background = gradientDrawable
        }

        if (containsFlag(borders, TOP)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, transparentColor)))
            barTop.background = gradientDrawable
        }
    }

    private fun setBorders(startColor: Int, endColor: Int, transparentColor: Int) {
        barLeft.background = null
        barRight.background = null
        barTop.background = null
        barBottom.background = null
        if (containsFlag(borders, LEFT)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, transparentColor)))
            barLeft.background = gradientDrawable
        }

        if (containsFlag(borders, RIGHT)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, transparentColor)))
            barRight.background = gradientDrawable
        }

        if (containsFlag(borders, BOTTOM)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, transparentColor)))
            barBottom.background = gradientDrawable
        }

        if (containsFlag(borders, TOP)) {
            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(ContextCompat.getColor(context, startColor), ContextCompat.getColor(context, endColor), ContextCompat.getColor(context, transparentColor)))
            barTop.background = gradientDrawable
        }
    }

    fun setColorBlack() {
        buttonText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        startColor = android.R.color.black
        endColor = android.R.color.black
        transparentColor = R.color.transparent_black
        setBorders(startColor, endColor, transparentColor)
    }

    private fun containsFlag(flagSet: Int, flag: Int): Boolean {
        return flagSet or flag == flagSet
    }

}