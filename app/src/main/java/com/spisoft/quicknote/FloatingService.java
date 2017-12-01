package com.spisoft.quicknote;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.quicknote.editor.BubbleLayout;
import com.spisoft.quicknote.editor.BubbleManager;
import com.spisoft.quicknote.editor.EditorView;
import com.spisoft.quicknote.editor.FakeFragmentManager;
import com.spisoft.quicknote.editor.FloatingWindow;
import com.spisoft.quicknote.utils.PinView;

import java.io.File;
import java.util.Stack;

/**
 * Created by phoenamandre on 01/02/16.
 */
public class FloatingService extends Service implements View.OnClickListener, EditorView.HideListener, FakeFragmentManager, FloatingWindow.FloatingWindowListener, BubbleLayout.OnBubbleClickListener, PinView.PasswordListener {
    private static final int LOCK_MSG = 1;
    public static final String START_MINIMIZE = "start_minimize";
    public static FloatingService sService;
    private View image;
    public static final String NOTE = "param1";

    private WindowManager mWindowManager;
    private boolean contains;
    private BubbleLayout mBubble;
    private EditorView mEditor;
    private View mContent;
    private WindowManager.LayoutParams paramsF;
    private FrameLayout mFrameLayout;
    private View mShadowButton;
    private BroadcastReceiver mReceiver;
    private BubbleManager mBubbleManager;
    private Stack<FloatingFragment> mFragments;
    private boolean mHasPressedMinimize;
    private FrameLayout mfragmentContainer;
    private ViewGroup mOptionMenuContainer;
    private boolean isLocked;
    private View mTitleBar;
    private View mDim;
    private boolean screenshotThreadIsStarted;
    private long mLastCheck;
    private String mScreenshotPath;
    private int mLastVisibility;

