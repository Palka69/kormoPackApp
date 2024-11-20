package com.example.kormopack.brandRecycleClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.kormopack.R

class FeedRecycleAdapter(
    context: Context,
    list: List<FeedBrand>,
    onBrandClickListener: OnBrandClickListener
): RecyclerView.Adapter<FeedRecycleAdapter.ViewHolder>() {

    interface OnBrandClickListener {
        fun onBrandClick(text: String)
    }

    private val layoutInflater: LayoutInflater
    private var list: List<FeedBrand>
    private val myContext: Context
    private val onBrandClickListener: OnBrandClickListener

    init {
        layoutInflater = LayoutInflater.from(context)
        this.list = list
        this.myContext = context
        this.onBrandClickListener = onBrandClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = layoutInflater.inflate(R.layout.feed_recycle_adapter_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thatFeedBrandCard = list[position]

        holder.card.setCardBackgroundColor(thatFeedBrandCard.cardBackColor)
        holder.name.text = thatFeedBrandCard.name
        holder.name.setTextColor(thatFeedBrandCard.textColor)
        holder.image.setImageDrawable(thatFeedBrandCard.brandLogo)

        holder.card.setOnClickListener {
            onBrandClickListener.onBrandClick(holder.name.text as String)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card: CardView
        var name: TextView
        var image: ImageView

        init {
            card = itemView.findViewById(R.id.card_view)
            name = itemView.findViewById(R.id.text_view)
            image = itemView.findViewById(R.id.image_view)
        }
    }
}