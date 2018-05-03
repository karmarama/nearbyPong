package agency.nice.nearbypong.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.provider.Settings
import android.view.WindowManager


/**
 * Created by ferranribell on 07/09/2017.
 */
val Activity.screenHeight: Int
    get() {
        return this.screenSize.y
    }

val Activity.screenWidth: Int
    get() {
        return this.screenSize.x
    }

val Activity.screenSize: Point
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val screenSize = Point()
        display.getRealSize(screenSize)

        return screenSize
    }

fun Activity.getUserId(): String {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}
