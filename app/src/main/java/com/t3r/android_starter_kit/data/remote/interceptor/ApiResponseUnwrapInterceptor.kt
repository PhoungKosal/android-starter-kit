package com.t3r.android_starter_kit.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject

/**
 * Unwraps the backend's envelope format:
 *   { "success": true, "message": "...", "data": { ... } }
 * so that Retrofit converters receive only the inner `data` payload.
 */
class ApiResponseUnwrapInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val contentType = response.body?.contentType()

        // Only process JSON responses
        if (contentType?.subtype != "json") return response

        val rawBody = response.body?.string() ?: return response

        val unwrapped = try {
            val json = JSONObject(rawBody)
            if (json.has("data") && json.has("success")) {
                val data = json.get("data")
                data.toString()
            } else {
                rawBody
            }
        } catch (_: Exception) {
            rawBody
        }

        val newBody = unwrapped.toResponseBody(contentType)
        return response.newBuilder().body(newBody).build()
    }
}
