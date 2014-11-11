package com.prey.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Detects Keyboard Status changes and fires events only once for each change
 */
public class KeyboardStatusDetector {
    KeyboardVisibilityListener visibilityListener;

    boolean keyboardVisible = false;

 

    public void registerActivity(Activity a) {
        registerView(a.getWindow().getDecorView().findViewById(android.R.id.content));
    }

    public KeyboardStatusDetector registerView(final View v) {
        v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                v.getWindowVisibleDisplayFrame(r);

                int heightDiff = v.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    /** Check this variable to debounce layout events */
                    if(!keyboardVisible) {
                        keyboardVisible = true;
                        if(visibilityListener != null) visibilityListener.onVisibilityChanged(true);
                    }
                } else {
                    if(keyboardVisible) {
                        keyboardVisible = false;
                        if(visibilityListener != null) visibilityListener.onVisibilityChanged(false);
                    }
                }
            }
        });

        return this;
    }

    public KeyboardStatusDetector setVisibilityListener(KeyboardVisibilityListener listener) {
        visibilityListener = listener;
        return this;
    }

 
}