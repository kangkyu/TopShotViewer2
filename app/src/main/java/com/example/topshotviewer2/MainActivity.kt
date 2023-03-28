package com.example.topshotviewer2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.topshotviewer2.ui.theme.TopShotViewer2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopShotViewer2Theme {
                PlayerList()
            }
        }
    }
}

@Composable
fun PlayerList() {
    var response: ApolloResponse<PlayerListQuery.Data>? by remember { mutableStateOf(null) }
    var playerList by remember { mutableStateOf(emptyList<PlayerListQuery.Data1>()) }
    LaunchedEffect(Unit) {
        response = apolloClient.query(PlayerListQuery()).execute()
        Log.d("PlayerList", "Success ${response?.data}")
        playerList = response?.data?.allPlayers?.data?.filterNotNull().orEmpty()
    }
    LazyColumn {
        items(playerList) { player ->
            Card {
                Text(text = player.name.toString())
            }
        }
    }
}
