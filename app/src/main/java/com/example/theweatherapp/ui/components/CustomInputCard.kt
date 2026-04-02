package com.example.theweatherapp.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.ViewCustomInputCardBinding

class CustomInputCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCustomInputCardBinding

    init {
        binding = ViewCustomInputCardBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attrs, R.styleable.CustomInputCard, defStyleAttr, 0).apply {
            try {
                val hint = getString(R.styleable.CustomInputCard_hint)
                val text = getString(R.styleable.CustomInputCard_text)
                val inputType = getInt(R.styleable.CustomInputCard_android_inputType, -1)

                setHint(hint ?: "")
                setText(text ?: "")
                if (inputType != -1) {
                    binding.editText.inputType = inputType
                }
            } finally {
                recycle()
            }
        }
    }

    fun setText(text: String) {
        binding.editText.setText(text)
    }

    fun getText(): String {
        return binding.editText.text.toString()
    }

    fun setHint(hint: String) {
        binding.textInputLayout.hint = hint
    }
}
