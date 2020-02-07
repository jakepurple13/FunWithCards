package com.programmersbox.funwithcards

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.codekidlabs.storagechooser.StorageChooser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.programmersbox.funwithcards.cards.DeckType
import com.programmersbox.funwithcards.cards.YugiohCard
import com.programmersbox.funwithcards.cards.YugiohDeck
import kotlinx.android.synthetic.main.activity_deck_choose.*
import kotlinx.android.synthetic.main.card_view.view.*
import java.io.File
import java.io.FileOutputStream

fun Context.getYugiFolder(fileName: String) = File(getExternalFilesDir("YugiohDeckMaker"), fileName)

fun Context.getDecks(): MutableList<YugiohDeckState> {
    val deckFile =
        if (FileSaver.canUseFile) getYugiFolder("decks.json").let { if (it.exists()) it.readText() else "" }.fromJson<List<YugiohDeckState>>() else emptyList()
    val sharedFile = defaultPrefs.getString("decks", null).fromJson<List<YugiohDeckState>>()
    return listOfNotNull(deckFile, sharedFile).flatten().distinctBy { it.deckName }.toMutableList()
}

fun Context.saveDecks(decks: List<YugiohDeckState>) {
    if (FileSaver.canUseFile) {
        val deckFile = getYugiFolder("decks.json")
        if (!deckFile.exists()) deckFile.createNewFile()
        deckFile.writeText(decks.toJson())
    }
    defaultPrefs.edit().putString("decks", decks.toJson()).apply()
}

class DeckChooseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck_choose)

        val deckList = getDecks()
        pickADeck.adapter = ChooseCardAdapter(this, deckList)

        newDeck.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Choose an Option")
                .setItems(arrayOf("Create a New Deck", "Import a Deck", "Export a Deck")) { dialog, index ->
                    dialog.dismiss()
                    when (index) {
                        0 -> addNewDeck(deckList)
                        1 -> importDeck(deckList)
                        2 -> exportDeck(deckList)
                    }
                }
                .show()
        }
    }

    /*

            findPreference("import_favorites").setOnPreferenceClickListener {

                val st = StorageChooser.Theme(this@GeneralPreferenceFragment.context)

                st.scheme = resources.getIntArray(R.array.paranoid_theme)

                val chooser = StorageChooser.Builder()
                        .withActivity(this@GeneralPreferenceFragment.activity)
                        .withFragmentManager(fragmentManager)
                        .withMemoryBar(true)
                        .actionSave(true)
                        .allowAddFolder(true)
                        .allowCustomPath(true)
                        .disableMultiSelect()
                        .withPredefinedPath(Environment.DIRECTORY_DOWNLOADS)
                        .setType(StorageChooser.FILE_PICKER)
                        .setTheme(st)
                        .build()

                // Show dialog whenever you want by
                chooser.show()

                // get path that the user has chosen
                chooser.setOnSelectListener { path ->
                    //if (path.contains("fun.json")) {
                    /*GlobalScope.launch {
                        val show = ShowDatabase.getDatabase(this@GeneralPreferenceFragment.context).showDao()
                        val g = Gson().fromJson(mReadJsonData(path), Array<Show>::class.java)
                        for (i in g) {
                            if (show.isInDatabase(i.name) <= 0) {
                                show.insert(i)
                            }
                        }
                        File(path).delete()
                    }*/
                    this@GeneralPreferenceFragment.context.importAllShowsAndEpisodes(mReadJsonData(path)!!)
                    //}
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@GeneralPreferenceFragment.context, "Finished Importing", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
     */

    private fun exportDeck(deckList: MutableList<YugiohDeckState>) {

        data class DeckInfo(val index: Int, val deckState: YugiohDeckState, var chosen: Boolean)

        val deckChoices = deckList.mapIndexed { index, yugiohDeckState -> DeckInfo(index, yugiohDeckState, false) }

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Decks to Export")
            .setMultiChoiceItems(deckList.map { it.deckName }.toTypedArray(), BooleanArray(deckList.size)) { _, which, isChecked ->
                deckChoices[which].chosen = isChecked
            }
            .setPositiveButton("Export") { _, _ ->
                val chosen = deckChoices.filter { it.chosen }.map { it.deckState }.toJson()

                val st = StorageChooser.Theme(this)
                st.scheme = resources.getIntArray(R.array.paranoid_theme)

                val chooser = StorageChooser.Builder()
                    .withActivity(this)
                    .withFragmentManager(fragmentManager)
                    .withMemoryBar(true)
                    .actionSave(true)
                    .allowAddFolder(true)
                    .allowCustomPath(true)
                    .disableMultiSelect()
                    .withPredefinedPath(Environment.DIRECTORY_DOWNLOADS)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .setTheme(st)
                    .build()
                chooser.show()
                chooser.setOnSelectListener {
                    val file = File(it)
                    if (!file.exists()) file.createNewFile()
                    val stream = FileOutputStream(file)
                    stream.use { streams -> streams.write(chosen.toByteArray()) }
                }
            }.show()
    }

    private fun importDeck(deckList: MutableList<YugiohDeckState>) {
        val chooser = StorageChooser.Builder()
            .withActivity(this)
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .actionSave(true)
            .allowAddFolder(true)
            .allowCustomPath(true)
            .disableMultiSelect()
            .withPredefinedPath(Environment.DIRECTORY_DOWNLOADS)
            .setType(StorageChooser.FILE_PICKER)
            .build()
        chooser.show()
        chooser.setOnSelectListener {

        }
    }

    private fun addNewDeck(deckList: MutableList<YugiohDeckState>) {
        val input = EditText(this@DeckChooseActivity)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        MaterialAlertDialogBuilder(this@DeckChooseActivity)
            .setTitle("Enter a Deck Name")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                if (deckList.any { it.deckName == input.text.toString() }) {
                    Toast.makeText(this, "You already have a deck named ${input.text}", Toast.LENGTH_LONG).show()
                } else {
                    deckList.add(YugiohDeckState(deckName = input.text.toString()))
                    saveDecks(deckList)
                    pickADeck.adapter = ChooseCardAdapter(this, deckList)
                }
            }
            .setNegativeButton("Stop") { _, _ -> }
            .show()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        pickADeck.adapter?.notifyDataSetChanged()
    }
}

