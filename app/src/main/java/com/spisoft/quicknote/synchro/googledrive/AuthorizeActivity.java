package com.spisoft.quicknote.synchro.googledrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.spisoft.quicknote.R;
import com.spisoft.quicknote.synchro.SynchroService;

public class AuthorizeActivity extends AppCompatActivity {

    private DriveWrapper mDriveWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_authorize);
        mDriveWrapper = new DriveWrapper(this, -1);
        mDriveWrapper.authorize(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case DriveWrapper.RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {

                 //   startService(new Intent(this, SynchroService.class));

                }

                break;
        }
        finish();
    }

}
