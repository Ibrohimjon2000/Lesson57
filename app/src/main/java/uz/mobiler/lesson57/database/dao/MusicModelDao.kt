package uz.mobiler.lesson57.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import uz.mobiler.lesson57.database.entity.MusicModel

@Dao
interface MusicModelDao {

    @Insert
    fun addModel(musicModel: MusicModel)

    @Query("select*from musicmodel")
    fun getAllMusics(): List<MusicModel>

    @Query("select*from musicmodel where id=:id")
    fun getMusicById(id:Int):MusicModel
}