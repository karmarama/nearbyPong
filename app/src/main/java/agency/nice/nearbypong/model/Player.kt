package agency.nice.nearbypong.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Created by ferranribell on 11/09/2017.
 */
@Entity(tableName = "player")
data class Player(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String,
        var side: Int,
        var score: Int
)