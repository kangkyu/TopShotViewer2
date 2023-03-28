import com.apollographql.apollo3.ApolloClient

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://public-api.nbatopshot.com/graphql")
    .build()

