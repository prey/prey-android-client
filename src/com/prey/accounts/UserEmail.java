package com.prey.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

public class UserEmail {

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static String getEmail222(Context context) {
	    AccountManager accountManager = AccountManager.get(context); 
	    Account account = getAccount(accountManager);

	    if (account == null) {
	      return null;
	    } else {
	      return account.name;
	    }
	  }

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static Account getAccount(AccountManager accountManager) {
	    Account[] accounts = accountManager.getAccountsByType("com.google");
	    Account account;
	    if (accounts.length > 0) {
	      account = accounts[0];      
	    } else {
	      account = null;
	    }
	    return account;
	}

}
