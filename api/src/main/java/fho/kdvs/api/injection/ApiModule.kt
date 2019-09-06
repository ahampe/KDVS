package fho.kdvs.api.injection

import dagger.Module
import dagger.Provides
import fho.kdvs.api.endpoint.CoverArtArchiveEndpoint
import fho.kdvs.api.endpoint.MusicBrainzEndpoint
import fho.kdvs.api.endpoint.SpotifyEndpoint
import fho.kdvs.api.endpoint.YouTubeEndpoint
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

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
    fun provideCovertArtArchiveEndpoint(): CoverArtArchiveEndpoint {
        val retrofit = buildRetrofit(CoverArtArchiveEndpoint.BASE_URL)
        return retrofit.create(CoverArtArchiveEndpoint::class.java)
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
}
