package agency.nice.nearbypong.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by ferranribell on 11/09/2017.
 */
const val GAME_TAG = "game"

@Entity(tableName = "game")
data class Game(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var playerOneId: String = "",
    var playerTwoId: String = "",
    var scoreOne: Int = 0,
    var scoreTwo: Int = 0
) : Serializable