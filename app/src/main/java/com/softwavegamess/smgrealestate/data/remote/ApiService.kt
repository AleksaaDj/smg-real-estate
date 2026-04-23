package com.softwavegamess.smgrealestate.data.remote

import com.softwavegamess.smgrealestate.data.remote.dto.PropertiesResponseDto
import retrofit2.http.GET

interface ApiService {
    @GET("properties")
    suspend fun getProperties(): PropertiesResponseDto
}
