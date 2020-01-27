package agency.nice.nearbypong.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class Player(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String,
        var side: Int,
        var score: Int
)
