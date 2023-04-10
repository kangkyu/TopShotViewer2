package com.example.topshotviewer2.data.players

import android.util.Log
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.topshotviewer2.PlayerListQuery
import com.example.topshotviewer2.model.PlayerList
import com.example.topshotviewer2.model.PlayerListPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class PlayersRepository() {

    // for now, store these in memory
    private val favorites: MutableStateFlow<Set<String>>
        get() = MutableStateFlow<Set<String>>(setOf())

    fun observeFavorites(): Flow<Set<String>> = favorites

    suspend fun getPlayerList(): PlayerList {
        return withContext(Dispatchers.IO) {
            val response: ApolloResponse<PlayerListQuery.Data> =
                apolloClient.query(PlayerListQuery()).execute()
            Log.d("PlayerList", "Success ${response.data}")
            val playerlist: List<PlayerListQuery.Data1> =
                response.data?.allPlayers?.data?.filterNotNull().orEmpty()

            val players = playerlist.map { toPlayerListPlayer(it) }
            PlayerList(allPlayers = players)
        }
    }

    private fun toPlayerListPlayer(player: PlayerListQuery.Data1): PlayerListPlayer {
        var name: String = ""
        player.name?.let { name = it }
        return PlayerListPlayer(id = player.id, name = name)
    }
}
