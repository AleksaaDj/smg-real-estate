package com.softwavegamess.smgrealestate.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.softwavegamess.smgrealestate.data.local.AppDatabase
import com.softwavegamess.smgrealestate.data.local.BookmarkEntity
import com.softwavegamess.smgrealestate.data.remote.ApiService
import com.softwavegamess.smgrealestate.data.remote.mapper.PropertyResponseMapper
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PropertyRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var repository: PropertyRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/").toString())
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(ApiService::class.java)
        repository = PropertyRepositoryImpl(
            api = api,
            mapper = PropertyResponseMapper(),
            bookmarkDao = database.bookmarkDao(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    @Test
    fun mergesBookmarkFlagsFromRoom() = runBlocking {
        database.bookmarkDao().insert(BookmarkEntity(propertyId = "42", createdAtMillis = 0L))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(samplePayload()),
        )

        val result = repository.getProperties()
        assertTrue(result.isSuccess)

        val property = result.getOrNull()!!.single { it.id == "42" }
        assertTrue(property.isBookmarked)
    }

    @Test
    fun toggleBookmarkPersistsRow() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(samplePayload()),
        )

        repository.getProperties()
        val toggled = repository.toggleBookmark("42")

        assertTrue(toggled.isSuccess)
        assertTrue(toggled.getOrNull() == true)
        assertTrue(database.bookmarkDao().isBookmarked("42"))
    }

    @Test
    fun toggleBookmarkRemovesRowWhenPresent() = runBlocking {
        database.bookmarkDao().insert(BookmarkEntity(propertyId = "42", createdAtMillis = 0L))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(samplePayload()),
        )

        repository.getProperties()
        val toggled = repository.toggleBookmark("42")

        assertTrue(toggled.isSuccess)
        assertEquals(false, toggled.getOrNull())
        assertFalse(database.bookmarkDao().isBookmarked("42"))
    }

    private fun samplePayload(): String =
        """
        {
          "results": [
            {
              "id": "42",
              "listingType": { "type": "TOP" },
              "listing": {
                "id": "42",
                "prices": { "currency": "CHF", "buy": { "price": 250000 } },
                "address": { "street": "Main", "postalCode": "8000", "locality": "Zürich" },
                "localization": {
                  "primary": "de",
                  "de": {
                    "text": { "title": "Nice place" },
                    "attachments": [
                      { "type": "IMAGE", "url": "https://example.com/a.jpg" }
                    ]
                  }
                }
              }
            }
          ]
        }
        """.trimIndent()
}
