/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.hocon

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.Serializable
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test

class HoconRootMapTest {
    private val configForRootMap = """
        key1 {
            a = "text1"
            b = 11
            }
        
        key2 {
            a = "text2"
            b = 12
        }
        
        key3 {
            a = "text3"
            b = 13
        }
    """
    private val configWithEmptyObject = "{}"
    private val configWithRootList = "[foo, bar]"
    private val emptyConfig = ""

    @Serializable
    data class CompositeValue(
            val a: String,
            val b: Int
    )

    @Test
    fun testConfigWithRootMap() {
        val config = ConfigFactory.parseString(configForRootMap)
        val obj = Hocon.decodeFromConfig<Map<String, CompositeValue>>(config)

        assertEquals(CompositeValue("text1", 11), obj["key1"])
        assertEquals(CompositeValue("text2", 12), obj["key2"])
        assertEquals(CompositeValue("text3", 13), obj["key3"])
    }

    @Test
    fun testEmptyObjectDecode() {
        val config = ConfigFactory.parseString(configWithEmptyObject)
        //non-null map decoded from empty object as empty map
        val map = Hocon.decodeFromConfig<Map<String, String>>(config)
        assertTrue(map.isEmpty())

        //nullable map decoded from empty object as null - not obvious
        val nullableMap = Hocon.decodeFromConfig<Map<String, String>?>(config)
        assertNull(nullableMap)

        //root-level list in config not supported but nullable list can be decoded from empty object
        val nullableList = Hocon.decodeFromConfig<List<String>?>(config)
        assertNull(nullableList)
    }

    @Ignore
    @Test
    fun testErrors() {
        //because com.typesafe:config lib not support list in root we can't decode non-null list
        val config = ConfigFactory.parseString(configWithEmptyObject)
        var error: Exception? = null
        try {
            Hocon.decodeFromConfig<List<String>>(config)
        } catch (e: NoSuchElementException) {
            error = e
        }
        assertNotNull(error)

        //because com.typesafe:config lib not support list in root it fails while parsing
        error = null
        try {
            ConfigFactory.parseString(configWithRootList)
        } catch (e: Exception) {
            error = e
        }
        assertNotNull(error)

        //com.typesafe:config lib parse empty config as empty object
        ConfigFactory.parseString(emptyConfig)
    }
}
