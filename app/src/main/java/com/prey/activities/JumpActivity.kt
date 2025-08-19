package com.prey.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Window
import com.prey.PreyLogger

/**
 * The JumpActivity is a simple Android Activity
 * that acts as a navigation hub, determining
 * the next screen to display based on the
 * presence of a NEXT_URL extra in its intent.
 */
class JumpActivity : Activity() {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState The saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    /**
     * Called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        jump()
    }

    private fun jump() {
        val extras: Bundle? = intent.extras
        var nextUrl: String = ""
        try {
            nextUrl = extras!!.getString(CheckPasswordHtmlActivity.NEXT_URL)!!
            if (nextUrl.isEmpty()) {
                intent = Intent(this, LoginActivity::class.java)
            } else {
                intent = Intent(this, CheckPasswordHtmlActivity::class.java)
                PreyLogger.d("_________________nextUrl:$nextUrl")
                intent.putExtra(CheckPasswordHtmlActivity.NEXT_URL, nextUrl)
            }
        } catch (e: Exception) {
            PreyLogger.d("not extra nextUrl")
        }
        startActivity(intent)
        finish()
    }

}