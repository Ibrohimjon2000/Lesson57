package uz.mobiler.lesson57.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class MusicModel(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    var author:String,
    var name: String,
    val duration:Long=0,
    @ColumnInfo(name = "image_path")
    var imagePath:String,
    @ColumnInfo(name = "music_path")
    var musicPath:String
):Serializable