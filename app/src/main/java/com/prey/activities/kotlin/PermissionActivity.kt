/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.prey.R

class PermissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar!!.hide()
        setContentView(R.layout.activity_permission)
        val permiso_link = findViewById<View>(R.id.permiso_link) as TextView
        val arrow1 = findViewById<View>(R.id.imageView2) as ImageView
        val arrow2 = findViewById<View>(R.id.imageView3) as ImageView
        val arrow3 = findViewById<View>(R.id.imageView4) as ImageView
        permiso_link.setOnClickListener {
            val intent = Intent(
                applicationContext,
                PermissionInformationActivity::class.java
            )
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        arrow1.setOnClickListener {
            val textView2_2 = findViewById<View>(R.id.textView2_2) as TextView
            if (textView2_2.visibility == View.GONE) {
                textView2_2.visibility = View.VISIBLE
                arrow1.setImageResource(R.drawable.up)
            } else {
                textView2_2.visibility = View.GONE
                arrow1.setImageResource(R.drawable.down)
            }
        }
        arrow2.setOnClickListener {
            val textView3_2 = findViewById<View>(R.id.textView3_2) as TextView
            if (textView3_2.visibility == View.GONE) {
                textView3_2.visibility = View.VISIBLE
                arrow2.setImageResource(R.drawable.up)
            } else {
                textView3_2.visibility = View.GONE
                arrow2.setImageResource(R.drawable.down)
            }
        }
        arrow3.setOnClickListener {
            val textView4_2 = findViewById<View>(R.id.textView4_2) as TextView
            if (textView4_2.visibility == View.GONE) {
                textView4_2.visibility = View.VISIBLE
                arrow3.setImageResource(R.drawable.up)
            } else {
                textView4_2.visibility = View.GONE
                arrow3.setImageResource(R.drawable.down)
            }
        }
    }
}