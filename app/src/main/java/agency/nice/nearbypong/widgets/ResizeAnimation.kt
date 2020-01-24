package agency.nice.nearbypong.widgets

import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by fernando.moyano on 01/11/2017.
 */

private const val MIN_SIZE: Int = 24

class ResizeAnimation : Animation() {

    private lateinit var view: View
    private var startHeight: Int = 0
    private var startWidth: Int = 0
    private var targetHeight: Int = -1
    private var targetWidth: Int = -1
    private var isIncreaseSize: Boolean = false

    fun init(view: View, targetHeight: Int, targetWidth: Int, isIncreaseSize: Boolean) {
        this.view = view
        this.targetHeight = targetHeight
        this.targetWidth = targetWidth
        this.startHeight = view.height
        this.isIncreaseSize = isIncreaseSize
        startWidth = view.width
        initialize(view.width, view.height, targetHeight, targetWidth)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        var newHeight: Int
        var newWidth: Int
        if (isIncreaseSize) {
            Log.d("TRANSFORMATION", "targetHeight " + targetHeight)
            newHeight = (targetHeight * interpolatedTime).toInt()
            Log.d("TRANSFORMATION", "newHeight " + newHeight)
            newWidth = (targetWidth * interpolatedTime).toInt()
        } else {
            newHeight = (targetHeight - (targetHeight * interpolatedTime)).toInt()
            Log.d("TRANSFORMATION", "newHeight " + newHeight)
            newWidth = (targetWidth - (targetWidth * interpolatedTime)).toInt()
        }
        view.layoutParams.height = newHeight
        view.layoutParams.width = newWidth
        view.pivotX = -newWidth.toFloat()
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}