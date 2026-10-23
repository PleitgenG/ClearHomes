package com.pleit.clearhome

import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val tileWidthDp: Int,
    private val onFocused: (AppItem) -> Unit,
    private val onOpen: (AppItem) -> Unit,
    private val onPin: (AppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    var items: List<AppItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun indexOf(packageName: String): Int = items.indexOfFirst { it.packageName == packageName }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val density = ctx.resources.displayMetrics.density
        val view = LayoutInflater.from(ctx).inflate(R.layout.item_app, parent, false)

        val width = (tileWidthDp * density).toInt()
        val lp = RecyclerView.LayoutParams(width, width * 9 / 16)
        val margin = (10 * density).toInt()
        lp.setMargins(margin, 0, margin, 0)
        view.layoutParams = lp

        val radius = 14 * density
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, radius)
            }
        }
        view.clipToOutline = true
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val view = holder.itemView
        view.tag = item

        if (item.isBanner) {
            holder.icon.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.icon.setPadding(0, 0, 0, 0)
        } else {
            holder.icon.scaleType = ImageView.ScaleType.FIT_CENTER
            val pad = (view.layoutParams.height * 0.2f).toInt()
            holder.icon.setPadding(pad, pad, pad, pad)
        }
        holder.icon.setImageDrawable(item.image)

        view.setOnClickListener { onOpen(item) }
        view.setOnLongClickListener {
            onPin(item)
            true
        }
        view.setOnFocusChangeListener { v, hasFocus ->
            val scale = if (hasFocus) 1.1f else 1f
            v.animate().scaleX(scale).scaleY(scale).setDuration(140).start()
            if (hasFocus) onFocused(item)
        }
    }
}
