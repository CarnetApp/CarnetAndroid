package com.spisoft.quicknote.billingutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public abstract class IsPaidCallback {
	public static final boolean DEBUG = false;
	private Context mContext;
	public IsPaidCallback(Context ct){
		mContext = ct; //used to debug
	}
	public boolean checkPayement(int isPaidResponse){
		boolean hasBeenPaid = isPaidResponse==BillingUtils.HAS_BEEN_PURCHASE;
    	if(isPaidResponse==1) { //Network error, we check on pref and take last value
    		hasBeenPaid = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(BillingUtils.PAID_STATUS_PREF, false);
    	}
    	return hasBeenPaid;
	}
	public  void hasBeenPaid(int isPaid){
		//if has been paid  or not (not network error) we save status)
		if(isPaid==0 || isPaid==2){
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(BillingUtils.PAID_STATUS_PREF,isPaid==0).commit();
		
		}	
		if(DEBUG){
			String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

			BufferedWriter bW;
			ConnectivityManager cm =
					(ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null &&
					activeNetwork.isConnectedOrConnecting();
			try {
				bW = new BufferedWriter(new FileWriter("/sdcard/archosbillingdebug.txt", true));
				bW.write(currentDateTimeString+ ": " + "is paid : "+isPaid+ " network: "+String.valueOf(isConnected));
				bW.newLine();
				bW.flush();
				bW.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
