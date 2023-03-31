import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://public-api.nbatopshot.com/graphql")
    .addHttpInterceptor(UserAgentInterceptor("kangkyu1111@gmail.com"))
    .build()

private class UserAgentInterceptor(val userAgent: String) : HttpInterceptor {
    override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
        return chain.proceed(request.newBuilder().addHeader("User-Agent", userAgent).build())
    }
}
