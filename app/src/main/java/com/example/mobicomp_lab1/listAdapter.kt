package com.example.mobicomp_lab1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

//class listAdapter(private val context: Context, private val dataSource: ArrayList<Recipe>) : BaseAdapter() {
//
//    private val inflater: LayoutInflater =
//        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//
//    override fun getCount(): Int {
//        return dataSource.size
//    }
//
//    override fun getItem(position: Int): Any {
//        return dataSource[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        // Get view for row item
//        val rowView = inflater.inflate(R.layout.list_item, parent, false)
//
//        return rowView
//    }
//}