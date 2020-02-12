package com.programmersbox.funwithcards.cards

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

fun <T> Deck<T>.toDeckJson(): String = Gson().toJson(deck)
fun <T> Deck<T>.toPrettyDeckJson(): String = GsonBuilder().setPrettyPrinting().create().toJson(deck)

/**
 * Takes [this] and coverts it to a [Deck]
 * @param adapters Pair<Class<*>, Any>
 *
 * -----------------
 *
 * First - The class to register to.
 *
 * -----------------
 *
 * Second - This object must implement at least one of
 * @see com.google.gson.TypeAdapter
 * @see com.google.gson.InstanceCreator
 * @see com.google.gson.JsonSerializer
 * @see com.google.gson.JsonDeserializer
 */
inline fun <reified T> String?.toDeck(vararg adapters: Pair<Class<*>, Any>): Deck<T>? = try {
    Deck(
        GsonBuilder().apply { adapters.forEach { registerTypeAdapter(it.first, it.second) } }.create()
            .fromJson<List<T>>(this, object : TypeToken<List<T>>() {}.type)!!
    )
} catch (e: Exception) {
    e.printStackTrace()
    null
}
