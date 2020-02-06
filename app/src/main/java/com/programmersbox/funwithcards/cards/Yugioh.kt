@file:Suppress("unused", "SpellCheckingInspection", "EnumEntryName", "DefaultLocale")

package com.programmersbox.funwithcards.cards

data class YugiohCard(
    val id: Long,
    val name: String,
    val type: TypeType,
    val atk: Int?,
    val def: Int?,
    val level: Int?,
    val attribute: AttributeType?,
    val linkval: Int?,
    val scale: Int?,
    val race: RaceType,
    val archetype: ArcheType?,
    val desc: String,
    val linkmarkers: List<LinkDirections>?,
    val card_sets: List<CardSet>?,
    val card_images: List<CardImages> = emptyList(),
    val card_prices: List<CardPrices> = emptyList()
) {
    override fun equals(other: Any?): Boolean = if (other is YugiohCard) other.id == id else super.equals(other)
    override fun hashCode(): Int = id.hashCode()
    fun highestPrice() = card_prices.sumByDouble(CardPrices::highestPrices)
    fun findSameArchetype(cardList: Iterable<YugiohCard>) = cardList.filter { it.archetype == archetype }
    fun findSameType(cardList: Iterable<YugiohCard>) = cardList.filter { it.type == type }
    fun findSameRace(cardList: Iterable<YugiohCard>) = cardList.filter { it.race == race }
    fun findSameAttribute(cardList: Iterable<YugiohCard>) = cardList.filter { it.attribute == attribute }
    fun findRelatedCards(cardList: Iterable<YugiohCard>) = "\"(.*?)\"".toRegex().findAll(this.desc).let { otherCardRegex ->
        cardList.filter {
            it.name.contains(this.name, true) || it.desc.contains(this.name, true) ||
                    (if (it.archetype == null || this.archetype == null) false else it.archetype == this.archetype) ||
                    otherCardRegex.any { regex -> it.name == regex.value.removeSurrounding("\"") }
        }.sortedWith(compareBy<YugiohCard> { it.archetype }.thenBy { it.name })
    }
}

enum class LinkDirections { Bottom, Top, Right, Left, `Bottom-Right`, `Bottom-Left`, `Top-Right`, `Top-Left` }

enum class ArcheType {
    Alien, Melodious, Archfiend, Elemental_HERO, Umi, ABC, Ignister, Unchained, Gem_, Rokket,
    Abyss_Actor, Mermail, Photon, Hole, Adamatia, Crystal_Beast, Heraldic, Mecha_Phantom_Beast, Aether,
    Blackwing, Ninja, Parshath, Invoked, Burning_Abyss, Alligator, Allure_Queen, Harpie, Ally_of_Justice, Magnet_Warrior, Sylvan, Altergeist,
    Nordic, Magician, Amazoness, Roid, Venom, Prophecy, Dracoverlord, Amorphage, Dark_Magician, Flamvell,
    Ancient_Gear, Ancient_Warriors, Sphinx, Angel, Monarch, Kuriboh, Anti, Apoqliphort, Vision_HERO, Valkyrie,
    Aquaactress, Gishki, Arcana_Force, Assault_Mode, Nemesis, Nekroz, Black_Luster_Soldier, Armed_Dragon, Chaos_Phantom, Inzektor,
    Ninjitsu_Art, Cyber_Dragon, Empowered_Warrior, Aroma, Guardian, Artifact, Noble_Knight, Six_Samurai, Darklord, Assault_Blackwing,
    Metaphys, X_Saber, Atlantean, Scrap, World_Chalice, Lightsworn, Charmer, Koa_ki_Meiru, Greed, Vendread, World_Legacy, Nephthys, Worm,
    B_E_S_, Naturia, Resonator, Evil_Eye, Batteryman, Battleguard, Battlewasp, Battlin__Boxer, Yang_Zing, Blue_Eyes,
    Barbaros, Pendulum_Dragon, Fur_Hire, Frog, Dark_World, Fortune_Lady, Tenyi, Koala, Rose_Dragon, Prediction_Princess,
    Red_Eyes, Eyes_Restrict, Plunder_Patroll, Bounzer, Cubic, Blaze_Accelerator, Ice_Barrier, Duston, Toon, Penguin, Bonding, Meklord, Boot_Up,
    Gadget, Borrel, Demise, Bamboo_Sword, Fire_Fist, Performage, Bujin, Laval, Salamangreat, Chemicritter, Destruction_Sword, Butterfly, of_Gusto,
    Earthbound, Cataclysmic, Junk, Celtic_Guard, Dragonmaid, Slime, Chaos, Gravekeeper_s, SPYRAL, Zefra, Chronomaly, Chrysalis, Cipher, Fire_King,
    Clear_Wing, Destiny_HERO, Cloudian, Codebreaker, Gladiator_Beast, Performapal, Machina, Neo_Spacian, Gimmick_Puppet, Constellar, Dark_Contract,
    Exodia, Shark, Umbral_Horror, Impcantation, Vampire, Endymion, Crusadia, Krawler, Crystron, Mask, Shaddoll, CXyz, Cyber_Angel, Cyberdark, D_D_,
    D_SLASH_D, Mayakashi, Danger_, Frightfur, Spirit_Message, Dark_Scorpion, Simorgh, Tellarknight, Deep_Sea, Edge_Imp, Deskbot, Dice, Galaxy_Eyes,
    Orcust, Dinomist, True_Draco, Dracoslayer, Dinowrestler, Aesir, Mist_Valley, Herald, Yosenju, of_Rituals, Dododo, Kozmo, Kaiju, Power_Tool,
    Generaider, Dragunity, Dream_Mirror, Parasite, Elementsaber, Maju, Timelord, Tindangle, Evil_HERO, Steelswarm, Infestation, Evoltile, Evolsaur,
    Evolzar, Infernoid, F_A_, Fabled, Morphtronic, Shiranui, Fire_Formation, Fishborg, Skull_Servant, Flower_Cardian, Fluffal, Masked_HERO,
    Fortune_Fairy, Metalfoes, Utopia, Gagaga, Gandora, Geargia, Genex, Mathmech, Ghostrick, Gizmek, Gogogo, Karakuri, Super_Defense_Robot, Gorgonic,
    Gouki, Elemental_Lord, Graydle, Predaplant, Guardragon, Hazy, Infinitrack, Heraldry, Heroic, Hi_Speedroid, Hieratic, Priestess,
    Horus_the_Black_Flame_Dragon, Igknight, Infernity, Iron_Chain, Jester, Jinzo, Jurrac, Knightmare, Qli, HERO, Legendary_Knight,
    Wind_Up, Lunalight, Lyrilusc, Madolche, Magical_Musket, Majespecter, Majestic, Malefic, Marincess, The_Agent, Megalith, Mekk_Knight, Super_Quant,
    Nimble, Odd_Eyes, Star_Seraph, Ojama, Onomato, Phantasm_Spiral, Paleozoic, Phantom_Knights, Prank_Kids, PSY_Frame, Raidraptor, Reptilianne,
    Ritual_Beast, Stellarknight, Secret_Six_Samurai, Shinobird, T_G_, Silent_Magician, Silent_Swordsman, Sky_Striker, Solemn, Speedroid, Starliege,
    Subterror, Superheavy, Supreme_King, Symphonic_Warrior, The_Weather, Thunder_Dragon, Time_Thief, Traptrix, Triamid, Trickstar, U_A_,
    Void, Volcanic, Vylon, Watt, Windwitch, Witchcrafter, Yubel, Zoodiac;

