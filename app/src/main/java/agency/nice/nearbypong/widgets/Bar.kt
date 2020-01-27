package agency.nice.nearbypong.widgets

import agency.nice.nearbypong.R
import agency.nice.nearbypong.model.MAX_GOALS
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator


/**
 * Created by ferranribell on 07/09/2017.
 */

const val SIDE_LEFT: Int = 0
const val SIDE_RIGHT: Int = 1

class Bar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    var bottomLimit: Int = 0
    var side: Int = SIDE_LEFT

    init {
        setImageResource(R.drawable.rectangle_green)
        setFlingListener()
    }

    private fun setFlingListener() {
        setOnTouchListener { view, motionEvent ->
            if (motionEvent!!.action == MotionEvent.ACTION_MOVE) {
                var newPosY = (motionEvent!!.rawY - view.height / 2)
                if (newPosY <= bottomLimit - height && newPosY > 0) {
                    view.y = newPosY
                }
            }

            true
        }
    }

    fun setDarkColour() {
        setImageResource(R.drawable.rectangle_black)
    }

    fun getTheLeft() = x - width / 2

    fun getTheRight() = x + width / 2

    fun getTheTop() = y

    fun getTheBottom() = y + height

    fun setHeight(size: Int) {
        val params = layoutParams
        params.height = size
        layoutParams = params
    }

    fun changeBarSize(difference: Int) {
        if (difference < 0) {
            val barHeight = resources.getDimensionPixelOffset(R.dimen.bar_height)
            val percentage: Float = (MAX_GOALS + difference).toFloat() / MAX_GOALS.toFloat()
            height = (barHeight * percentage).toInt()
        }
    }

    fun blink() {
        val blinkAnimation =
            AlphaAnimation(1f, 0.5f) // Change alpha from fully visible to invisible
        blinkAnimation.apply {
            duration = 100 // duration - half a second
            interpolator = LinearInterpolator() // do not alter animation rate
            repeatCount = 1 // Repeat animation infinitely
            repeatMode = Animation.REVERSE
        }
        startAnimation(blinkAnimation)
    }
}
