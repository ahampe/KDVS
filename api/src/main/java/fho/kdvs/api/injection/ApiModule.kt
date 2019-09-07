package fho.kdvs.api.injection

import dagger.Module
import dagger.Provides
import fho.kdvs.api.endpoint.CoverArtArchiveEndpoint
import fho.kdvs.api.endpoint.MusicBrainzEndpoint
import fho.kdvs.api.endpoint.SpotifyEndpoint
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class ApiModule {

    @Provides
    fun provideSpotifyEndpoint(): SpotifyEndpoint {
        val retrofit = buildRetrofit(SpotifyEndpoint.BASE_URL)
        return retrofit.create(SpotifyEndpoint::class.java)
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

    private fun buildRetrofit(baseUrl: String,
                              factory: Converter.Factory = GsonConverterFactory.create()): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(factory)
            .build()
    }

}