    fun findAllCardsThisType(cardList: Iterable<YugiohCard>) = cardList.filter { it.archetype == this }

    companion object {
        operator fun invoke(s1: String?): ArcheType = valueOf(
            if (s1 == null) "$s1"
            else {
                var s = s1
                if (s.contains(" ")) s = s.replace(" ", "_")
                if (s.contains("@")) s = s.replace("@", "")
                if (s.contains(".")) s = s.replace(".", "_")
                if (s.contains("-")) s = s.replace("-", "_")
                if (s.contains("'")) s = s.replace("'", "_")
                if (s.contains("/")) s = s.replace("/", "_SLASH_")
                if (s.contains("!")) s = s.replace("!", "_")
                s
            }
        )
    }
}

enum class AttributeType {
    EARTH, WATER, WIND, DARK, LIGHT, FIRE, DIVINE;

    fun findAllCardsThisType(cardList: Iterable<YugiohCard>) = cardList.filter { it.attribute == this }
}

enum class RaceType {
    Aqua, Beast, `Beast-Warrior`, `Creator-God`, Cyberse, Dinosaur, `Divine-Beast`, Dragon, Fairy, Fiend, Fish, Insect, Machine, Plant, Psychic, Pyro,
    Reptile, Rock, `Sea-Serpent`, Spellcaster, Thunder, Warrior, `Winged-Beast`, Wyrm, Zombie,
    Andrew, Bonz, Christine, David, Emma, Ishizu, Joey, Kaiba, Keith, Mai, Mako, Pegasus, Rex, Weevil, Yugi, //People
    Normal, Field, Equip, Continuous, `Quick-Play`, Ritual, Counter;

    fun findAllCardsThisType(cardList: Iterable<YugiohCard>) = cardList.filter { it.race == this }

    companion object {
        operator fun invoke(s: String) = when (s) {
            "Sea Serpent" -> `Sea-Serpent`
            "Winged Beast" -> `Winged-Beast`
            else -> valueOf(s)
        }
    }
}

enum class TypeType {
    TOKEN, //TOKEN

    EFFECT_MONSTER, FLIP_EFFECT_MONSTER, FLIP_TUNER_MONSTER, GEMINI_MONSTER, NORMAL_MONSTER, NORMAL_TUNER_MONSTER, PENDULUM_EFFECT_MONSTER,
    PENDULUM_FLIP_EFFECT_MONSTER, PENDULUM_NORMAL_MONSTER, PENDULUM_TUNER_EFFECT_MONSTER, RITUAL_EFFECT_MONSTER, RITUAL_MONSTER,
    SKILL_CARD, SPIRIT_MONSTER, TOON_MONSTER, TUNER_MONSTER, UNION_EFFECT_MONSTER, UNION_TUNER_EFFECT_MONSTER, //Normal Deck Monsters

