package com.spisoft.quicknote;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HelpAuthorizeFloatingWindowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_authorize_floating_window);


    }

    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT>=  Build.VERSION_CODES.M&&Settings.canDrawOverlays(this)){
            Intent intent = new Intent(this, FloatingService.class);
            intent.putExtra(FloatingService.NOTE, getIntent().getSerializableExtra(FloatingService.NOTE));
            intent.putExtra(FloatingService.START_MINIMIZE, true);
            startService(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help_authorize_floating_window, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAuthorizeClick(View view) {
        Intent in = new Intent();
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        in.putExtra("android.intent.extra.PACKAGE_NAME", getPackageName());
        startActivity(in);
    }

    public void onCancelClick(View view) {
        finish();
    }
}
