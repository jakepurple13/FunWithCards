package com.programmersbox.funwithcards.cards

import kotlin.properties.Delegates
import kotlin.random.Random

@DslMarker
annotation class DeckMarker

@DslMarker
annotation class CardMarker

data class Card(val value: Int, val suit: Suit) {
    val valueTen: Int get() = if (value > 10) 10 else value
    val symbol: String = when (value) {
        13 -> "K"
        12 -> "Q"
        11 -> "J"
        1 -> "A"
        else -> "$value"
    }

    @DeckMarker
    class CardBuilder {
        var value by Delegates.notNull<Int>()
        var suit by Delegates.notNull<Suit>()

        companion object {
            @CardMarker
            operator fun invoke(block: CardBuilder.() -> Unit) = CardBuilder().apply(block).let { Card(it.value, it.suit) }

            @CardMarker
            fun cardBuilder(block: CardBuilder.() -> Unit) = invoke(block)
        }
    }

    companion object {
        val RandomCard: Card get() = Card(Random.nextInt(1, 13), Suit.values().random())
        operator fun get(suit: Suit) = Card(Random.nextInt(1, 13), suit)
        operator fun get(vararg suit: Suit) = suit.map { Card(Random.nextInt(1, 13), it) }
        operator fun get(num: Int) = Card(num, Suit.values().random())
        operator fun get(vararg num: Int) = num.map { Card(it, Suit.values().random()) }
    }
}

enum class Suit(val printableName: String, val symbol: String, val unicodeSymbol: String) {
    HEARTS("Hearts", "H", "♥"),
    DIAMONDS("Diamonds", "D", "♦"),
    CLUBS("Clubs", "C", "♣"),
    SPADES("Spades", "S", "♠")
}

class Deck<T>(vararg cards: T) {
    private val deckOfCards = mutableListOf(*cards)
    val size: Int get() = deckOfCards.size
    val deck: List<T> get() = deckOfCards
    private var listener: DeckListener<T>? = null
    fun addDeckListener(listener: DeckListener<T>) {
        this.listener = listener
    }

    fun addDeckListener(listener: DeckListenerBuilder<T>.() -> Unit) {
        this.listener = DeckListenerBuilder.addListener(listener)
    }

    private fun MutableList<T>.addCards(vararg card: T) = addAll(card).also { listener?.onAdd(card.toList()) }
    private fun MutableList<T>.addCards(card: Iterable<T>) = addAll(card).also { listener?.onAdd(card.toList()) }
    private fun MutableList<T>.drawCard(index: Int) = removeAt(index).also { listener?.onDraw(it) }
    private fun MutableList<T>.removeCards(cards: Iterable<T>) = removeAll(cards).also { cards.forEach { c -> listener?.onDraw(c) } }

    fun draw() = deckOfCards.drawCard(deckOfCards.lastIndex)
    fun addCard(vararg card: T) = deckOfCards.addCards(*card)
    fun addCards(card: Iterable<T>) = deckOfCards.addCards(card)
    fun findCard(predicate: (T) -> Boolean) = deckOfCards.find(predicate)
    fun findCards(predicate: (T) -> Boolean) = deckOfCards.filter(predicate)
    fun drawCards(predicate: (T) -> Boolean) = findCards(predicate).also { deckOfCards.removeCards(it) }
    fun shuffle() = deckOfCards.shuffle().also { listener?.onShuffle() }
    fun isEmpty() = deckOfCards.isEmpty()
    fun isNotEmpty() = deckOfCards.isNotEmpty()
    override fun toString(): String = deckOfCards.toString()
    operator fun invoke(deck: Deck<T>) = deckOfCards.addCards(deck.deckOfCards)
    operator fun invoke(vararg cards: T) = deckOfCards.addCards(*cards)
    operator fun invoke(card: Iterable<T>) = deckOfCards.addCards(card)
    operator fun invoke(listener: DeckListenerBuilder<T>.() -> Unit) = addDeckListener(listener)
    operator fun get(index: Int) = deckOfCards[index]
    operator fun get(range: IntRange) = deck.subList(range.first, range.last)
    operator fun set(index: Int, card: T) = deckOfCards.set(index, card)
    operator fun minus(amount: Int) = mutableListOf<T>().apply { repeat(amount) { this += draw() } }.toList()
    operator fun plusAssign(card: T) = addCard(card).let { Unit }
    operator fun minusAssign(card: T) = remove(card).let { Unit }
    operator fun iterator() = deckOfCards.iterator()
    operator fun contains(card: T) = card in deckOfCards
    fun remove(card: T) = deckOfCards.remove(card).also { if (it) listener?.onDraw(card) }

    companion object {
        fun defaultDeck() = Deck(*Suit.values().map { suit -> (1..13).map { value -> Card(value, suit) } }.flatten().toTypedArray())
        operator fun plus(card: Card) = Deck(card)
    }

    interface DeckListener<T> {
        fun onAdd(cards: List<T>)
        fun onShuffle()
        fun onDraw(card: T)
    }

    @DeckMarker
    class DeckListenerBuilder<T> {

        private var drawCard: (T) -> Unit = {}

        @DeckMarker
        fun onDraw(block: (T) -> Unit) {
            drawCard = block
        }

        private var addCards: (List<T>) -> Unit = {}

        @DeckMarker
        fun onAdd(block: (List<T>) -> Unit) {
            addCards = block
        }

        private var shuffleDeck: () -> Unit = {}

        @DeckMarker
        fun onShuffle(block: () -> Unit) {
            shuffleDeck = block
        }

        internal fun build() = object : DeckListener<T> {
            override fun onAdd(cards: List<T>) = this@DeckListenerBuilder.addCards(cards)
            override fun onDraw(card: T) = drawCard(card)
            override fun onShuffle() = shuffleDeck()
        }

        companion object {
            @DeckMarker
            fun <T> addListener(block: DeckListenerBuilder<T>.() -> Unit): DeckListener<T> = DeckListenerBuilder<T>().apply(block).build()
        }

    }

    @DeckMarker
    class DeckBuilder<T> private constructor() {

        @DeckMarker
        val deckListener = DeckListenerBuilder<T>()

        private val cardList = mutableListOf<T>()

        @CardMarker
        val cards: List<T>
            get() = cardList

        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(block: Card.CardBuilder.() -> Unit) = Card.CardBuilder(block).let { cardList.add(Card(it.value, it.suit)) }

        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(value: Int, suit: Suit) = cardList.add(Card(value, suit))

        @CardMarker
        fun cards(vararg cards: T) = cardList.addAll(cards)

        @CardMarker
        fun deck(deck: Deck<T>) = cardList.addAll(deck.deckOfCards)

        @CardMarker
        fun cards(cards: Iterable<T>) = cardList.addAll(cards)

        fun shuffle() = cardList.shuffle()

        private fun build() = Deck<T>().apply {
            addCards(cardList)
            addDeckListener(deckListener.build())
        }

        companion object {
            @DeckMarker
            operator fun <T> invoke(block: DeckBuilder<T>.() -> Unit) = buildDeck(block)

            @DeckMarker
            fun <T> buildDeck(block: DeckBuilder<T>.() -> Unit) = DeckBuilder<T>().apply(block).build()
        }
    }
}
