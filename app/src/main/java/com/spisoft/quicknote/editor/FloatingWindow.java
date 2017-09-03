package com.spisoft.quicknote.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

/**
 * Created by alexandre on 11/02/16.
 */
public class FloatingWindow extends LinearLayout {
    public FloatingWindow(Context context) {
        super(context);
    }

    FloatingWindowListener mFloatingWindowListener;

    public FloatingWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(FloatingWindowListener floatingWindowListener){
        mFloatingWindowListener = floatingWindowListener;

    }
    public interface FloatingWindowListener{
        void onBackPressed();

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode()==KeyEvent.KEYCODE_BACK||event.getKeyCode()==KeyEvent.KEYCODE_HOME)&&event.getAction()==KeyEvent.ACTION_DOWN&&mFloatingWindowListener!=null) {
            mFloatingWindowListener.onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
