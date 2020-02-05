package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.funwithcards.cards.DeckType
import com.programmersbox.funwithcards.cards.YugiohCard
import com.programmersbox.funwithcards.cards.YugiohDeck
import kotlinx.android.synthetic.main.activity_deck.*
import kotlinx.android.synthetic.main.card_view.view.*

class DeckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)
        val deck = getDecks().find { it.deckName == intent.getStringExtra("deck_info") }
        deckTitle.text = deck?.deckName
        val onUpdate: (YugiohCard?) -> Unit = {
            mainDeck.swapAdapterWith<DeckAdapter, DeckAdapter.ViewHolder>(true) { da ->
                DeckAdapter(this, deck!!.deck[DeckType.MAIN].deck, deck.deck, DeckType.MAIN, da.onUpdate)
            }
            extraDeck.swapAdapterWith<DeckAdapter, DeckAdapter.ViewHolder>(true) { da ->
                DeckAdapter(this, deck!!.deck[DeckType.EXTRA].deck, deck.deck, DeckType.EXTRA, da.onUpdate)
            }
            sideDeck.swapAdapterWith<DeckAdapter, DeckAdapter.ViewHolder>(true) { da ->
                DeckAdapter(this, deck!!.deck[DeckType.SIDE].deck, deck.deck, DeckType.SIDE, da.onUpdate)
            }
            it?.let { topCard -> deck?.topCard = topCard }
            val decks = getDecks()
            decks.removeIf { it.deckName == deck!!.deckName }
            decks.add(deck!!)
            saveDecks(decks)
            Loged.f(getDecks()) { it.deckName }
        }
        mainDeck.adapter = deck?.deck?.get(DeckType.MAIN)?.deck?.let { DeckAdapter(this, it, deck.deck, DeckType.MAIN, onUpdate) }
        extraDeck.adapter = deck?.deck?.get(DeckType.EXTRA)?.deck?.let { DeckAdapter(this, it, deck.deck, DeckType.EXTRA, onUpdate) }
        sideDeck.adapter = deck?.deck?.get(DeckType.SIDE)?.deck?.let { DeckAdapter(this, it, deck.deck, DeckType.SIDE, onUpdate) }
    }
}

class DeckAdapter(
    private val context: Context,
    list: List<YugiohCard>,
    private val fullDeck: YugiohDeck,
    private val type: DeckType,
    val onUpdate: (YugiohCard?) -> Unit
) :
    DragSwipeAdapterTwo<YugiohCard, DeckAdapter.ViewHolder>(list.toMutableList()) {

    private val glide = Glide.with(context)
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view, null, false))

    override fun ViewHolder.onBind(item: YugiohCard) {
        glide
            .load(item.card_images.random().image_url_small)
            .error(R.drawable.ic_launcher_foreground)
            .override(Target.SIZE_ORIGINAL)
            .into(image)
        title.text = item.name
        itemView.setOnClickListener {
            context.startActivity(Intent(context, CardInfoActivity::class.java).apply { putExtra("card_info", item.toJson()) })
        }
        itemView.setOnLongClickListener {
            val whereTo = arrayOf(
                "Remove",
                if (type == DeckType.MAIN || type == DeckType.EXTRA) "Move to Side" else "Move to Main",
                "Make Top Card"
            )
            MaterialAlertDialogBuilder(context)
                .setTitle("Remove or Move?")
                .setItems(whereTo) { d, index ->
                    d.dismiss()
                    when (index) {
                        0 -> fullDeck[type].remove(item).also { this@DeckAdapter.notifyDataSetChanged() }
                        1 -> {
                            fullDeck[type].remove(item)
                            when (type) {
                                DeckType.MAIN, DeckType.EXTRA -> fullDeck.addToDeck(item, DeckType.SIDE)
                                DeckType.SIDE -> fullDeck.addToDeck(item)
                            }
                            onUpdate(null)
                        }
                        2 -> onUpdate(item)
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