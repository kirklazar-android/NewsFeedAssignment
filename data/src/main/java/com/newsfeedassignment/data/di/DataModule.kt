package com.newsfeedassignment.data.di

import android.content.Context
import androidx.room.Room
import com.newsfeedassignment.core.repository.BookmarkRepository
import com.newsfeedassignment.core.repository.NewsRepository
import com.newsfeedassignment.data.network.ApiKeyInterceptor
import com.newsfeedassignment.data.network.GNewsApi
import com.newsfeedassignment.data.network.GNewsConfig
import com.newsfeedassignment.data.persistence.BookmarkDao
import com.newsfeedassignment.data.persistence.NewsDatabase
import com.newsfeedassignment.data.persistence.RoomBookmarkRepository
import com.newsfeedassignment.data.repository.HomeNewsRepository
import com.newsfeedassignment.data.sync.NewsFeedRefresher
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton fun moshi(): Moshi = Moshi.Builder().build()

    @Provides @Singleton
    fun httpClient(@Named("gnewsApiKey") apiKey: String): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(apiKey))
        .build()

    @Provides @Singleton
    fun api(client: OkHttpClient, moshi: Moshi): GNewsApi = Retrofit.Builder()
        .baseUrl(GNewsConfig.BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GNewsApi::class.java)

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): NewsDatabase = Room.databaseBuilder(context, NewsDatabase::class.java, "newsfeedassignment.db").build()

    @Provides fun bookmarkDao(database: NewsDatabase): BookmarkDao = database.bookmarkDao()

    @Provides fun bookmarkRepository(dao: BookmarkDao): BookmarkRepository = RoomBookmarkRepository(dao)

    @Provides fun homeNewsRepository(api: GNewsApi, database: NewsDatabase): HomeNewsRepository = HomeNewsRepository(api, database)

    @Provides fun newsFeedRefresher(api: GNewsApi, database: NewsDatabase): NewsFeedRefresher = NewsFeedRefresher(api, database)

    @Provides fun newsRepository(repository: HomeNewsRepository): NewsRepository = repository
}
