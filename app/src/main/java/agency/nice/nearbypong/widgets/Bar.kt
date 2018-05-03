package agency.nice.nearbypong.widgets

import agency.nice.nearbypong.R
import agency.nice.nearbypong.ui.core.Constants
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView


/**
 * Created by ferranribell on 07/09/2017.
 */

class Bar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    companion object {
        var SIDE_LEFT: Int = 0
        var SIDE_RIGHT: Int = 1
    }

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

    fun setDarkColour(){
        setImageResource(R.drawable.rectangle_black)
    }


    fun getTheLeft(): Float {
        return x - width / 2
    }

    fun getTheRight(): Float {
        return x + width / 2
    }

    fun getTheTop(): Float {
        return y
    }

    fun getTheBottom(): Float {
        return y + height
    }

    fun setHeight(size: Int) {
        var params = layoutParams
        params.height = size
        layoutParams = params
    }

    fun changeBarSize(difference: Int) {
        if (difference < 0) {
            var barHeight = resources.getDimensionPixelOffset(R.dimen.bar_height)
            val porcentage: Float = (Constants.MAX_GOALS + difference).toFloat() / Constants.MAX_GOALS.toFloat()
            height = (barHeight * porcentage).toInt()
        }
    }

    fun blink() {
        val blinkanimation = AlphaAnimation(1f, 0.5f) // Change alpha from fully visible to invisible
        blinkanimation.duration = 100 // duration - half a second
        blinkanimation.interpolator = LinearInterpolator() // do not alter animation rate
        blinkanimation.repeatCount = 1 // Repeat animation infinitely
        blinkanimation.repeatMode = Animation.REVERSE
        startAnimation(blinkanimation)
    }
}