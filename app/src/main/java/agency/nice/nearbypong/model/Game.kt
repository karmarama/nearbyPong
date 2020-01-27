package agency.nice.nearbypong.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by ferranribell on 11/09/2017.
 */
const val GAME_TAG = "game"
const val GOAL = "goal"
const val SENT = "sent"
const val SIDE_LEFT = 0
const val SIDE_RIGHT = 1
const val MAX_GOALS = 5
const val CONFIRMATION_VALUE = "established"

@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var playerOneId: String = "",
    var playerTwoId: String = "",
    var scoreOne: Int = 0,
    var scoreTwo: Int = 0
) : Serializable