fun intent(context: Context, cls: Class<*>, block: Intent.() -> Unit = {}) = Intent(context, cls).apply(block)

data class YugiohDeckState(var topCard: YugiohCard? = null, val deckName: String, val deck: YugiohDeck = YugiohDeck())

fun MaterialAlertDialogBuilder.setItems(vararg items: Any, listener: (DialogInterface, Int) -> Unit) =
    setItems(items.map(Any::toString).toTypedArray(), listener)

class ChooseCardAdapter(private val context: Context, list: List<YugiohDeckState>) :
    DragSwipeAdapterTwo<YugiohDeckState, ChooseCardAdapter.ViewHolder>(list.toMutableList()) {

    private val glide = Glide.with(context)
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.deck_view, null, false))

    override fun ViewHolder.onBind(item: YugiohDeckState) {
        glide.load((item.topCard ?: item.deck[DeckType.MAIN].deck.firstOrNull())?.card_images?.random()?.image_url_small ?: R.drawable.backofcard)
            .error(R.drawable.backofcard).placeholder(R.drawable.backofcard)
            .override(Target.SIZE_ORIGINAL)
            .into(image)
        title.text = item.deckName
        itemView.setOnClickListener { context.startActivity(intent(context, DeckActivity::class.java) { putExtra("deck_info", item.deckName) }) }
        itemView.setOnLongClickListener {
            MaterialAlertDialogBuilder(context)
                .setItems("Edit Deck", "Delete Deck") { d, index ->
                    d.dismiss()
                    when (index) {
                        0 -> itemView.performClick()
                        else -> {
                            val decks = context.getDecks()
                            decks.removeIf { it.deckName == item.deckName }
                            list.removeIf { it.deckName == item.deckName }
                            context.saveDecks(decks)
                            this@ChooseCardAdapter.notifyDataSetChanged()
                        }
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