package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.funwithcards.cards.DeckType
import com.programmersbox.funwithcards.cards.YugiohCard
import com.programmersbox.funwithcards.cards.YugiohDeckException
import kotlinx.android.synthetic.main.activity_card_info.*
import kotlinx.android.synthetic.main.card_full_info.view.*

class CardInfoActivity : AppCompatActivity() {

    private var card: YugiohCard? = null
    private var cardImages = 0
        set(value) {
            field = if (value >= card?.card_images?.size ?: 1 || value < 0) 0 else value
            Glide.with(this).load(card?.card_images?.get(field)?.image_url).into(cardImage)
            imageCountSet(card)
        }

    @SuppressLint("SetTextI18n")
    private fun imageCountSet(card: YugiohCard?) {
        image_count.text = "${cardImages + 1}/${card?.card_images?.size ?: 1}"
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_info)
        card = intent.getStringExtra("card_info").fromJson<YugiohCard>()
        Loged.f(card)
        Glide.with(this).load(card?.card_images?.get(cardImages)?.image_url)
            .placeholder(R.drawable.backofcard).error(R.drawable.backofcard).into(cardImage)
        imageCountSet(card)
        cardImage.setOnClickListener { cardImages++ }
        relatedCards.setOnClickListener {
            startActivity(Intent(this, RelatedCardsActivity::class.java).apply { putExtra("related_card", card.toJson()) })
        }

        moreInfo.setOnClickListener {

            val fullView = layoutInflater.inflate(R.layout.card_full_info, null, false)
            with(fullView) {
                fun <T> T?.setView(view: TextView, text: (T) -> String) = if (this == null) {
                    view.visibility = View.GONE
                } else {
                    view.text = text(this)
                }
                titleOfCard.text = card?.name
                cardDescription.text = card?.desc
                cardID.text = "ID: ${card?.id}"
                cardRace.text = "Race: ${card?.race}"
                card?.atk.setView(cardATK) { "ATK/ $it" }
                card?.def.setView(cardDEF) { "DEF/ $it" }
                card?.linkval.setView(cardLinkVal) { "LINK-$it" }
                card?.scale.setView(cardScale) { "Scale: $it" }
                card?.linkmarkers.setView(cardLinkMarkers) { "Link Markers: ${it.joinToString { marker -> marker.name }}" }
                card?.archetype.setView(cardArchetype) { "Archetype: $it" }
                card?.attribute.setView(cardAttribute) { "Attribute: $it" }
                card?.level.setView(cardLevel) { "Level $it" }
                Glide.with(this)
                    .load(card?.card_images?.get(cardImages)?.image_url_small)
                    .placeholder(R.drawable.backofcard).error(R.drawable.backofcard)
                    .override(Target.SIZE_ORIGINAL)
                    .into(yugiohCard)
            }

            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            fullView.layoutParams = lp
            MaterialAlertDialogBuilder(this).setView(fullView).show()
        }

        cardImage.setOnLongClickListener {
            val decks = getDecks()
            MaterialAlertDialogBuilder(this)
                .setTitle("Add to Deck")
                .setItems(decks.map { it.deckName }.toTypedArray()) { dialog, index ->
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Which Deck?")
                        .setItems(arrayOf("Main/Extra", "Side")) { d1, index2 ->
                            d1.dismiss()
                            try {
                                decks[index].deck.addToDeck(card!!, if (index2 == 0) DeckType.MAIN else DeckType.SIDE)
                            } catch (e: YugiohDeckException) {
                                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                            } finally {
                                saveDecks(decks)
                            }
                        }
                        .show()
                }
                .show()
            true
        }
    }
}
