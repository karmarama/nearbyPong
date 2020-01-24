package agency.nice.nearbypong.helpers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics


/**
 * Created by ferranribell on 19/09/2017.
 */

const val PLAY_GAME = "play_game"
const val FRIEND_FOUND = "friend_found"
const val RETRY_CONNECTION = "retry_connection"
const val GAME_OVER = "game_over"
const val DEVICE_ID = "DeviceID"

fun trackEvent(context: Context, eventName: String, userId: String) {
    val bundle = Bundle()
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userId)
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, DEVICE_ID)
    FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
}