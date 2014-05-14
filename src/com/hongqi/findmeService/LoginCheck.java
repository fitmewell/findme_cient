package com.hongqi.findmeService;

import com.hongqi.findmeHttp.HttpUtil;
import com.hongqi.findmeHttp.envirCheck;
import com.hongqi.findme_ui.login_fragment;

import android.R.anim;
import android.app.IntentService;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

public class LoginCheck extends IntentService {
	String result;
	public LoginCheck() {
		super("");
		// TODO Auto-generated constructor stub
	}

	public LoginCheck(String name) {
		super("");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String username = intent.getStringExtra("username");
		String password = intent.getStringExtra("password");
		envirCheck.check();
		String returnM = HttpUtil.queryStringForPost(username, password);
		if (!returnM.equals("false")) {
			result = returnM;
		} else if (returnM.equals("false") || returnM == null) {
			result = "false";
		}
		Message message = new Message();
		message.what = 1;
		message.obj = result;
		Handler handler = new Handler();
		handler.sendMessage(message);
	}

}
