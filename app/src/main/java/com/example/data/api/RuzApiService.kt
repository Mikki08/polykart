package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// DTOs for RUZ SPbPU API (https://ruz.spbstu.ru/api/v1/ruz/)
data class RuzGroupDto(
    val id: Int,
    val name: String,
    val level: Int? = null,
    val faculty: RuzFacultyDto? = null
)

data class RuzFacultyDto(
    val id: Int? = null,
    val name: String? = null,
    val abbr: String? = null
)

data class RuzGroupsResponse(
    val groups: List<RuzGroupDto>? = emptyList()
)

data class RuzTeacherDto(
    val id: Int,
    @Json(name = "full_name") val fullName: String,
    val chair: String? = null
)

data class RuzTeachersResponse(
    val teachers: List<RuzTeacherDto>? = emptyList()
)

data class RuzBuildingDto(
    val id: Int,
    val name: String,
    val abbr: String? = null,
    val address: String? = null
)

data class RuzBuildingsResponse(
    val buildings: List<RuzBuildingDto>? = emptyList()
)

data class RuzAuditoryDto(
    val id: Int? = null,
    val name: String,
    val building: RuzBuildingDto? = null
)

data class RuzRoomsResponse(
    val rooms: List<RuzAuditoryDto>? = emptyList()
)

data class RuzLessonTypeDto(
    val id: Int? = null,
    val name: String? = null
)

data class RuzLessonDto(
    val subject: String? = null,
    @Json(name = "time_start") val timeStart: String? = null,
    @Json(name = "time_end") val timeEnd: String? = null,
    val typeObj: RuzLessonTypeDto? = null,
    val type: Int? = null,
    val auditories: List<RuzAuditoryDto>? = emptyList(),
    val teachers: List<RuzTeacherDto>? = emptyList(),
    val groups: List<RuzGroupDto>? = emptyList(),
    val additional_info: String? = null
)

data class RuzDayDto(
    val date: String,
    val weekday: Int,
    val lessons: List<RuzLessonDto>? = emptyList()
)

data class RuzScheduleResponse(
    val days: List<RuzDayDto>? = emptyList(),
    val week: RuzWeekInfoDto? = null
)

data class RuzWeekInfoDto(
    @Json(name = "date_start") val dateStart: String? = null,
    @Json(name = "date_end") val dateEnd: String? = null,
    @Json(name = "is_odd") val isOdd: Boolean? = null
)

interface RuzApiService {
    @GET("search/groups")
    suspend fun searchGroups(@Query("q") query: String): RuzGroupsResponse

    @GET("search/teachers")
    suspend fun searchTeachers(@Query("q") query: String): RuzTeachersResponse

    @GET("scheduler/{groupId}")
    suspend fun getGroupSchedule(
        @Path("groupId") groupId: Int,
        @Query("date") date: String? = null
    ): RuzScheduleResponse

    @GET("teachers/{teacherId}/scheduler")
    suspend fun getTeacherSchedule(
        @Path("teacherId") teacherId: Int,
        @Query("date") date: String? = null
    ): RuzScheduleResponse

    @GET("buildings")
    suspend fun getBuildings(): RuzBuildingsResponse

    @GET("buildings/{buildingId}/rooms")
    suspend fun getRoomsInBuilding(@Path("buildingId") buildingId: Int): RuzRoomsResponse

    @GET("buildings/{buildingId}/rooms/{roomId}/scheduler")
    suspend fun getRoomSchedule(
        @Path("buildingId") buildingId: Int,
        @Path("roomId") roomId: Int,
        @Query("date") date: String? = null
    ): RuzScheduleResponse
}

object RuzApiClient {
    private const val BASE_URL = "https://ruz.spbstu.ru/api/v1/ruz/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: RuzApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RuzApiService::class.java)
    }
}
