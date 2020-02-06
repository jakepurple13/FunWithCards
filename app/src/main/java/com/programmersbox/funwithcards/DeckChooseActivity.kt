package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.funwithcards.cards.DeckType
import com.programmersbox.funwithcards.cards.YugiohCard
import com.programmersbox.funwithcards.cards.YugiohDeck
import kotlinx.android.synthetic.main.activity_deck_choose.*
import kotlinx.android.synthetic.main.card_view.view.*

fun Context.getDecks() = (defaultPrefs.getString("decks", null).fromJson<List<YugiohDeckState>>() ?: emptyList()).toMutableList()
fun Context.saveDecks(decks: List<YugiohDeckState>) = defaultPrefs.edit().putString("decks", decks.toJson()).apply()

class DeckChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck_choose)

        val deckList = getDecks()
        pickADeck.adapter = ChooseCardAdapter(this, deckList)

        newDeck.setOnClickListener {
            val input = EditText(this@DeckChooseActivity)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            MaterialAlertDialogBuilder(this@DeckChooseActivity)
                .setTitle("Enter a Deck Name")
                .setView(input)
                .setPositiveButton("Create") { _, _ ->
                    if (deckList.any { it.deckName == input.text.toString() }) {
                        Toast.makeText(this, "You already have a deck named ${input.text}", Toast.LENGTH_LONG).show()
                    } else {
                        deckList.add(YugiohDeckState(deckName = input.text.toString()))
                        saveDecks(deckList)
                        pickADeck.adapter = ChooseCardAdapter(this, deckList)
                    }
                }
                .setNegativeButton("Stop") { _, _ -> }
                .show()
        }

    }
}

@Suppress("FunctionName")
fun Intent(context: Context, cls: Class<*>, block: Intent.() -> Unit) = Intent(context, cls).apply(block)

data class YugiohDeckState(var topCard: YugiohCard? = null, val deckName: String, val deck: YugiohDeck = YugiohDeck())

class ChooseCardAdapter(private val context: Context, list: List<YugiohDeckState>) :
    DragSwipeAdapterTwo<YugiohDeckState, ChooseCardAdapter.ViewHolder>(list.toMutableList()) {

    private val glide = Glide.with(context)
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.deck_view, null, false))

    override fun ViewHolder.onBind(item: YugiohDeckState) {
        glide
            .load((item.topCard ?: item.deck[DeckType.MAIN].deck.firstOrNull())?.card_images?.random()?.image_url_small)
            .error(R.drawable.ic_launcher_foreground)
            .override(Target.SIZE_ORIGINAL)
            .into(image)
        title.text = item.deckName
        itemView.setOnClickListener { context.startActivity(Intent(context, DeckActivity::class.java) { putExtra("deck_info", item.deckName) }) }
        itemView.setOnLongClickListener {
            MaterialAlertDialogBuilder(context)
                .setItems(
                    arrayOf(
                        "Edit Deck",
                        "Delete Deck"
                    )
                ) { d, index ->
                    d.dismiss()
                    when (index) {
                        0 -> itemView.performClick()
                        else -> {
                            val decks = context.getDecks()
                            decks.removeIf { it.deckName == item.deckName }
                            list.removeIf { it.deckName == item.deckName }
                            context.saveDecks(decks)
                            this@ChooseCardAdapter.notifyDataSetChanged()
                        }
                    }
                }
                .show()
            true
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.yugiohCard
        val title: TextView = itemView.cardTitle
    }
}