    public void onCreate() {
        super.onCreate();
        mFragments = new Stack<>();
        sService = this;
        LayoutInflater li = LayoutInflater.from(this);
        image = li.inflate(R.layout.floating_note, null);
        mBubble = new BubbleLayout(this);
        mBubble.setMainBubbleView(li.inflate(R.layout.my_bubble, null));
        mBubble.setOnBubbleClickListener(this);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //requestMinimize();
                if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED))
                    mBubbleManager.putNearestBoarder();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MAIN);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mReceiver, filter);
        ((FloatingWindow) image).setListener(this);
        //image.setAlpha((float) 0.8);
        mContent = image.findViewById(R.id.content);
        mOptionMenuContainer = (ViewGroup) image.findViewById(R.id.option_menu_container);
        mfragmentContainer = (FrameLayout) image.findViewById(R.id.fragment_container);

        mEditor = (EditorView) image.findViewById(R.id.editor_view);
        mFragments.push(mEditor);
        mEditor.setHideListener(this);
        mTitleBar = image.findViewById(R.id.title_bar);
        mEditor.setOptionMenu(mOptionMenuContainer);
        mContent.findViewById(R.id.minimize).setOnClickListener(this);
        mContent.findViewById(R.id.dim_button).setOnClickListener(this);
        mShadowButton = mContent.findViewById(R.id.shadow_button);
        mShadowButton.setVisibility(View.GONE);
        mShadowButton.setOnClickListener(this);
        mDim = mContent.findViewById(R.id.dim_button);
        mDim.setOnClickListener(this);
        mContent.findViewById(R.id.close).setOnClickListener(this);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mBubbleManager = new BubbleManager(mWindowManager, this, mBubble);
        if (PreferenceHelper.shouldLockOnBubbleStart(this))
            lock();
        //startScreenshotThread();
    }

    public void addFragment(FloatingFragment fragment) {
        mfragmentContainer.removeAllViews();
        mFragments.push(fragment);
        mfragmentContainer.addView(fragment.getView());
        mOptionMenuContainer.removeAllViews();
        fragment.setOptionMenu(mOptionMenuContainer);

    }

    public void removeFragment() {
        mfragmentContainer.removeAllViews();
        mFragments.pop();
        FloatingFragment fragment = mFragments.peek();
        mfragmentContainer.addView(fragment.getView());
        mOptionMenuContainer.removeAllViews();
        fragment.setOptionMenu(mOptionMenuContainer);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int ret = super.onStartCommand(intent, flags, startID);
        mEditor.setNote((Note) intent.getSerializableExtra(NOTE));
        addFloatingView();
        if (!intent.getBooleanExtra(START_MINIMIZE, false))
            invert();
        else mBubble.makeToast(getString(R.string.toast_on_minimize));
        return ret;
    }

    public Note getNote() {
        return mEditor.getNote();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void addFloatingView() {
        if (!contains && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("", true)) {


            Intent it = new Intent(getApplicationContext(), MainActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    it,
                    0);
            Notification.Builder builder = new Notification.Builder(this).
                    setSmallIcon(R.drawable.ic_launcher).
                    setContentTitle(getString(R.string.app_name)).
                    setContentText(getString(R.string.app_name));


            startForeground(3, builder.build());
            //startForeground();
            paramsF = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);

            paramsF.gravity = Gravity.TOP | Gravity.LEFT;
            paramsF.x = 0;
            paramsF.y = 0;
            mWindowManager.addView(image, paramsF);
            image.setVisibility(View.GONE);

            contains = true;

        }


    }

    private void invert() {
        image.setVisibility(image.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);


        if (image.getVisibility() == View.VISIBLE) {
            mHasPressedMinimize = false;
            mBubbleManager.hide();
            paramsF.flags = 0;
            paramsF.width = WindowManager.LayoutParams.MATCH_PARENT;
            paramsF.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            mBubble.makeToast(getString(R.string.toast_on_minimize));
            mBubbleManager.show();
            paramsF.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            paramsF.width = WindowManager.LayoutParams.WRAP_CONTENT;
            paramsF.height = WindowManager.LayoutParams.WRAP_CONTENT;
            sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));
        }
        mWindowManager.updateViewLayout(image, paramsF);

        if (image.getVisibility() == View.VISIBLE)
            image.requestFocus();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mEditor.onDestroy();

        removeView();
        sService = null;

    }


    private void removeView() {
        if (contains) {
            mWindowManager.removeViewImmediate(image);
            mBubbleManager.remove();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == LOCK_MSG)
                lock();
        }

    };

    public void startScreenshotThread() {
        if (!screenshotThreadIsStarted) {
            screenshotThreadIsStarted = true;
            mLastCheck = System.currentTimeMillis();
            new Thread() {
                public void run() {
                    while (sService != null) {
                        try {
                            File pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File screenshots = new File(pix, "Screenshots");

                            File[] files = screenshots.listFiles();
                            for (File file : files) {
                                if (file.lastModified() >= mLastCheck && file.getName().toLowerCase().endsWith("png")) {
                                    long l = file.length();
                                    Thread.sleep(5000);

                                    mScreenshotPath = "file://" + file.getAbsolutePath();

                                    mHandler.post(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          mBubble.makeToast(getString(R.string.add_screenshot));
                                                      }
                                                  }
                                    );
                                }
                            }

                            screenshots = new File("/storage/sdcard1/Pictures/Screenshots");
                            if (screenshots.exists()) {

                                files = screenshots.listFiles();
                                for (File file : files) {
                                    if (file.lastModified() >= mLastCheck && file.getName().toLowerCase().endsWith("png")) {
                                        long l = file.length();
                                        Thread.sleep(5000);

                                        mScreenshotPath = "file://" + file.getAbsolutePath();

                                        mHandler.post(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              mBubble.makeToast(getString(R.string.add_screenshot));
                                                          }
                                                      }
                                        );
                                    }
                                }
                            }

                            mLastCheck = System.currentTimeMillis();

                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

    }

    private void lock() {
        if (isLocked)
            return;
        isLocked = true;
        PinView pinView = new PinView(this);
        pinView.setPasswordListener(this);
        addFragment(pinView);
    }

    @Override
    public void onClick(View view) {
        if (view == mContent.findViewById(R.id.minimize)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHasPressedMinimize = true;
                    requestMinimize();
                }
            }, 200);

        } else if (view == mDim) {
            if (image.getAlpha() == 1)
                image.setAlpha((float) 0.5);
            else
                image.setAlpha(1);

        } else if (view == mContent.findViewById(R.id.close)) {
            sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 200);

        } else if (view == mShadowButton) {

            if (image.getAlpha() == 1) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSPARENT);

                params.x = 0;
                params.y = 0;
                ((ViewGroup) mTitleBar.getParent()).removeView(mTitleBar);
                mWindowManager.addView(mTitleBar, params);

                paramsF.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            } else {
                mWindowManager.removeViewImmediate(mTitleBar);
                ((ViewGroup) mContent).addView(mTitleBar, 0);
                paramsF.flags = 0;
            }
            view.setFocusable(true);

            view.setFocusableInTouchMode(true);
            view.setEnabled(true);
            view.setClickable(true);


            image.setAlpha(image.getAlpha() == 1 ? (float) 0.5 : 1);
            mWindowManager.updateViewLayout(image, paramsF);
        }
    }

    @Override
    public void onHide(boolean hide) {

        if (hide) {
            mLastVisibility = image.getVisibility();
            image.setVisibility(View.GONE);
        } else image.setVisibility(mLastVisibility);
    }

    @Override
    public void onExit() {
        stopSelf();
    }


    public void requestMinimize() {
        if (image.getVisibility() == View.VISIBLE) {
            invert();
            if (PreferenceHelper.shouldLockOnMinimize(this)) {
                mHandler.removeMessages(LOCK_MSG);
                mHandler.sendEmptyMessageDelayed(LOCK_MSG, PreferenceHelper.getLockTime(this));
            }

        }
    }

    public void requestRestore() {
        if (!mHasPressedMinimize && contains) {
            requestMaximize();
        }
    }

    public void requestMaximize() {
        if (image.getVisibility() != View.VISIBLE && contains) {
            invert();
            mHandler.removeMessages(LOCK_MSG);
        }
    }

    @Override
    public void onMainBubbleClick() {
        if (mScreenshotPath == null)
            requestMaximize();

    }

    @Override
    public void onSecondaryBubbleClick(View v) {

    }

    @Override
    public void onRemoveToast() {
        mScreenshotPath = null;
    }

    @Override
    public void onDisplayToast() {

    }

    @Override
    public void onBackPressed() {
        if (mFragments.size() > 1 && !isLocked)
            removeFragment();
        else
            requestMinimize();
    }

    @Override
    public boolean checkPassword(String password) {
        if (PreferenceHelper.getPassword(this).equals(password))
            return true;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPasswordOk() {
        removeFragment();
        isLocked = false;
    }

    public void askDestroy() {
        Log.d("askDestroydebug", "askDestroy");
        stopSelf();
    }
}
