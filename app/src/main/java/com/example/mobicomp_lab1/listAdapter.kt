package com.example.mobicomp_lab1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.mobicomp_lab1.entity.Reminder

class listAdapter(private val context: Context, private val dataSource: ArrayList<Reminder>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.list_view_item, parent, false)
        val messageTextView = rowView.findViewById(R.id.messageLine) as TextView
        val timeTextView = rowView.findViewById(R.id.timeLine) as TextView
//        val detailTextView = rowView.findViewById(R.id.recipe_list_detail) as TextView
//        val thumbnailImageView = rowView.findViewById(R.id.recipe_list_thumbnail) as ImageView

        val reminder = getItem(position) as Reminder

        messageTextView.text = reminder.message
        timeTextView.text = reminder.time.toString()

        return rowView
    }
}