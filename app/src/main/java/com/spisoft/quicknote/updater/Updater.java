package com.spisoft.quicknote.updater;

import android.content.Context;

public interface Updater {

    public void update(Context ct, int oldVersion, int newVersion);
}
