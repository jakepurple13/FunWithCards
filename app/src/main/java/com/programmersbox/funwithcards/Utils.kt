package com.programmersbox.funwithcards


import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.programmersbox.funwithcards.cards.*
import okhttp3.OkHttpClient

val Context.defaultPrefs: SharedPreferences get() = getSharedPreferences("Prefs", Context.MODE_PRIVATE)

/**
 * converts [this] to a Json string
 */
fun Any?.toJson(): String = Gson().toJson(this)

/**
 * converts [this] to a Json string but its formatted nicely
 */
fun Any?.toPrettyJson(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)

/**
 * Takes [this] and coverts it to an object
 */
inline fun <reified T> String?.fromJson(): T? = try {
    GsonBuilder()
        .registerTypeAdapter(RaceType::class.java, JsonDeserializer<RaceType?> { json, _, _ -> RaceType(json.asString) })
        .registerTypeAdapter(TypeType::class.java, JsonDeserializer<TypeType?> { json, _, _ -> TypeType(json.asString) })
        .registerTypeAdapter(ArcheType::class.java, JsonDeserializer<ArcheType?> { json, _, _ -> ArcheType(json.asString) })
        .registerTypeAdapter(Deck.DeckListener::class.java, InstanceCreator<Deck.DeckListener<T>?> { null })
        .create()
        .fromJson(this, object : TypeToken<T>() {}.type)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun <T : ViewGroup> T.animateChildren(transition: Transition? = AutoTransition(), block: T.() -> Unit) =
    TransitionManager.beginDelayedTransition(this, transition).apply { block() }

fun Context.getCards() = resources.openRawResource(R.raw.cards)
    .bufferedReader()
    .readText()
    .fromJson<List<YugiohCard>>()!!

fun getApi(url: String): String? {
    val request = okhttp3.Request.Builder()
        .url(url)
        .get()
        .build()
    val response = OkHttpClient().newCall(request).execute()
    return if (response.code == 200) response.body!!.string() else null
}

fun printRuntimeInfo() {
    data class RuntimeInfo(
        val availableProcessors: Int = Runtime.getRuntime().availableProcessors(),
        val freeMemory: Long = Runtime.getRuntime().freeMemory(),
        val totalMemory: Long = Runtime.getRuntime().totalMemory(),
        val maxMemory: Long = Runtime.getRuntime().maxMemory()
    )
    Loged.f(RuntimeInfo())
}