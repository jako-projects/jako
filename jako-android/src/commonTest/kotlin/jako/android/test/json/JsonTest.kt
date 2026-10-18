package jako.android.test.json

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import android.util.MalformedJsonException
import java.io.StringReader
import java.io.StringWriter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONTokener
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JsonTest {

    @Test
    fun testJSONObjectParseAndSerialize() {
        val raw = """
            {
                "name": "jako",
                "version": 1,
                "active": true,
                "pi": 3.14,
                "big": 9876543210,
                "nothing": null,
                "tags": ["kmp", "native", "android"],
                "nested": {"key": "val"}
            }
        """.trimIndent()

        val obj = JSONObject(raw)
        assertEquals(8, obj.length())
        assertEquals("jako", obj.getString("name"))
        assertEquals(1, obj.getInt("version"))
        assertTrue(obj.getBoolean("active"))
        assertEquals(3.14, obj.getDouble("pi"), 1e-6)
        assertEquals(9876543210L, obj.getLong("big"))
        assertTrue(obj.isNull("nothing"))
        assertTrue(obj.isNull("non_existent"))
        assertTrue(obj.has("nothing"))
        assertFalse(obj.has("non_existent"))
        assertSame(JSONObject.NULL, obj.get("nothing"))

        val tags = obj.getJSONArray("tags")
        assertEquals(3, tags.length())
        assertEquals("kmp", tags.getString(0))
        assertEquals("native", tags.getString(1))
        assertEquals("android", tags.getString(2))

        val nested = obj.getJSONObject("nested")
        assertEquals("val", nested.getString("key"))

        // Opt fallbacks
        assertEquals("fallback", obj.optString("missing", "fallback"))
        assertEquals(42, obj.optInt("missing", 42))
        assertEquals(99L, obj.optLong("missing", 99L))
        assertTrue(obj.optBoolean("missing", true))
        assertNull(obj.optJSONObject("missing"))
        assertNull(obj.optJSONArray("missing"))

        // Type coercion in getters (string <-> number / boolean)
        obj.put("strNum", "123")
        obj.put("strBool", "TRUE")
        assertEquals(123, obj.getInt("strNum"))
        assertEquals(123L, obj.getLong("strNum"))
        assertTrue(obj.getBoolean("strBool"))

        // Putting null removes key
        obj.put("strNum", null as Any?)
        assertFalse(obj.has("strNum"))

        // Accumulate
        val acc = JSONObject()
        acc.accumulate("item", "first")
        assertEquals("first", acc.getString("item"))
        acc.accumulate("item", "second")
        val itemArr = acc.getJSONArray("item")
        assertEquals(2, itemArr.length())
        assertEquals("first", itemArr.getString(0))
        assertEquals("second", itemArr.getString(1))

        // Round-trip serialization
        val serialized = obj.toString()
        val reparsed = JSONObject(serialized)
        assertEquals("jako", reparsed.getString("name"))
        assertEquals(3, reparsed.getJSONArray("tags").length())
    }

    @Test
    fun testJSONArrayAndWrap() {
        val arr = JSONArray("[10, \"hello\", false, null, [1, 2], {\"a\": 1}]")
        assertEquals(6, arr.length())
        assertEquals(10, arr.getInt(0))
        assertEquals("hello", arr.getString(1))
        assertFalse(arr.getBoolean(2))
        assertTrue(arr.isNull(3))
        assertEquals(2, arr.getJSONArray(4).getInt(1))
        assertEquals(1, arr.getJSONObject(5).getInt("a"))

        // Put at index with gap padding
        val sparse = JSONArray()
        sparse.put(2, "atTwo")
        assertEquals(3, sparse.length())
        assertTrue(sparse.isNull(0))
        assertTrue(sparse.isNull(1))
        assertEquals("atTwo", sparse.getString(2))

        // Wrap Map, Collection, primitive array
        val fromMap = JSONObject(mapOf("k" to listOf(1, 2, 3)))
        assertEquals(3, fromMap.getJSONArray("k").length())
        val fromPrimArray = JSONArray(intArrayOf(7, 8, 9))
        assertEquals(3, fromPrimArray.length())
        assertEquals(8, fromPrimArray.getInt(1))

        // quote & escape
        assertEquals("\"a\\/b\\\"c\\n\"", JSONObject.quote("a/b\"c\n"))

        // Forbidden NaN / Infinity
        assertFailsWith<JSONException> {
            JSONObject().put("bad", Double.NaN)
        }
    }

    @Test
    fun testJSONTokenerAndJSONStringer() {
        val tokener = JSONTokener("// line comment\n/* block */ {\"a\": 'single_quoted', b: 0x10}")
        val obj = tokener.nextValue() as JSONObject
        assertEquals("single_quoted", obj.getString("a"))
        assertEquals(16, obj.getInt("b"))

        val str = JSONStringer()
            .`object`()
            .key("arr")
            .array()
            .value(1L)
            .value(true)
            .value("ok")
            .endArray()
            .endObject()
            .toString()
        assertEquals("{\"arr\":[1,true,\"ok\"]}", str)
    }

    @Test
    fun testJsonReaderAndJsonWriter() {
        val writerOut = StringWriter()
        val writer = JsonWriter(writerOut)
        writer.beginObject()
            .name("id").value(101L)
            .name("title").value("Kotlin Native")
            .name("enabled").value(true)
            .name("empty").nullValue()
            .name("scores")
            .beginArray()
            .value(95.5)
            .value(100L)
            .endArray()
            .name("skipMe")
            .beginObject()
            .name("deep").value("ignored")
            .endObject()
            .endObject()
        writer.close()

        val jsonText = writerOut.toString()
        assertEquals(
            "{\"id\":101,\"title\":\"Kotlin Native\",\"enabled\":true,\"empty\":null,\"scores\":[95.5,100],\"skipMe\":{\"deep\":\"ignored\"}}",
            jsonText
        )

        val reader = JsonReader(StringReader(jsonText))
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek())
        reader.beginObject()
        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                "id" -> assertEquals(101, reader.nextInt())
                "title" -> assertEquals("Kotlin Native", reader.nextString())
                "enabled" -> assertTrue(reader.nextBoolean())
                "empty" -> {
                    assertEquals(JsonToken.NULL, reader.peek())
                    reader.nextNull()
                }
                "scores" -> {
                    reader.beginArray()
                    assertEquals(95.5, reader.nextDouble(), 1e-6)
                    assertEquals(100L, reader.nextLong())
                    assertFalse(reader.hasNext())
                    reader.endArray()
                }
                "skipMe" -> reader.skipValue()
                else -> throw AssertionError("Unexpected field: $name")
            }
        }
        reader.endObject()
        assertEquals(JsonToken.END_DOCUMENT, reader.peek())
        reader.close()

        // Malformed JSON test in strict mode vs lenient mode
        val strictReader = JsonReader(StringReader("{unquoted: 'value'}"))
        strictReader.beginObject()
        assertFailsWith<MalformedJsonException> {
            strictReader.nextName()
        }
        strictReader.close()

        val lenientReader = JsonReader(StringReader("// comment\n{unquoted: 'value'}"))
        lenientReader.isLenient = true
        assertTrue(lenientReader.isLenient())
        lenientReader.beginObject()
        assertEquals("unquoted", lenientReader.nextName())
        assertEquals("value", lenientReader.nextString())
        lenientReader.endObject()
        lenientReader.close()
    }
}