    TRAP_CARD, SPELL_CARD, //Trap/Spell

    FUSION_MONSTER, LINK_MONSTER, PENDULUM_EFFECT_FUSION_MONSTER, SYNCHRO_MONSTER, SYNCHRO_PENDULUM_EFFECT_MONSTER, SYNCHRO_TUNER_MONSTER, XYZ_MONSTER,
    XYZ_PENDULUM_EFFECT_MONSTER; //Extra Deck Monsters

    companion object {
        operator fun invoke(s: String) = valueOf(s.replace(" ", "_").toUpperCase())
        val extraDeckTypes = arrayOf(
            FUSION_MONSTER, LINK_MONSTER, PENDULUM_EFFECT_FUSION_MONSTER, SYNCHRO_MONSTER, SYNCHRO_PENDULUM_EFFECT_MONSTER, SYNCHRO_TUNER_MONSTER,
            XYZ_MONSTER, XYZ_PENDULUM_EFFECT_MONSTER
        )

        val deckTypes = arrayOf(
            EFFECT_MONSTER, FLIP_EFFECT_MONSTER, FLIP_TUNER_MONSTER, GEMINI_MONSTER, NORMAL_MONSTER, NORMAL_TUNER_MONSTER,
            PENDULUM_EFFECT_MONSTER, PENDULUM_FLIP_EFFECT_MONSTER, PENDULUM_NORMAL_MONSTER, PENDULUM_TUNER_EFFECT_MONSTER,
            RITUAL_EFFECT_MONSTER, RITUAL_MONSTER, SKILL_CARD, SPELL_CARD, SPIRIT_MONSTER, TOON_MONSTER, TRAP_CARD, TUNER_MONSTER,
            UNION_EFFECT_MONSTER, UNION_TUNER_EFFECT_MONSTER
        )
    }

    fun findAllCardsThisType(cardList: Iterable<YugiohCard>) = cardList.filter { it.type == this }
}

data class CardSet(
    val set_name: String,
    val set_code: String,
    val set_rarity: String,
    val set_price: String
) {
    fun findAllCardsInSet(cardList: Iterable<YugiohCard>) =
        this to cardList.filter { it.card_sets?.map { s -> s.set_name }?.contains(set_name) == true }
}

data class CardPrices(
    val cardmarket_price: String,
    val tcgplayer_price: String,
    val ebay_price: String,
    val amazon_price: String
) {
    fun highestPrices() =
        listOfNotNull(cardmarket_price, tcgplayer_price, ebay_price, amazon_price).maxBy { it.toDoubleOrNull() ?: 0.0 }?.toDoubleOrNull() ?: 0.0
}

data class CardImages(val id: Number, val image_url: String, val image_url_small: String)

//----------------------------------------------------------------------------------------------------------------------------------------------------

enum class SortItems(val sort: Comparator<YugiohCard>) {
    NAME(compareBy { it.name }),
    ID(compareBy { it.id }),
    FRAME(compareBy<YugiohCard> { it.type }.thenBy { it.name }),
    ATK(compareByDescending<YugiohCard> { it.atk }.thenBy { it.name }),
    DEF(compareByDescending<YugiohCard> { it.def ?: it.linkval }.thenBy { it.name }),
    LEVEL(compareByDescending<YugiohCard> { it.level }.thenBy { it.name }),
    COST(compareByDescending<YugiohCard> { it.highestPrice() }.thenBy { it.name }),
    ATTRIBUTE(compareBy<YugiohCard> { it.attribute }.thenBy { it.name }),
    RACE(compareBy<YugiohCard> { it.race }.thenBy { it.name }),
    ARCHETYPE(compareBy<YugiohCard> { it.archetype }.thenBy { it.name }),
    IMAGE_COUNT(compareByDescending<YugiohCard> { it.card_images.size }.thenBy { it.name }),
    RANDOM(compareBy { it.name });

    operator fun invoke() = sort
    fun sortWith(list: Iterable<YugiohCard>) = if(this==RANDOM) list.shuffled() else list.sortedWith(sort)

    companion object {
        operator fun invoke(item: SortItems, list: Iterable<YugiohCard>) = item.sortWith(list)
    }

}

fun Iterable<YugiohCard>.toCardSets(): Map<String, List<YugiohCard>> {
    val list = mutableMapOf<String, MutableList<YugiohCard>>().withDefault { mutableListOf() }
    forEach {
        it.card_sets?.forEach { set ->
            list[set.set_name] = list.getOrDefault(set.set_name, mutableListOf())
                .apply { addAll(this@toCardSets.filter { card -> card.card_sets?.contains(set) == true }) }
        }
    }
    return list
}
