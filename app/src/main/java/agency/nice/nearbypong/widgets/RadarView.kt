package agency.nice.nearbypong.widgets

import agency.nice.nearbypong.R
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.util.concurrent.TimeUnit


/**
 * Created by fernando.moyano on 22/11/2017.
 */
class RadarView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ANIMATION_MILLIS: Int = 6
    private val initialRadious: Float = 19.4f
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private var timerAnimator: ValueAnimator? = null
    private var circleCount: Int = 0
    private var finish: Boolean = false
    private lateinit var onAnimationFinished: OnAnimationFinished

    /**
     * Colour types for circles
     */
    enum class CircleType {
        ACCENT,
        REGULAR,
        DARK
    }


    fun setDeviceDimensions(width: Int, height: Int) {
        deviceWidth = width
        deviceHeight = height
    }


    fun getConcentricCirclesPaint(circleType: CircleType): Paint {
        var paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.apply {
            isAntiAlias = true
            isDither = true
            strokeWidth = 2f
            style = Paint.Style.STROKE
            color = when (circleType) {
                CircleType.ACCENT -> ContextCompat.getColor(context, R.color.colorAccent)
                CircleType.REGULAR -> ContextCompat.getColor(context, R.color.colorPrimary)
                CircleType.DARK -> ContextCompat.getColor(context, R.color.transparent)
            }
        }
        return paint
    }

    fun getCenterCirclePaint(circleType: CircleType): Paint {
        var paint = Paint()
        paint.apply {
            isAntiAlias = true
            isDither = true
            color = when (circleType) {
                CircleType.ACCENT -> ContextCompat.getColor(context, R.color.colorAccent)
                CircleType.REGULAR -> ContextCompat.getColor(context, R.color.colorPrimary)
                CircleType.DARK -> ContextCompat.getColor(context, R.color.transparent)
            }
        }
        return paint
    }


    private fun drawProgress(progress: Int) {
        circleCount = progress
        invalidate()
    }

    fun stop() {
        if (timerAnimator != null && timerAnimator!!.isRunning()) {
            timerAnimator!!.cancel()
            timerAnimator = null
        }
    }

    fun setAnimationFinishedCallback(onAnimationFinished: OnAnimationFinished) {
        this.onAnimationFinished = onAnimationFinished
    }


    fun start(andFinish: Boolean) {
        stop()
        finish = andFinish

        timerAnimator = ValueAnimator.ofInt(0, ANIMATION_MILLIS)
        timerAnimator?.let {
            it.apply {
                duration = (TimeUnit.SECONDS.toMillis((ANIMATION_MILLIS - 1).toLong()))
                interpolator = (LinearInterpolator())
                addUpdateListener({ animation ->
                    drawProgress(animation.animatedValue as Int)
                })
                repeatCount = if (andFinish) 0 else ValueAnimator.INFINITE
                start()
            }
        }

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas?.drawCircle((deviceWidth / 2).toFloat(), (deviceHeight / 4).toFloat(), initialRadious, getCenterCirclePaint(CircleType.ACCENT))

        if (!finish) {
            var i = 0
            while (i < 6) {
                canvas?.drawCircle((deviceWidth / 2).toFloat(), (deviceHeight / 4).toFloat(),
                        (i / 15f * deviceWidth / 2) + initialRadious, getConcentricCirclesPaint(if (circleCount == i) CircleType.ACCENT else CircleType.REGULAR))
                i++
            }
        } else {
            var radiusIndex = ANIMATION_MILLIS - circleCount
            canvas?.drawCircle((deviceWidth / 2).toFloat(), (deviceHeight / 4).toFloat(), (radiusIndex / 15f * deviceWidth / 2) + initialRadious,
                    getConcentricCirclesPaint(CircleType.REGULAR))
            var i = 0
            while (i < radiusIndex) {
                canvas?.drawCircle((deviceWidth / 2).toFloat(), (deviceHeight / 4).toFloat(),
                        (i / 15f * deviceWidth / 2) + initialRadious, getConcentricCirclesPaint(CircleType.REGULAR))
                i++
            }
            if (circleCount == ANIMATION_MILLIS) {
                canvas?.drawCircle((deviceWidth / 2).toFloat(), (deviceHeight / 4).toFloat(), initialRadious, getCenterCirclePaint(CircleType.DARK))
                onAnimationFinished.onAnimationFinish()
            }
        }
    }


    interface OnAnimationFinished {
        fun onAnimationFinish()
    }
}