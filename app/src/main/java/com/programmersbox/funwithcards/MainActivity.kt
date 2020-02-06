package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmersbox.funwithcards.cards.DeckType
import com.programmersbox.funwithcards.cards.SortItems
import com.programmersbox.funwithcards.cards.YugiohCard
import com.programmersbox.funwithcards.cards.YugiohDeckException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_view.view.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Loged.FILTER_BY_CLASS_NAME = "programmersbox"
        Loged.TAG = "Cards"
        Loged.OTHER_CLASS_FILTER = { !it.contains("Framing") }

        printRuntimeInfo()

        val cards = getCards()
        cardView.adapter = CardAdapter(this, cards.toMutableList())

        var comparator = compareBy<YugiohCard> { it.name }
            .thenBy { it.desc }
            .thenBy { it.race }
            .thenBy { it.archetype }
            .thenBy { it.type }

        val filter: (YugiohCard) -> Boolean = {
            val searchText = search_info.text.toString()
            it.name.contains(searchText, true) || it.desc.contains(searchText, true)
        }

        search_info.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) =
                cardView.swapAdapterWith<CardAdapter, CardAdapter.ViewHolder>(true) {
                    CardAdapter(this@MainActivity, cards.filter(filter).sortedWith(comparator).toMutableList())
                }
        })

        var sortCheck = 0

        sort_button.setOnClickListener {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setSingleChoiceItems(SortItems.values().map { it.name }.toTypedArray(), sortCheck) { dialog: DialogInterface, index: Int ->
                    sortCheck = index
                    comparator = SortItems.values()[index].sort
                    cardView.swapAdapterWith<CardAdapter, CardAdapter.ViewHolder>(true) {
                        CardAdapter(this@MainActivity, SortItems.values()[index].sortWith(it.list).toMutableList())
                    }
                    dialog.dismiss()
                }
                .setTitle("Sort by")
                .show()
        }

        myDecks.setOnClickListener { startActivity(Intent(this@MainActivity, DeckChooseActivity::class.java)) }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : RecyclerView.Adapter<VH>, VH : RecyclerView.ViewHolder> RecyclerView.swapAdapterWith(
    removeAndRecycleExistingViews: Boolean,
    block: (T) -> RecyclerView.Adapter<VH>
) = swapAdapter(block(adapter as T), removeAndRecycleExistingViews)

abstract class DragSwipeAdapterTwo<T, VH : RecyclerView.ViewHolder>(list: MutableList<T>) : DragSwipeAdapter<T, VH>(list) {
    abstract fun VH.onBind(item: T)
    override fun onBindViewHolder(holder: VH, position: Int) = holder.onBind(list[position])
}

class CardAdapter(private val context: Context, list: MutableList<YugiohCard>) : DragSwipeAdapterTwo<YugiohCard, CardAdapter.ViewHolder>(list) {

    private val glide = Glide.with(context)
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view, null, false))

    override fun ViewHolder.onBind(item: YugiohCard) {
        glide.load(item.card_images.random().image_url_small)
            .placeholder(R.drawable.backofcard)
            .error(R.drawable.backofcard)
            .override(Target.SIZE_ORIGINAL).into(image)
        title.text = item.name
        itemView.setOnClickListener {
            context.startActivity(Intent(context, CardInfoActivity::class.java).apply { putExtra("card_info", item.toJson()) })
        }
        itemView.setOnLongClickListener {
            val decks = context.getDecks()
            MaterialAlertDialogBuilder(context)
                .setTitle("Add to Deck")
                .setItems(decks.map { it.deckName }.toTypedArray()) { dialog, index ->
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Which Deck?")
                        .setItems(arrayOf("Main/Extra", "Side")) { d1, index2 ->
                            d1.dismiss()
                            try {
                                decks[index].deck.addToDeck(item, if (index2 == 0) DeckType.MAIN else DeckType.SIDE)
                            } catch (e: YugiohDeckException) {
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                            } finally {
                                context.saveDecks(decks)
                            }
                        }
                        .show()
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
