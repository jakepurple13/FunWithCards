package com.programmersbox.funwithcards

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.programmersbox.funwithcards.cards.YugiohCard
import kotlinx.android.synthetic.main.activity_related_cards.*

class RelatedCardsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_related_cards)
        val card = intent.getStringExtra("related_card").fromJson<YugiohCard>()
        relatedTitle.text = card?.name
        relatedCardView.adapter = CardAdapter(this, card?.findRelatedCards(getCards())?.toMutableList() ?: mutableListOf())
    }
}