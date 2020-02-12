package com.programmersbox.funwithcards

import com.programmersbox.funwithcards.cards.*
import io.pokemontcg.Pokemon
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Before
    fun setup() {
        Loged.UNIT_TESTING = true
        Loged.FILTER_BY_CLASS_NAME = "programmersbox"
        Loged.OTHER_CLASS_FILTER = { !it.contains("Framing") }
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun pokemonCards() {
        /*val p = File("/Users/jrein/Documents/FunWithCards/app/src/main/res/raw/pokemon_cards_two.json")
        getApi("https://pokeapi.co/api/v2/")?.let {
            p.createNewFile()
            p.writeText(it)
        }*/
        val cards = Pokemon().card().all()
        val p = File("/Users/jrein/Documents/FunWithCards/app/src/main/res/raw/pokemon_cards.json")
        p.writeText(cards.toJson())
        //val cards = p.readText().fromJson<List<Card>>()!!
        //println(cards.size)
        //println(cards.joinToString("\n"))
    }

    open class Rectangle {
        open fun draw() = println("Rectangle's drawing")
    }

    interface Polygon {
        fun draw() = println("Polygon's drawing") // interface members are 'open' by default
    }

    class Square : Rectangle(), Polygon {
        // The compiler requires draw() to be overridden:
        override fun draw() {
            super<Rectangle>.draw() // call to Rectangle.draw()
            super<Polygon>.draw() // call to Polygon.draw()
        }
    }

    @Test
    fun shapeTest() {
        Square().draw()

        val f = object {
            val s = 0
        }
        println(f::class.java.simpleName)

        fun makeObject() = object {
            var g = 0
            var s = "Hello"
        }

        println(makeObject())
        println(makeObject().s)

        /*fun asdf(v: ) {

        }

        asdf(f)*/

    }

    private fun getCards() =
        File("/Users/jrein/Documents/FunWithCards/app/src/main/res/raw/cards.json").readText().fromJson<List<YugiohCard>>()!!

    private fun newLine(type: String = "") = println(type + "-----".repeat(10))
    private fun <T> Collection<T>.takeRandom(n: Int): List<T> = mutableListOf<T>().apply { repeat(n) { this += this@takeRandom.random() } }

    private fun <T> Iterable<T>.takeRandom(n: Int, predicate: (T) -> Boolean): List<T> =
        filter(predicate).let { filtered -> mutableListOf<T>().apply { repeat(n) { this += filtered.random() } } }

    @Test
    fun other8() {
        val cards = getCards()
        val deck = YugiohDeck()
        deck.addToDeck(*cards.takeRandom(5) { it.type in TypeType.deckTypes && !it.desc.contains("\n") }.toTypedArray(), deckType = DeckType.MAIN)
        println(deck[DeckType.MAIN])
        val json = deck[DeckType.MAIN].toDeckJson()
        newLine()
        println(json)
        newLine()
        println(json.toDeck<YugiohCard>())

        deck[DeckType.MAIN].addDeckListener(object : Deck.DeckListener<YugiohCard> {
            override fun onAdd(vararg cards: YugiohCard) = Unit
            override fun onShuffle() = Unit
            override fun onDraw(card: YugiohCard, size: Int) = Unit
        })
    }

    @Test
    fun other7() {
        fun Deck<Card>.printDeck() = println(deck.joinToString { "${it.symbol}${it.suit.unicodeSymbol}" })
        val deck = Deck.defaultDeck()
        deck {
            onDraw { it, _ -> println("${it.symbol}${it.suit.unicodeSymbol}") }
        }
        deck.printDeck()
        deck.cut()
        deck.printDeck()
        newLine()
        deck.cutShuffle(2)
        deck.printDeck()
        deck.draw(5)
        (4..9).toDeck()
        arrayOf(1, 2, 3).toDeck() addCards listOf(1, 3, 4)
        listOf(1, 2, 3).toDeck()
        arrayOf(1, 2, 3).toDeck() addCards arrayOf(1, 2, 3)
        listOf(1, 2, 3).toDeck { onAdd { } }

        val f = deck.toDeckJson()
        println(f)
        val f1 = f.toDeck<Card>()
        f1?.printDeck()

        println(deck.random())
        println(deck.random { it.suit == Suit.SPADES })
        val c = deck.randomDraw { it.suit == Suit.SPADES }
        println(c)
        println(deck.findCards { it == c })
        //val c1 = deck.randomDraw { it.value == 14 }
        //println(c1)

        //val c2 = deck[85]
        val c3 = deck findCardLocation Card.RandomCard
        deck.sortDeck(compareBy { it.value })
        deck.printDeck()
        deck.sortDeckBy { it.suit }
        deck.printDeck()
        deck.trueRandomShuffle(1)
        deck.printDeck()
        deck.sortDeck(compareBy<Card> { it.suit }.thenBy { it.value })
        deck.printDeck()
    }

    @Test
    fun other6() {
        val cards = getCards()
        val deck = YugiohDeck()
        deck.addToDeck(*cards.takeRandom(50) { it.type in TypeType.deckTypes }.toTypedArray(), deckType = DeckType.MAIN)
        deck.addToDeck(*cards.takeRandom(7).toTypedArray(), deckType = DeckType.SIDE)
        deck.addToDeck(*cards.takeRandom(7) { it.type in TypeType.extraDeckTypes }.toTypedArray(), deckType = DeckType.EXTRA)

        deck[DeckType.MAIN].addDeckListener {
            onAdd { println("Adding ${it.map { it.name }} to Main") }
            onDraw { it, _ -> println("I drew ${it.name}") }
            onShuffle { Loged.f("Shuffling...") }
        }

        deck.addDeckListener(DeckType.SIDE) {
            onAdd { println("Adding ${it.map { it.name }} to Side") }
        }

        deck[DeckType.EXTRA] {
            onAdd { println("Adding ${it.map { it.name }} to Extra") }
        }

        deck.addToDeck(
            cards.random() to DeckType.SIDE,
            cards.random() to DeckType.MAIN,
            cards.random { it.type in TypeType.extraDeckTypes } to DeckType.EXTRA
        )

        deck[DeckType.MAIN] add cards.random()

        cards.random() into DeckType.MAIN s deck
        cards.random() into deck s DeckType.MAIN

        deck.shuffle()

        newLine(deck[DeckType.MAIN].draw().name)

        println(deck[DeckType.MAIN].deck.joinToString { it.name })
        println(deck[DeckType.EXTRA].deck.joinToString { it.name })
        println(deck[DeckType.SIDE].deck.joinToString { it.name })
        newLine()
        println(deck[DeckType.MAIN].drawCards { it.type == TypeType.SPELL_CARD }.joinToString { it.name })
        println(deck[DeckType.MAIN].deck.joinToString { it.name })

        for (i in deck[DeckType.MAIN]) {
            if (i !in deck[DeckType.EXTRA]) println(i.name)
        }
    }

    private infix fun YugiohCard.into(deck: DeckType) = this to deck
    private infix fun YugiohCard.into(deck: YugiohDeck) = this to deck

    private infix fun Pair<YugiohCard, DeckType>.s(deck: YugiohDeck) = deck.addToDeck(first, second)
    private infix fun Pair<YugiohCard, YugiohDeck>.s(type: DeckType) = second.addToDeck(first, type)

    @Test
    fun other5() {
        val cards = getCards()
        val archeType = cards.groupBy { it.archetype }
        println(archeType.entries.sortedByDescending { it.value.size }.joinToString("\n") { "${it.key} - ${it.value.size}" })
        newLine()

        //val type = cards.filter { it.archetype == ArcheType.Greed }
        //println(type.joinToString("\n${"-----".repeat(10)}\n"))
    }

    @Test
    fun other4() {
        val cards = getCards()
        val images = cards.sortedWith(SortItems.IMAGE_COUNT.sort)
        println(images.groupBy { it.card_images.size }.entries.joinToString("\n") { "${it.key} -> ${it.value.size}" })
        newLine()
        println(images.groupBy { it.card_images.size }.entries.joinToString("\n") { "${it.key} -> ${it.value.joinToString { it.name }}" })
    }

    @Test
    fun other3() {

        val d = Deck.defaultDeck().apply {
            isEmpty
            isNotEmpty
            findCards { true }
            findCards { true }
            size
            remove(Card.RandomCard, Card.RandomCard)
        }
        d.randomCard

        val d1 = 5..d
        val d2 = d[2..5]
        val d3 = -d
        val d4 = d - 5
        d += Card.RandomCard
        d[5] = Card.RandomCard
        d.addCard(Card.RandomCard, Card.RandomCard)
        d.addCard(5, Card.RandomCard)
        d.addCard(
            6 to Card.RandomCard,
            7 to Card.RandomCard
        )

        Deck.DeckBuilder<Card> {
            deckListener {
                onAdd {}
                onShuffle {}
                onDraw { _, _ -> }
            }
            card(4, Suit.SPADES)
            card {
                value = 4
                suit = Suit.SPADES
            }
            card(2 to Suit.SPADES)
        }

        Card.RandomCard.apply {
            suit.apply {
                symbol
                unicodeSymbol
                printableName
            }
            symbol
            value
            valueTen
        }

        Card.CardBuilder.cardBuilder {}

        val cards = getCards()
        val deck = YugiohDeck().apply {
            this[DeckType.MAIN].addDeckListener {
                onDraw { it, _ -> println("I draw ${it.name}") }
                onShuffle { println("Shuffling") }
            }
        }
        val card = cards.random()
        deck.addToDeck(card)
        deck.addToDeck(card)
        deck.addToDeck(card)
        try {
            deck.addToDeck(card)
            deck[DeckType.MAIN].addToDeck(card)
        } catch (e: YugiohDeckException) {
            println(e.message)
        }
        try {
            deck.addToDeck(card, DeckType.EXTRA)
            deck.addToDeck(card, DeckType.MAIN)
        } catch (e: YugiohDeckException) {
            println(e.message)
        }
        try {
            deck.addToDeck(cards.random(), DeckType.SIDE)
        } catch (e: YugiohDeckException) {
            println(e.message)
        }
        cards.takeRandom(20).forEach { deck.addToDeck(it, DeckType.values().random()) }
        deck.addToDeck(*cards.takeRandom(5).toTypedArray())
        newLine("Main")
        println(deck[DeckType.MAIN].deck.joinToString("\n") { it.name })
        newLine("Extra")
        println(deck[DeckType.EXTRA].deck.joinToString("\n") { it.name })
        newLine("Side")
        println(deck[DeckType.SIDE].deck.joinToString("\n") { it.name })
    }

    @Test
    fun other2() {
        val cards = getCards()
        val links = cards.filter { it.linkmarkers != null }.sortedByDescending { it.linkmarkers!!.size }.groupBy { it.linkmarkers!!.size }
        println(links.entries.joinToString("\n\n") { "${it.key} - ${it.value.size}" })
        println(links[6])
    }

    @Test
    fun other1() {
        val cards = getCards()
        val f = cards.find { it.name == "Metal Reflect Slime" }
        println(f)
        newLine()
        newLine()
        println(cards.filter { it.type == TypeType.TOKEN }.joinToString("\n\n"))
    }

    @Test
    fun other() {
        val cards = getCards()
        val list = cards.takeRandom(10)
        println(list.joinToString("\n\n"))
        newLine()
        newLine()
        val cardImageList = cards.filter { it.card_images.size > 1 }
        println(cardImageList.joinToString("\n\n"))
    }

    @Test
    fun related() {
        val cards = getCards()
        println(cards.size)
        val dm = cards.find { it.name == "Flame Swordsman" }!!
        val regex = "\"(.*?)\"".toRegex().findAll(dm.desc)
        val regexList = cards.filter { card -> regex.any { card.name == it.value.removeSurrounding("\"") } }
        println(regexList.joinToString("\n"))
        println(dm)
        newLine()
        val relatedCards = dm.findRelatedCards(cards)
        println(relatedCards.size)
        newLine("Start")
        println(relatedCards.joinToString("\n${"-----".repeat(10)}\n"))
        newLine("End")
        println(cards.find { it.name == "Flame Manipulator" }!!)
        println(cards.find { it.name == "Masaki the Legendary Swordsman" }!!)
        println(dm.toPrettyJson())
    }

    @Test
    fun grouping() {
        val cards = getCards()
        fun newLine(type: String = "") = println(type + "-----".repeat(10))
        newLine("Attr")
        val byAttr = cards.groupBy { it.attribute }
        println(byAttr.entries.joinToString("\n") { "${it.key} - ${it.value.size}" })
        newLine("Type")
        val byType = cards.groupBy { it.type }
        println(byType.entries.joinToString("\n") { "${it.key} - ${it.value.size}" })
        newLine("Race")
        val byRace = cards.groupBy { it.race }
        println(byRace.entries.joinToString("\n") { "${it.key} - ${it.value.size}" })
        newLine("Archetype")
        val byArchetype = cards.groupBy { it.archetype }
        println(byArchetype.entries.joinToString("\n") { "${it.key} - ${it.value.size}" })
    }

    @Test
    fun link() {
        val cards = getCards()
        val links = cards.filter { it.linkval != null }
        //println(links.joinToString("\n") { "${it.name} - ${it.atk} - ${it.def}" } )
        val f = SortItems.ATK.sortWith(links)
        println(f.joinToString("\n") { "${it.name} - ${it.atk} - ${it.def}" })
    }

    @Test
    fun atkDef() {
        val cards = getCards()
        val byAtk = cards.groupBy { it.atk }
        val byDef = cards.groupBy { it.def }
        println(byAtk.entries.sortedByDescending { it.key }.joinToString("\n") { "ATK: ${it.key} -> ${it.value.size}" })
        println()
        println(byDef.entries.sortedByDescending { it.key }.joinToString("\n") { "DEF: ${it.key} -> ${it.value.size}" })
        println(byAtk[5000].orEmpty().joinToString("\n\n") { "$it" })
    }

    @Test
    fun jsonTesting() {
        val cards = getCards()
        val cardSets = cards
            .mapNotNull { it.card_sets?.distinctBy { innerSet -> innerSet.set_name } }
            .flatten()
            .distinctBy { it.set_name }
        //println(cardSets.joinToString("\n\n"))
        val list = cards.toCardSets()
        //println(list.entries.joinToString("\n") { "${it.key} - ${it.value.size}" })
        val work = cardSets.filter { it.set_name in list.keys }
        println("${work.size} == ${list.keys.size} == ${cardSets.size}")
        newLine()
        newLine()
        //val list2 = cards.toCardSets2()
        //println("${list2.keys.size} == ${list.size} == ${cardSets.size}")
        //println("${list2.keys.size} == ${cardSets.size}")

    }

}