package agency.nice.nearbypong.helpers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics


/**
 * Created by ferranribell on 19/09/2017.
 */

val PLAY_GAME = "play_game"
val FRIEND_FOUND = "friend_found"
val RETRY_CONNECTION = "retry_connection"
val GAME_OVER = "game_over"
val DEVICE_ID = "DeviceID"


fun trackEvent(context: Context, eventName: String, userId: String) {
    val bundle = Bundle()
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userId)
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, DEVICE_ID)
    FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
}