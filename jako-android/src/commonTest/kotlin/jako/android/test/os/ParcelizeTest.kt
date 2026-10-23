package jako.android.test.os

import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.parcelableCreator
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

enum class AccountRole {
    GUEST,
    MEMBER,
    ADMIN
}

@Parcelize
data class TagItem(
    val id: Int,
    val label: String
) : Parcelable

@Parcelize
object EmptySingletonPayload : Parcelable

@Parcelize
data class PrimitiveAndNullablePayload(
    val flag: Boolean,
    val byteVal: Byte,
    val shortVal: Short,
    val charVal: Char,
    val intVal: Int,
    val longVal: Long,
    val floatVal: Float,
    val doubleVal: Double,
    val text: String,
    val nullableInt: Int?,
    val nullableLong: Long?,
    val nullableString: String?,
    val role: AccountRole,
    val nullableRole: AccountRole?
) : Parcelable {
    @IgnoredOnParcel
    var transientCounter: Int = 42
}

@Parcelize
class ArrayPayload(
    val bytes: ByteArray,
    val booleans: BooleanArray,
    val chars: CharArray,
    val ints: IntArray,
    val longs: LongArray,
    val floats: FloatArray,
    val doubles: DoubleArray,
    val nullableInts: IntArray?
) : Parcelable

@Parcelize
data class ComplexUserProfile(
    val userId: Long,
    val username: String,
    val primaryTag: TagItem,
    val secondaryTag: TagItem?,
    val tags: List<TagItem>,
    val roles: Set<String>,
    val scores: Map<String, Int>,
    val avatarUri: Uri?,
    val bounds: Rect,
    val anchor: Point,
    val extras: Bundle
) : Parcelable

data class CustomVersion(val major: Int, val minor: Int)

object CustomVersionParceler : Parceler<CustomVersion> {
    override fun create(parcel: Parcel): CustomVersion {
        return CustomVersion(parcel.readInt(), parcel.readInt())
    }

    override fun CustomVersion.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(major)
        parcel.writeInt(minor)
    }
}

@Parcelize
@TypeParceler<CustomVersion, CustomVersionParceler>
data class CustomParcelerPayload(
    val name: String,
    val version: CustomVersion
) : Parcelable

class ParcelizeTest {

