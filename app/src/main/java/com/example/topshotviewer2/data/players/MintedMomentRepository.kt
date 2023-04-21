package com.example.topshotviewer2.data.players

import android.util.Log
import apolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.topshotviewer2.MintedMomentsQuery
import com.example.topshotviewer2.model.MintedMoment
import com.example.topshotviewer2.model.MintedMomentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MintedMomentRepository {
    suspend fun getMoments(): MintedMomentList {

        return withContext(Dispatchers.IO) {
            val response: ApolloResponse<MintedMomentsQuery.Data> =
                apolloClient.query(MintedMomentsQuery(ownerFlowAddress = Optional.Present("63e0a50d19e02110"))).execute()

            val momentList: List<MintedMomentsQuery.Data3> =
                response.data?.searchMintedMoments?.data?.searchSummary?.data?.data?.filterNotNull().orEmpty()

            val moments = momentList.map {moment -> toMintedMoment(moment)}
            MintedMomentList(moments = moments)
        }
    }

    private fun toMintedMoment(momentData: MintedMomentsQuery.Data3): MintedMoment {
        var playerTitle: String = ""
        momentData.onMintedMoment?.play?.stats?.playerName?.let { playerTitle = it }
        var tierName: String = ""
        momentData.onMintedMoment?.tier?.let { tierName = it.name }
        var serialNumber: String = ""
        momentData.onMintedMoment?.flowSerialNumber?.let { serialNumber = it }
        var thumbnail: String = ""
        momentData.onMintedMoment?.assetPathPrefix?.let { thumbnail = "${it}Hero_2880_2880_Black.jpg?quality=60&width=480" }

        return MintedMoment(playerTitle = playerTitle, tierName = tierName, serialNumber = serialNumber, thumbnail = thumbnail)
    }
}
