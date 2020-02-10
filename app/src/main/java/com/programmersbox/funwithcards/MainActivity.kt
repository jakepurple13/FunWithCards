package com.programmersbox.funwithcards

import android.Manifest
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
import android.widget.SectionIndexer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmersbox.funwithcards.cards.DeckType
import com.programmersbox.funwithcards.cards.SortItems
import com.programmersbox.funwithcards.cards.YugiohCard
import com.programmersbox.funwithcards.cards.YugiohDeckException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_view.view.*

object FileSaver {
    var canUseFile: Boolean = false
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Loged.FILTER_BY_CLASS_NAME = "programmersbox"
        Loged.TAG = "Cards"
        Loged.OTHER_CLASS_FILTER = { !it.contains("Framing") }

        printRuntimeInfo()
        permissionCheck()

        @Suppress("IMPLICIT_CAST_TO_ANY")
        fun action(index: Int): YugiohCard.() -> String = {
            when (SortItems.values()[index]) {
                SortItems.NAME -> name
                SortItems.ID -> id
                SortItems.FRAME -> type
                SortItems.ATK -> atk
                SortItems.DEF -> def
                SortItems.LEVEL -> level
                SortItems.COST -> highestPrice()
                SortItems.ATTRIBUTE -> attribute
                SortItems.RACE -> race
                SortItems.ARCHETYPE -> archetype
                SortItems.IMAGE_COUNT -> card_images.size
                SortItems.RANDOM -> ""
            }.toString()
        }

        val cards = getCards()
        cardView.adapter = CardAdapter(this, cards.toMutableList(), action(0))

        var comparator = compareBy<YugiohCard> { it.name }
            .thenBy { it.desc }
            .thenBy { it.race }
            .thenBy { it.archetype }
            .thenBy { it.type }

        val filter: (YugiohCard) -> Boolean = {
            val searchText = search_info.text.toString()
            it.name.contains(searchText, true) || it.desc.contains(searchText, true)
        }

        var sortCheck = 0

        search_info.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                cardView.adapter = (cardView.adapter as? CardAdapter)?.let {
                    CardAdapter(this@MainActivity, cards.filter(filter).sortedWith(comparator).toMutableList(), action(sortCheck))
                }
            }
        })

        sort_button.setOnClickListener {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setSingleChoiceItems(SortItems.values().map(SortItems::name).toTypedArray(), sortCheck) { dialog: DialogInterface, index: Int ->
                    sortCheck = index
                    comparator = SortItems.values()[index].sort
                    cardView.adapter = (cardView.adapter as? CardAdapter)?.let {
                        CardAdapter(this@MainActivity, SortItems.values()[index].sortWith(it.list).toMutableList(), action(index))
                    }
                    dialog.dismiss()
                }
                .setTitle("Sort by")
                .show()
        }

        myDecks.setOnClickListener { startActivity(Intent(this@MainActivity, DeckChooseActivity::class.java)) }
    }

}

fun Context.permissionCheck(granted: () -> Unit = {}) {

    TedPermission.with(this)
        .setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(this@permissionCheck, "Permission Granted", Toast.LENGTH_SHORT).show()
                granted()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(this@permissionCheck, "Permission Denied\n$deniedPermissions", Toast.LENGTH_SHORT).show()
            }
        })
        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .check()

    /*Permissions.check(this,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
        "Storage permissions are required because so we can save Decks",
        Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
        object : PermissionHandler() {
            override fun onGranted() {
                //do your task
                FileSaver.canUseFile = true
                granted()
            }

            override fun onDenied(context: Context?, deniedPermissions: java.util.ArrayList<String>?) {
                super.onDenied(context, deniedPermissions)
                if (!FileSaver.canUseFile) Toast.makeText(this@permissionCheck, "Can't use all functions", Toast.LENGTH_SHORT).show()
                Permissions.check(
                    this@permissionCheck, deniedPermissions!!.toTypedArray(),
                    "Storage permissions are required because so we can save Decks",
                    Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                    this
                )
            }
        })*/
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

class CardAdapter(private val context: Context, list: MutableList<YugiohCard>, private var action: YugiohCard.() -> String = { name }) :
    DragSwipeAdapterTwo<YugiohCard, CardAdapter.ViewHolder>(list), SectionIndexer {

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

    private var mSectionPositions: MutableList<Int> = mutableListOf()
    override fun getSectionForPosition(position: Int): Int = 0
    override fun getSections(): Array<String> {
        val sections = ArrayList<String>(26)
        mSectionPositions = ArrayList(26)
        var i = 0
        val size = list.size
        while (i < size) {
            val section = list[i].action()[0].toUpperCase().toString()
            if (!sections.contains(section)) {
                sections.add(section)
                mSectionPositions.add(i)
            }
            i++
        }
        return sections.toTypedArray()
    }

    override fun getPositionForSection(sectionIndex: Int): Int = mSectionPositions[sectionIndex]

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.yugiohCard
        val title: TextView = itemView.cardTitle
    }
}
