package com.spisoft.quicknote.editor;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by alexandre on 12/02/16.
 */
public class BubbleManager {

    private final WindowManager mWindowManager;
    private final Context mContext;
    private final WindowManager.LayoutParams paramsF;
    private final BubbleLayout mBubble;

    public void remove() {
        mWindowManager.removeViewImmediate(mBubble);
    }


    private class AnimationHandler extends Handler {
        public int mTime;
        float animationTime = 0;
        private  int mEndX;
        private  int mStartX;
        private  int mDeltaX;
        private float coeff;
        private boolean mIsCanceled;

        public void cancel(){
            removeMessages(0);
            mIsCanceled = true;
        }
        public void handleMessage(Message msg) {
            paramsF.x = (int) (mStartX + (float)mDeltaX * ((float)msg.arg1/(float)1000));
            mWindowManager.updateViewLayout(mBubble, paramsF);
            if(!mIsCanceled&&((float)msg.arg1/(float)1000)+coeff<=1)
                sendMessageDelayed(obtainMessage(0, (int) (msg.arg1+coeff*1000),0), 20);
        }
        public void


        goToX(int endX, int time){
            mIsCanceled =false;
            mEndX  = endX;
            mStartX = paramsF.x;
            mDeltaX = mEndX -mStartX;
            coeff = (float)1/((float)time/(float)20);

            mTime=100/time;
            sendMessageDelayed(obtainMessage(0, 0,0),20);
        }
    }
    AnimationHandler animationHandler = new AnimationHandler();

    public BubbleManager(WindowManager windowManager, Context context, BubbleLayout bubble){
        mWindowManager = windowManager;
        mContext = context;
        mBubble = bubble;
        paramsF = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT);

        paramsF.gravity = Gravity.TOP | Gravity.LEFT;
        paramsF.x = 0;
        paramsF.y = 0;
        Display display = mWindowManager.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        mWindowManager.addView(mBubble, paramsF);
        mBubble.measure(0,0);
          mBubble.setOnTouchListener(new View.OnTouchListener() {
              public boolean mHasMoved;
              private int initialX;
              private int initialY;
              private float initialTouchX;
              private float initialTouchY;

              @Override
              public boolean onTouch(View v, MotionEvent event) {
                  switch (event.getAction()) {
                      case MotionEvent.ACTION_DOWN:
                          mBubble.getMainView().setAlpha(1);
                          mHasMoved = false;
                          initialX = paramsF.x;
                          initialY = paramsF.y;
                          initialTouchX = event.getRawX();
                          initialTouchY = event.getRawY();
                          break;
                      case MotionEvent.ACTION_UP:
                          if (!mHasMoved) {
                              mBubble.onMainClick();
                          } else {
                              putNearestBoarder();
                          }
                          mBubble.getMainView().setAlpha((float) 0.5);
                          break;
                      case MotionEvent.ACTION_MOVE:
                          if (Math.abs(initialX - (initialX + (int) (event.getRawX() - initialTouchX))) < 50)
                              break;
                          mHasMoved = true;
                          paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);

                          int newY =initialY + (int) (event.getRawY() - initialTouchY);

                          if(newY<0)
                              paramsF.y=0;
                          else if(newY+mBubble.getMeasuredHeight()>size.y)
                              paramsF.y=size.y-mBubble.getMeasuredHeight();
                          else
                            paramsF.y = newY;


                          mWindowManager.updateViewLayout(mBubble, paramsF);
                          break;
                  }
                  return false;
              }
          });

        putNearestBoarder();
    }

    public void putNearestBoarder() {
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int newX;
        if(paramsF.x>size.x/2){
            newX = size.x-mBubble.getMainView().getMeasuredWidth()/2;;
        }
        else{
            newX = 0-mBubble.getMainView().getMeasuredWidth()/2;
        }
        if(paramsF.y==0)
            paramsF.y = 0;
        else if(paramsF.y+mBubble.getMainView().getMeasuredHeight()>=size.y)
            paramsF.y = size.y-mBubble.getMeasuredHeight();
        final int finalnewX = newX;
        animationHandler.goToX(newX, 200);



    }



    public void hide(){
        mBubble.setVisibility(View.GONE);
    }

    public void show() {

        mBubble.setVisibility(View.VISIBLE);

    }
}
