package com.spisoft.quicknote.billingutils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BillingUtils implements IabHelper.QueryInventoryFinishedListener, IabHelper.OnIabPurchaseFinishedListener {
	IInAppBillingService mService;
	String base64EncodedPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgDz8BVIOAU5oL+OPCgX8fONA/E9QLzNqw5o+UaH0GeSM0jrajpqSPBQmwLUZnB3xSTkDIfeFmA9ar2MrD6iuMdqEz5g0OnA5AMHOim/Z4jQeR73ejtmHMMHNJfez4n/Cn5z/lVhK2LGFkLBUwTzsFDdpjUr7YeBys/W85rVD6LF3gcLAPEekIfcXUxhvM6ABkGAnjOAMmpry/gAanb1H7ARkjeoVLY3hsGtzCJaYgKbL1XdQ32nKeIFbt6AjgPw+jgiYFjcW39gDyTP7VFBoziNMi7erFP9vVoLLe9VrICTQVY9FPzVZkEkC0v61YTHTlKUsC7H0X6EL6B0NNOmLowIDAQAB";
	IabHelper mHelper;
	String ID = "adfree";
	IsPaidCallback ispc=null;
	public static String PAID_STATUS_PREF="pref_paid_status";
	public static final int HAS_BEEN_PURCHASE = 0;
	public static final int CHECKING_IMPOSSIBLE = 1;
	public static final int HAS_NOT_BEEN_PURCHASE = 2;
	public static final int CHECKING_IMPOSSIBLE_PURCHASE_FLOW=3;
	
	private static final boolean DBG = false;

	public BillingUtils(Context ct){
		mHelper = new IabHelper(ct, base64EncodedPublicKey);

	}
	public void checkPayement( final IsPaidCallback ispc){
		this.ispc=ispc;
		try{
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					if (!result.isSuccess()) {
						// Oh noes, there was a problem.
						ispc.hasBeenPaid(CHECKING_IMPOSSIBLE);
					}
					else{
						checkPayementAfterSetup(ispc);
					}

				}
			});
		}
		catch (IllegalStateException e){
			if(mHelper.mSetupDone)
				checkPayementAfterSetup(ispc);
		}
	}
	private void checkPayementAfterSetup( IsPaidCallback ispc){
		List<String> additionalSkuList = new ArrayList<String>();
		additionalSkuList.add(ID);
		mHelper.queryInventoryAsync(true,additionalSkuList,
				BillingUtils.this);
	}
	public void purchaseAfterSetup(Activity act, IsPaidCallback ispc){
		if(mHelper.mSetupDone ){
			try{
				mHelper.launchPurchaseFlow(act, ID, 10001,   
						this, UUID.randomUUID().toString());
			} catch (IllegalStateException s){
				//we wait and we try again
				try {
					Thread.sleep(1000);
					try{
						mHelper.launchPurchaseFlow(act, ID, 10001,   
								this, UUID.randomUUID().toString());
					} catch (IllegalStateException s2){

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void purchase(final Activity act, final IsPaidCallback ispc){
		if(mHelper.mSetupDone ){
			purchaseAfterSetup(act, ispc);
		}
		else if(!mHelper.mAsyncInProgress){
			try{
				mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					@Override
					public void onIabSetupFinished(IabResult result) {
						purchaseAfterSetup(act, ispc);
					}
				});
			} catch (IllegalStateException s){
				//an async task is still running, we can wait (the on
				try {
					Thread.sleep(500);
					purchaseAfterSetup(act, ispc);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void onQueryInventoryFinished(IabResult result,
			Inventory inv) {
		if (result.isFailure()) {
			// handle error here
			if(ispc!=null)
				ispc.hasBeenPaid(CHECKING_IMPOSSIBLE);
		}
		else {
			if(DBG )Log.d("billing", "ok :"+(inv.hasPurchase(ID)?"has it":"hasn't it"));
			// does the user have the premium upgrade?   
			if(ispc!=null)
				ispc.hasBeenPaid(inv.hasPurchase(ID)?HAS_BEEN_PURCHASE:inv.hasDetails(ID)?HAS_NOT_BEEN_PURCHASE:CHECKING_IMPOSSIBLE);
		}

	}
	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		if (result.isFailure()) {
			ispc.hasBeenPaid(result.getResponse()==IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED||result.getResponse()==IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED?HAS_BEEN_PURCHASE:CHECKING_IMPOSSIBLE_PURCHASE_FLOW);
			return;
		}      
		else if (info.getSku().equals(ID)) {
			// consume
			//checkPayement(ispc);
			ispc.hasBeenPaid(info.mPurchaseState==IabHelper.BILLING_RESPONSE_RESULT_OK||info.mPurchaseState==IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED?HAS_BEEN_PURCHASE:HAS_NOT_BEEN_PURCHASE);
		}
		//ispc.hasBeenPaid(HAS_NOT_BEEN_PURCHASE);
	}
	public boolean handleActivityResult(int requestCode, int resultCode,
			Intent data) {
		return mHelper.handleActivityResult(requestCode, resultCode, data);

	}
}
