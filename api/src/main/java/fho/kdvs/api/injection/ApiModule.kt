package fho.kdvs.api.injection

import dagger.Module
import dagger.Provides
import fho.kdvs.api.endpoint.CoverArtArchiveEndpoint
import fho.kdvs.api.endpoint.MusicBrainzEndpoint
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.endpoint.YouTubeEndpoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


@Module
class ApiModule {

    @Provides
    fun provideSpotifyEndpoint(): SpotifyEndpoint {
        val retrofit = buildRetrofit(SpotifyEndpoint.BASE_URL)
        return retrofit.create(SpotifyEndpoint::class.java)
    }

    @Provides
    fun provideYouTubeEndpoint(): YouTubeEndpoint {
        val retrofit = buildRetrofit(YouTubeEndpoint.BASE_URL)
        return retrofit.create(YouTubeEndpoint::class.java)
    }

    @Provides
    fun provideMusicBrainzEndpoint(): MusicBrainzEndpoint {
        val retrofit = buildRetrofit(MusicBrainzEndpoint.BASE_URL)
        return retrofit.create(MusicBrainzEndpoint::class.java)
    }

    @Provides
    fun provideCoverArtArchiveEndpoint(): CoverArtArchiveEndpoint {
        val retrofit = buildRetrofit(CoverArtArchiveEndpoint.BASE_URL)
        return retrofit.create(CoverArtArchiveEndpoint::class.java)
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
        client.connectTimeout(15, TimeUnit.SECONDS)
        client.readTimeout(15, TimeUnit.SECONDS)
        client.writeTimeout(15, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(client.build())
            .build()
    }
}
