package agency.nice.nearbypong.widgets

import agency.nice.nearbypong.R
import agency.nice.nearbypong.model.BallParameters
import agency.nice.nearbypong.ui.core.SIDE_LEFT
import agency.nice.nearbypong.ui.core.SIDE_RIGHT
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import kotlinx.android.synthetic.main.view_ball.view.*
import java.util.*


/**
 * Created by ferranribell on 07/09/2017.
 */

class Ball @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    var topLimit: Int = 0
    var bottomLimit: Int = 0
    var sideLimit: Int = 0
    var side: Int = SIDE_LEFT
    var velocity = 1500f
    var friction = 0.1f
    private var velocityY = velocity
    private var velocityX = velocity
    lateinit var listener: BallListener

    private val flingAnimationX = FlingAnimation(this, DynamicAnimation.X).setFriction(friction)
    val flingAnimationY = FlingAnimation(this, DynamicAnimation.Y).setFriction(friction)

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.view_ball, this, true)
        setFlingListener()
    }

    fun rotate() {
        flingAnimationX.cancel()
        flingAnimationY.cancel()
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )

        rotate.duration = 1000
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = Animation.INFINITE
        outside.animation = rotate
    }

    fun stopRotate() {
        outside?.let {
            animation.cancel()
        }
    }

    private fun setFlingListener() {
        flingAnimationY.addUpdateListener { _, value, _ ->
            if (value <= topLimit) {
                velocityY = velocity
                flingAnimationY.setStartVelocity(velocityY).start()
            }
            if (value >= bottomLimit) {
                velocityY = -1 * velocity
                flingAnimationY.setStartVelocity(velocityY).start()
            }
        }

        flingAnimationX.addUpdateListener { _, value, _ ->

            velocityX = velocity
            if (side == SIDE_LEFT && value <= -1 * width / 2) {
                listener.goal()
            }

            if (side == SIDE_LEFT && value > sideLimit + width / 2) {
                listener.sendBallData(this.x, this.y, velocityX, velocityY)
            }

            if (side == SIDE_RIGHT && value <= -1 * width / 2) {
                listener.sendBallData(this.x, this.y, velocityX, velocityY)
            }

            if (side == SIDE_RIGHT && value + width / 2 > sideLimit) {
                listener.goal()
            }
        }
    }

    interface BallListener {
        fun goal()
        fun sendBallData(posX: Float, posY: Float, velocityX: Float, velocityY: Float)
    }

    fun barBounce() {
        velocityX += Random().nextFloat() * if (Random().nextBoolean()) 100f else -100f
        velocityY += Random().nextFloat() * if (Random().nextBoolean()) 100f else -100f
        if (!flingAnimationX.isRunning) {
            start()
        } else {
            flingAnimationX.setStartVelocity(if (side == SIDE_LEFT) velocityX else -1 * velocityX)
                .start()
        }
    }


    fun crazyBounce() {
        insideFilled.animate().alpha(0f).start()
        velocityX = velocity + Random().nextFloat() * if (Random().nextBoolean()) 100f else -100f
        velocityY = velocity + Random().nextFloat() * if (Random().nextBoolean()) 100f else -100f

        flingAnimationY.addUpdateListener { _, value, _ ->
            if (value <= topLimit + height) {
                velocityY = velocity
                flingAnimationY.setStartVelocity(velocityY).start()
            }
            if (value >= bottomLimit - height) {
                velocityY = -1500f
                flingAnimationY.setStartVelocity(velocityY).start()
            }
        }

        flingAnimationX.addUpdateListener { _, value, _ ->
            velocityX = velocity
            if (value + width <= -1) {
                velocityX = velocity
                flingAnimationX.setStartVelocity(velocityX).start()
            }

            if (value > sideLimit - width) {
                velocityX *= -1
                flingAnimationX.setStartVelocity(velocityX).start()
            }
        }

        flingAnimationX.setStartVelocity(velocityX).start()
        flingAnimationY.setStartVelocity(velocityY).start()
    }

    fun moveBallTo(ballParameters: BallParameters) {
        visibility = View.VISIBLE
        x = if (side == SIDE_LEFT) sideLimit.toFloat() - width else 5f
        y = ballParameters.posY
        velocityY = ballParameters.velocityY
        velocityX =
            if (side == SIDE_LEFT) -1 * ballParameters.velocityX else ballParameters.velocityX
        flingAnimationX.setStartVelocity(velocityX).start()
        flingAnimationY.setStartVelocity(velocityY).start()
    }

    fun stop() {
        visibility = View.GONE
        cancelFlings()
    }

    fun cancelFlings() {
        flingAnimationX.cancel()
        flingAnimationY.cancel()
    }

    fun start() {
        velocityX = if (side == SIDE_RIGHT) -1 * velocity else velocity
        velocityY = if (Random().nextBoolean()) velocity else -1 * velocity
        flingAnimationX.setStartVelocity(velocityX).start()
        flingAnimationY.setStartVelocity(velocityY).start()
    }

    fun colourDarkBlue() {
        var color = ContextCompat.getColor(context, R.color.blue)
        inside.setColorFilter(color)
        insideFilled.setColorFilter(color)
        outside.setColorFilter(color)

        AlphaAnimation(1f, 0.5f).apply {
            duration = 100 // duration - half a second
            interpolator = LinearInterpolator() // do not alter animation rate
            repeatCount = 1 // Repeat animation infinitely
            repeatMode = Animation.REVERSE
            startAnimation(this)
        }
    }

    fun changeDrawable(side: Int) {
        var color = ContextCompat.getColor(context, R.color.orange)
        if (side == SIDE_LEFT) {
            color = ContextCompat.getColor(context, R.color.cyan)
        }
        inside.setColorFilter(color)
        insideFilled.setColorFilter(color)
        outside.setColorFilter(color)

        AlphaAnimation(1f, 0.5f).apply {
            duration = 100 // duration - half a second
            interpolator = LinearInterpolator() // do not alter animation rate
            repeatCount = 1 // Repeat animation infinitely
            repeatMode = Animation.REVERSE
            startAnimation(this)
        }

    }

    fun changeAlphaToMax() {
        insideFilled.alpha = 1f
    }

    fun fillInsideColor() {
        changeAlphaToMax()
        insideFilled.animate().alpha(0f).start()
    }
}