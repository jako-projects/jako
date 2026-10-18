package com.bennyhuo.jako.sample

import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils
import android.util.JsonReader
import android.util.SparseArray
import java.io.StringReader
import org.json.JSONArray
import org.json.JSONObject

object AndroidDemo {

    data class AndroidReport(
        val endpointUrl: String,
        val activeFeatures: String,
        val viewportArea: Int,
        val parsedTokenCount: Int
    )

    fun run(): AndroidReport {
        // 1. android.net.Uri & Uri.Builder
        val uri = Uri.parse("https://api.example.com/v1")
            .buildUpon()
            .appendPath("projects")
            .appendPath("jako")
            .appendQueryParameter("target", "native")
            .fragment("overview")
            .build()

        // 2. org.json.JSONObject & JSONArray
        val payload = JSONObject()
            .put("module", "jako-android")
            .put("features", JSONArray().put("Uri").put("JSONObject").put("SparseArray"))

        // 3. android.util.SparseArray & android.text.TextUtils
        val sparse = SparseArray<String>()
        val featuresArray = payload.getJSONArray("features")
        for (i in 0 until featuresArray.length()) {
            sparse.put((i + 1) * 10, featuresArray.getString(i))
        }
        val labels = ArrayList<String>(sparse.size())
        for (i in 0 until sparse.size()) {
            labels.add("${sparse.keyAt(i)}:${sparse.valueAt(i)}")
        }
        val joinedFeatures = TextUtils.join(", ", labels)

        // 4. android.graphics.Rect geometry
        val viewport = Rect(0, 0, 1920, 1080)
        viewport.inset(160, 40)
        val area = viewport.width() * viewport.height()

        // 5. android.util.JsonReader streaming parser
        var tokenCount = 0
        JsonReader(StringReader(payload.toString())).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "module" -> {
                        reader.nextString()
                        tokenCount++
                    }
                    "features" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.nextString()
                            tokenCount++
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }

        return AndroidReport(
            endpointUrl = uri.toString(),
            activeFeatures = joinedFeatures,
            viewportArea = area,
            parsedTokenCount = tokenCount
        )
    }
}
