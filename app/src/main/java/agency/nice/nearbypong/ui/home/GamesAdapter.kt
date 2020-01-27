package agency.nice.nearbypong.ui.home

import agency.nice.nearbypong.R
import agency.nice.nearbypong.model.Game
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_game.view.*

/**
 * Created by ferranribell on 17/10/2017.
 */
class GamesAdapter(var context: Context, var list: List<Game>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_ITEM = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bindItems(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_ITEM
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(game: Game) {
            itemView.apply {
                score_one.text = game.scoreOne.toString()
                score_two.text = game.scoreTwo.toString()
            }
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}