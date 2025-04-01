package com.example.mainactivity.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface PartidaDao {
    @Insert
    fun insertar(partida: Partida): Completable

    @Query("SELECT * FROM partidas")
    fun obtenerTodas(): Single<List<Partida>>
}


