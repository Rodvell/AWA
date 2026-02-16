package com.curso.android.module3.amiibo.data.remote.model

import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * DTO para el detalle completo de un Amiibo.
 * Incluye información adicional como juegos compatibles y fechas de lanzamiento.
 */
@Serializable
data class AmiiboDetailResponse(
    val amiibo: List<AmiiboDetailDto>
)

@Serializable
data class AmiiboDetailDto(
    val head: String,
    val tail: String,
    val name: String,
    val character: String,
    @SerialName("gameSeries")
    val gameSeries: String,
    @SerialName("amiiboSeries")
    val amiiboSeries: String,
    val type: String,
    val image: String,
    val release: ReleaseDto? = null,
    @SerialName("games3DS")
    val games3DS: List<GameDto>? = null,
    @SerialName("gamesSwitch")
    val gamesSwitch: List<GameDto>? = null,
    @SerialName("gamesWiiU")
    val gamesWiiU: List<GameDto>? = null
)

@Serializable
data class ReleaseDto(
    val au: String? = null,
    val eu: String? = null,
    val jp: String? = null,
    val na: String? = null
)

@Serializable
data class GameDto(
    val gameName: String,
    val gameID: List<String>? = null
)

/**
 * Modelo de dominio para el detalle del Amiibo.
 */
data class AmiiboDetail(
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
    val compatibleGames: List<CompatibleGame>
)

@Serializable
data class CompatibleGame(
    val name: String,
    val platform: String
)

/**
 * Convierte el DTO a modelo de dominio.
 */
fun AmiiboDetailDto.toDetail(): AmiiboDetail {
    val games = mutableListOf<CompatibleGame>()

    games3DS?.forEach { game ->
        games.add(CompatibleGame(name = game.gameName, platform = "3DS"))
    }
    gamesSwitch?.forEach { game ->
        games.add(CompatibleGame(name = game.gameName, platform = "Switch"))
    }
    gamesWiiU?.forEach { game ->
        games.add(CompatibleGame(name = game.gameName, platform = "Wii U"))
    }

    return AmiiboDetail(
        id = "$head-$tail",
        name = name,
        character = character,
        gameSeries = gameSeries,
        amiiboSeries = amiiboSeries,
        type = type,
        imageUrl = image,
        releaseNA = release?.na,
        releaseEU = release?.eu,
        releaseJP = release?.jp,
        releaseAU = release?.au,
        compatibleGames = games.distinctBy { it.name }.sortedBy { it.name }
    )
}

private val json = Json { ignoreUnknownKeys = true }

/**
 * Convierte AmiiboDetail a Entity para guardar en Room.
 */
fun AmiiboDetail.toEntity(): AmiiboDetailEntity {
    return AmiiboDetailEntity(
        id = id,
        name = name,
        character = character,
        gameSeries = gameSeries,
        amiiboSeries = amiiboSeries,
        type = type,
        imageUrl = imageUrl,
        releaseNA = releaseNA,
        releaseEU = releaseEU,
        releaseJP = releaseJP,
        releaseAU = releaseAU,
        compatibleGamesJson = json.encodeToString(compatibleGames)
    )
}

/**
 * Convierte Entity de Room a modelo de dominio.
 */
fun AmiiboDetailEntity.toDomainModel(): AmiiboDetail {
    val games = try {
        json.decodeFromString<List<CompatibleGame>>(compatibleGamesJson)
    } catch (e: Exception) {
        emptyList()
    }

    return AmiiboDetail(
        id = id,
        name = name,
        character = character,
        gameSeries = gameSeries,
        amiiboSeries = amiiboSeries,
        type = type,
        imageUrl = imageUrl,
        releaseNA = releaseNA,
        releaseEU = releaseEU,
        releaseJP = releaseJP,
        releaseAU = releaseAU,
        compatibleGames = games
    )
}
