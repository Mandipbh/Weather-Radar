package com.example.theweatherapp.ui.dashboard.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatherapp.R
import com.example.theweatherapp.ui.dashboard.language.model.LanguageModel

class LanguageAdapter(
    private var languages: List<LanguageModel>,
    private val onLanguageSelected: (LanguageModel) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    inner class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLanguageName: TextView = view.findViewById(R.id.tvLanguageName)
        val ivCheckIcon: ImageView = view.findViewById(R.id.ivCheck)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    val previousIndex = languages.indexOfFirst { it.isSelected }

                    languages.forEach { it.isSelected = false }
                    languages[position].isSelected = true

                    if (previousIndex != -1) notifyItemChanged(previousIndex)
                    notifyItemChanged(position)

                    onLanguageSelected(languages[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LanguageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_language, parent, false)
        )

    override fun onBindViewHolder(h: LanguageViewHolder, position: Int) {
        val data = languages[position]

        h.tvLanguageName.text = data.name
        
        // Show check icon and update visual state
        if (data.isSelected) {
            h.ivCheckIcon.visibility = View.VISIBLE
            h.itemView.alpha = 1.0f
        } else {
            h.ivCheckIcon.visibility = View.INVISIBLE
            h.itemView.alpha = 0.7f
        }
    }

    override fun getItemCount() = languages.size
}