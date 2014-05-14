package com.hongqi.findme_ui;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Text;

import com.hongqi.findme_ui.friendlist_fragment.ShowTheWay;
import com.hongqi.findme_ui.login_fragment.onLoginSuccessListener;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity implements onLoginSuccessListener,
		ShowTheWay {
	String username = null, uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);
		getFragmentManager().beginTransaction()
				.replace(R.id.active_main, new login_fragment()).commit();

	}

	public String getUID() {
		return this.uid;
	}

	@Override
	public void onLoginSuccess(String ui, String uname) {
		username = uname;
		// TODO Auto-generated method stub
		Toast.makeText(this, ui + "Success" + uname, Toast.LENGTH_LONG).show();
		friendlist_fragment friendlist = new friendlist_fragment();
		friendlist.updateUserInfo(ui, uname);
		getFragmentManager().beginTransaction()
				.replace(R.id.active_main, friendlist).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_settings:
			this.setTitle("");
			break;
		case R.id.quit_user:
			logOut();
			SharedPreferences sp = getSharedPreferences("UserInfo",
					Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("autologin", false);
			editor.commit();
			getFragmentManager().beginTransaction()
					.replace(R.id.active_main, new login_fragment()).commit();
			break;
		case R.id.quit_program:
			logOut();
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			TextView title = (TextView) findViewById(R.id.title);
			String titleTXT = title.getText().toString();
			if (titleTXT.equals("设置") || titleTXT.equals("注册")
					|| titleTXT.equals("登录") || titleTXT.equals("地图")) {
				return super.onKeyDown(keyCode, event);
			} else {
				moveTaskToBack(true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void translateInfo(String username, String friendname,
			String friendID, String localX, String localY) {
		// TODO Auto-generated method stub
		MapFragment mapFragment = new MapFragment();
		mapFragment.init(username, friendname, friendID, localX, localY);
		getFragmentManager().beginTransaction().addToBackStack(null)
				.setCustomAnimations(R.animator.flash_in, R.animator.flash_out)
				.replace(R.id.active_main, mapFragment).commit();
	}

	private void logOut() {
		if (username != null) {
			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams
					.setConnectionTimeout(client.getParams(), 10000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 10000);

			HttpPost post = new HttpPost(
					"http://findmeweb.sinaapp.com/servlet/logout");
			List<NameValuePair> list = new ArrayList<>();
			list.add(new BasicNameValuePair("u", username));
			try {
				post.setEntity(new UrlEncodedFormEntity(list));
				HttpResponse response = client.execute(post);

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return;
		}
	}
}
