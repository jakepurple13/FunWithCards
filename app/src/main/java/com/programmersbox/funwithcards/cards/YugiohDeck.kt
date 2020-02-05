package com.programmersbox.funwithcards.cards

class YugiohDeckException(message: String?) : Exception(message)
enum class DeckType { MAIN, SIDE, EXTRA }
class YugiohDeck {

    private val mainDeck: Deck<YugiohCard> = Deck()
    private val extraDeck: Deck<YugiohCard> = Deck()
    private val sideDeck: Deck<YugiohCard> = Deck()

    operator fun get(deckType: DeckType) = when (deckType) {
        DeckType.MAIN -> mainDeck
        DeckType.EXTRA -> extraDeck
        DeckType.SIDE -> sideDeck
    }

    @Throws(YugiohDeckException::class)
    fun addToDeck(vararg card: YugiohCard, deckType: DeckType = DeckType.MAIN) = card.forEach { addToDeck(it, deckType) }

    @Throws(YugiohDeckException::class)
    fun addToDeck(card: YugiohCard, deckType: DeckType = DeckType.MAIN) = when (deckType) {
        DeckType.MAIN, DeckType.EXTRA -> addToDeck(card)
        DeckType.SIDE -> addToDeck(sideDeck, card)
    }.also {
        if (!sideDeckCheck()) throw YugiohDeckException("Side Deck can only have 15 cards")
        if (!extraDeckCheck()) throw YugiohDeckException("Extra Deck can only have 15 cards")
        if (!mainDeckCheck()) throw YugiohDeckException("Main Deck can have between 40 and 60 cards")
    }

    @Throws(YugiohDeckException::class)
    private fun addToDeck(card: YugiohCard): Boolean = when (card.type) {
        in TypeType.deckTypes -> addToDeck(mainDeck, card)
        in TypeType.extraDeckTypes -> addToDeck(extraDeck, card)
        TypeType.TOKEN -> false
        else -> throw YugiohDeckException("This card cannot be placed in extra deck or main deck ${card.type}")
    }

    @Throws(YugiohDeckException::class)
    private fun addToDeck(deck: Deck<YugiohCard>, card: YugiohCard) =
        if (fullCheck(card))
            throw YugiohDeckException(
                "Cannot have more than 3 \"${card.name}\" cards." +
                        " Main Deck: ${mainDeck.deck.count { it.id == card.id }}," +
                        " Side Deck: ${sideDeck.deck.count { it.id == card.id }}," +
                        " Extra Deck: ${extraDeck.deck.count { it.id == card.id }}"
            )
        else deck.addCard(card)

    private fun mainDeckCheck() = mainDeck.deck.count { it.type in TypeType.deckTypes }.let { it <= 60 }
    private fun extraDeckCheck() = extraDeck.deck.count { it.type in TypeType.extraDeckTypes } <= 15
    private fun sideDeckCheck() = sideDeck.deck.size <= 15
    private fun fullCheck(card: YugiohCard) = (sideDeck.deck + mainDeck.deck).count { it.id == card.id } >= 3
}

@Throws(YugiohDeckException::class)
fun Deck<YugiohCard>.addToDeck(card: YugiohCard): Boolean =
    if (deck.count { it == card } >= 3) throw YugiohDeckException("Cannot have more than 3 cards of ${card.name}") else addCard(card)

