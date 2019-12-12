package com.spisoft.quicknote.indexer;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

/**
 * Created by alexandre on 05/07/17.
 */

public class IndexerService extends IntentService {
    public static final String ACTION_UPDATE_OR_ADD = "action_update_or_add";
    public static final String ACTION_DELETE = "action_delete";
    public static final String ACTION_RESCAN_ALL = "action_rescan_all";

    public IndexerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()){
            case ACTION_UPDATE_OR_ADD:

                break;
            case ACTION_DELETE:

                break;
            case ACTION_RESCAN_ALL:

                break;
        }
    }


}
