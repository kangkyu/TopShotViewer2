package com.example.topshotviewer2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.example.topshotviewer2.ui.theme.TopShotViewer2Theme
import kotlinx.coroutines.launch

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
        item { Text("Players") }
        items(playerList) { player ->
            PlayerView(player)
        }
    }
}

@Composable
fun PlayerView(player: PlayerListQuery.Data1) {
    var detailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var response: ApolloResponse<PlayerDetailsQuery.Data>? by remember { mutableStateOf(null) }
    var playerDetails by remember { mutableStateOf(PlayerDetailsQuery.PlayerData(jerseyNumber = null, position = null)) }
    Card(modifier = Modifier.padding(all = 8.dp)) {
        Column() {
            Row {
                Text(
                    text = player.name.toString(),
                    modifier = Modifier.padding(all = 4.dp)
                )
                OutlinedButton(onClick = {
                    detailed = !detailed
                    if (detailed) {
                        scope.launch {
                            response = apolloClient.query(PlayerDetailsQuery(playerId = player.id)).execute()
                            response?.data?.getPlayerDataWithCurrentStats?.playerData?.apply {
                                playerDetails = this
                            }
                        }
                    }
                }) {
                    Text(if (detailed) "Back" else "Details")
                }
            }
            if (detailed) {
                Row {
                    if (playerDetails != null && playerDetails.jerseyNumber != null && playerDetails.position != null) {
                        Text(text = "Jersey: ${playerDetails.jerseyNumber} Position: ${playerDetails.position}")
                    } else {
                        Text("player details not available")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TopShotViewer2Theme {
        PlayerView(PlayerListQuery.Data1(id="1630462", name="Aari McDonald"))
    }
}
