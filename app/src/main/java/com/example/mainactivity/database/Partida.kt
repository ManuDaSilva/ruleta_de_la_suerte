package com.example.mainactivity.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partidas")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreJugador: String,
    val fecha: String,
    val monedasGanadas: Int,
    val saldoFinal: Int,
)