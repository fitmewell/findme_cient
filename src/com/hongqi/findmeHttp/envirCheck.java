package com.hongqi.findmeHttp;

import android.os.StrictMode;

public class envirCheck {

	public envirCheck() {
	}
	
	public static void check(){
	 if(android.os.Build.VERSION.SDK_INT >= 11) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			.detectDiskReads()
			.detectDiskWrites()
			.detectNetwork() 
			.penaltyLog()
			.build());

			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			.detectLeakedSqlLiteObjects()
			.penaltyLog()
			.penaltyDeath()
			.build());
			}
	}
}
