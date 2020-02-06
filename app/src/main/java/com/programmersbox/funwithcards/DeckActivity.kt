package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.funwithcards.cards.*
import kotlinx.android.synthetic.main.activity_deck.*
import kotlinx.android.synthetic.main.card_view.view.*

class DeckActivity : AppCompatActivity() {

    private var sortItem = SortItems.NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)
        val deck = getDecks().find { it.deckName == intent.getStringExtra("deck_info") }
        deckTitle.text = deck?.deckName
        val onUpdate: (YugiohCard?) -> Unit = {
            mainDeck.swapAdapterWith<DeckAdapter, DeckAdapter.ViewHolder>(true)
            { da -> DeckAdapter(this, deck!!.deck[DeckType.MAIN].deck.sortedWith(sortItem.sort), deck.deck, DeckType.MAIN, da.onUpdate) }
            extraDeck.swapAdapterWith<DeckAdapter, DeckAdapter.ViewHolder>(true)
            { da -> DeckAdapter(this, deck!!.deck[DeckType.EXTRA].deck.sortedWith(sortItem.sort), deck.deck, DeckType.EXTRA, da.onUpdate) }
            sideDeck.swapAdapterWith<DeckAdapter, DeckAdapter.ViewHolder>(true)
            { da -> DeckAdapter(this, deck!!.deck[DeckType.SIDE].deck.sortedWith(sortItem.sort), deck.deck, DeckType.SIDE, da.onUpdate) }
            it?.let { topCard -> deck?.topCard = topCard }
            val decks = getDecks()
            decks.removeIf { it.deckName == deck!!.deckName }
            decks.add(deck!!)
            saveDecks(decks)
            Loged.f(getDecks().map { it.deckName })
        }
        mainDeck.adapter =
            deck?.deck?.get(DeckType.MAIN)?.deck?.let { DeckAdapter(this, it.sortedWith(sortItem.sort), deck.deck, DeckType.MAIN, onUpdate) }
        extraDeck.adapter =
            deck?.deck?.get(DeckType.EXTRA)?.deck?.let { DeckAdapter(this, it.sortedWith(sortItem.sort), deck.deck, DeckType.EXTRA, onUpdate) }
        sideDeck.adapter =
            deck?.deck?.get(DeckType.SIDE)?.deck?.let { DeckAdapter(this, it.sortedWith(sortItem.sort), deck.deck, DeckType.SIDE, onUpdate) }

        sortBy.setOnClickListener {
            MaterialAlertDialogBuilder(this@DeckActivity)
                .setSingleChoiceItems(SortItems.values().map { it.name }.toTypedArray(), sortItem.ordinal) { dialog: DialogInterface, index: Int ->
                    sortItem = SortItems.values()[index]
                    onUpdate(null)
                    dialog.dismiss()
                }
                .setTitle("Sort by")
                .show()
        }
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
        ViewHolder(
            LayoutInflater.from(context).inflate(
                if (type == DeckType.MAIN) R.layout.card_view else R.layout.card_image_view, null, false
            )
        )

    override fun ViewHolder.onBind(item: YugiohCard) {
        glide.load(item.card_images.random().image_url_small).into(image)
        title?.text = item.name
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
                        0 -> fullDeck[type].remove(item).also { onUpdate(null) }
                        1 -> {
                            fullDeck[type].remove(item)
                            try {
                                when (type) {
                                    DeckType.MAIN, DeckType.EXTRA -> fullDeck.addToDeck(item, DeckType.SIDE)
                                    DeckType.SIDE -> fullDeck.addToDeck(item)
                                }
                            } catch (e: YugiohDeckException) {
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                fullDeck[type].addCard(item)
                            } finally {
                                onUpdate(null)
                            }
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
        val title: TextView? = itemView.cardTitle
    }
}