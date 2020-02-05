package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.programmersbox.funwithcards.cards.YugiohCard
import kotlinx.android.synthetic.main.activity_card_info.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_info)
        card = intent.getStringExtra("card_info").fromJson<YugiohCard>()
        Loged.f(card)
        Glide.with(this).load(card?.card_images?.get(cardImages)?.image_url).into(cardImage)
        imageCountSet(card)
        cardImage.setOnClickListener { cardImages++ }
        relatedCards.setOnClickListener {
            startActivity(Intent(this, RelatedCardsActivity::class.java).apply { putExtra("related_card", card.toJson()) })
        }

    }
}