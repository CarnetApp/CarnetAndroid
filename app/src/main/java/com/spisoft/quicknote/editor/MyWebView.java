package com.spisoft.quicknote.editor;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.WebView;

/**
 * Created by alexandre on 08/02/16.
 */
public class MyWebView extends WebView {
    public MyWebView(Context context) {
        super(context);
    }




    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {




       return new ActionMode() {
           @Override
           public void setTitle(CharSequence charSequence) {

           }

           @Override
           public void setTitle(int i) {

           }

           @Override
           public void setSubtitle(CharSequence charSequence) {

           }

           @Override
           public void setSubtitle(int i) {

           }

           @Override
           public void setCustomView(View view) {

           }

           @Override
           public void invalidate() {

           }

           @Override
           public void finish() {

           }

           @Override
           public Menu getMenu() {
               return null;
           }

           @Override
           public CharSequence getTitle() {
               return null;
           }

           @Override
           public CharSequence getSubtitle() {
               return null;
           }

           @Override
           public View getCustomView() {
               return null;
           }

           @Override
           public MenuInflater getMenuInflater() {
               return null;
           }
       };
    }

}