    @Test
    fun testPrimitivesNullablesAndIgnoredOnParcel() {
        val original = PrimitiveAndNullablePayload(
            flag = true,
            byteVal = 0x5A.toByte(),
            shortVal = (-1234).toShort(),
            charVal = 'K',
            intVal = 987654,
            longVal = 123456789012345L,
            floatVal = 3.14f,
            doubleVal = 2.718281828,
            text = "Hello Cross-Platform @Parcelize",
            nullableInt = 77,
            nullableLong = null,
            nullableString = "non-null",
            role = AccountRole.ADMIN,
            nullableRole = null
        )
        original.transientCounter = 999

        val parcel = Parcel.obtain()
        try {
            assertEquals(0, original.describeContents())
            original.writeToParcel(parcel, 0)
            val bytes = parcel.marshall()
            assertTrue(bytes.isNotEmpty())

            val outParcel = Parcel.obtain()
            try {
                outParcel.unmarshall(bytes, 0, bytes.size)
                outParcel.setDataPosition(0)
                val restored = parcelableCreator<PrimitiveAndNullablePayload>().createFromParcel(outParcel)
                assertEquals(original, restored)
                // @IgnoredOnParcel property must not be serialized; retains default value 42
                assertEquals(42, restored.transientCounter)
            } finally {
                outParcel.recycle()
            }
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun testPrimitiveArraysRoundTrip() {
        val original = ArrayPayload(
            bytes = byteArrayOf(1, 2, 3, -4),
            booleans = booleanArrayOf(true, false, true),
            chars = charArrayOf('J', 'a', 'k', 'o'),
            ints = intArrayOf(10, 20, 30),
            longs = longArrayOf(100L, 200L),
            floats = floatArrayOf(1.5f, -2.5f),
            doubles = doubleArrayOf(0.125, 64.0),
            nullableInts = null
        )

        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            val restored = parcelableCreator<ArrayPayload>().createFromParcel(parcel)
            assertContentEquals(original.bytes, restored.bytes)
            assertContentEquals(original.booleans, restored.booleans)
            assertContentEquals(original.chars, restored.chars)
            assertContentEquals(original.ints, restored.ints)
            assertContentEquals(original.longs, restored.longs)
            assertContentEquals(original.floats, restored.floats)
            assertContentEquals(original.doubles, restored.doubles)
            assertNull(restored.nullableInts)
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun testComplexNestedCollectionsAndAndroidTypes() {
        val extras = Bundle().apply {
            putString("region", "CN")
            putInt("level", 9)
        }
        val original = ComplexUserProfile(
            userId = 1001L,
            username = "bennyhuo",
            primaryTag = TagItem(1, "Kotlin"),
            secondaryTag = null,
            tags = listOf(TagItem(2, "KMP"), TagItem(3, "Android")),
            roles = setOf("maintainer", "author"),
            scores = mapOf("compiler" to 100, "runtime" to 98),
            avatarUri = Uri.parse("https://github.com/bennyhuo.png"),
            bounds = Rect(10, 20, 110, 220),
            anchor = Point(50, 60),
            extras = extras
        )

        val parcel = Parcel.obtain()
        try {
            parcel.writeTypedObject(original, 0)
            parcel.writeTypedObject(null as ComplexUserProfile?, 0)
            parcel.setDataPosition(0)

            val restored = parcel.readTypedObject(parcelableCreator<ComplexUserProfile>())
            val restoredNull = parcel.readTypedObject(parcelableCreator<ComplexUserProfile>())
            assertNotNull(restored)
            assertNull(restoredNull)

            assertEquals(original.userId, restored.userId)
            assertEquals(original.username, restored.username)
            assertEquals(original.primaryTag, restored.primaryTag)
            assertNull(restored.secondaryTag)
            assertEquals(original.tags, restored.tags)
            assertEquals(original.roles, restored.roles)
            assertEquals(original.scores, restored.scores)
            assertEquals(original.avatarUri, restored.avatarUri)
            assertEquals(original.bounds, restored.bounds)
            assertEquals(original.anchor, restored.anchor)
            assertEquals("CN", restored.extras.getString("region"))
            assertEquals(9, restored.extras.getInt("level"))
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun testTypedListCreatorArraySingletonAndBundle() {
        val list = listOf(TagItem(10, "A"), TagItem(20, "B"))
        val parcel = Parcel.obtain()
        try {
            parcel.writeTypedList(list)
            EmptySingletonPayload.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)

            val restoredList: List<TagItem>? = parcel.createTypedArrayList(parcelableCreator<TagItem>())
            assertEquals<List<TagItem>?>(list, restoredList)

            val singleton = parcelableCreator<EmptySingletonPayload>().createFromParcel(parcel)
            assertSame(EmptySingletonPayload, singleton)
        } finally {
            parcel.recycle()
        }

        val arr = parcelableCreator<TagItem>().newArray(3)
        assertEquals(3, arr.size)
        assertNull(arr[0])

        val bundle = Bundle()
        val tag = TagItem(7, "BundleTag")
        bundle.putParcelable("tag", tag)
        assertEquals(tag, bundle.getParcelable<TagItem>("tag"))
    }

    @Test
    fun testDirectCreatorObjectAndTypeParceler() {
        val original = CustomParcelerPayload("JakoPlugin", CustomVersion(2, 4))
        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            val restored = CustomParcelerPayload.CREATOR.createFromParcel(parcel)
            assertEquals(original, restored)
        } finally {
            parcel.recycle()
        }
    }
}
