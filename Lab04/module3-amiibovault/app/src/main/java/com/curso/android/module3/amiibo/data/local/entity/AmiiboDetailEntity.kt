package com.curso.android.module3.amiibo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity para almacenar el detalle completo de un Amiibo.
 *
 * Se guarda en una tabla separada porque:
 * 1. No todos los Amiibos tendrán su detalle cargado
 * 2. El detalle se carga bajo demanda (cuando el usuario lo solicita)
 * 3. Los juegos compatibles se guardan como JSON string para simplificar
 */
@Entity(tableName = "amiibo_details")
data class AmiiboDetailEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val character: String,
    val gameSeries: String,
    val amiiboSeries: String,
    val type: String,
    val imageUrl: String,
    val releaseNA: String?,
    val releaseEU: String?,
    val releaseJP: String?,
    val releaseAU: String?,
    // Juegos compatibles guardados como JSON string
    val compatibleGamesJson: String
)
