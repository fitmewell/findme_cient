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

import com.hongqi.findmeHttp.HttpUtil;
import com.hongqi.findmeHttp.envirCheck;
import com.hongqi.findmeService.LoginCheck;
import com.hongqi.findme_ui.friendlist_fragment.ShowTheWay;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class login_fragment extends Fragment {

	onLoginSuccessListener mycallback;
	ListView listview;
	TextView title, username, password;
	Button registerBtn, loginbtn, button;
	CheckBox remenPass, autoLogin = null;
	boolean remembered, autoLogined;
	SharedPreferences sp;
	SharedPreferences.Editor editor = null;
	Handler LoginHandler;
	ImageView process_image;
	Animation animation;
	LinearLayout logindiv;
	public interface onLoginSuccessListener {
		public void onLoginSuccess(String ui, String uname);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mycallback = (onLoginSuccessListener) activity;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.login_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Spinner spinner = (Spinner) getActivity().findViewById(R.id.right_btn);
		spinner.setVisibility(View.GONE);
		animation = new AnimationUtils().loadAnimation(getActivity(), R.animator.test_anim);
		sp = getActivity().getSharedPreferences("UserInfo",
				Context.MODE_PRIVATE);
		editor = sp.edit();
		autoLogined = sp.getBoolean("autologin", false);
		Toast.makeText(getActivity(),
				String.valueOf(sp.getString("username", "")),
				Toast.LENGTH_SHORT).show();
		

		title = (TextView) getActivity().findViewById(R.id.title);
		username = (TextView) getActivity().findViewById(R.id.username);
		password = (TextView) getActivity().findViewById(R.id.password);
		registerBtn = (Button) getActivity().findViewById(R.id.registerbtn);
		loginbtn = (Button) getActivity().findViewById(R.id.loginbtn);
		button = (Button) getActivity().findViewById(R.id.back);
		remenPass = (CheckBox) getActivity().findViewById(R.id.checkBox1);
		autoLogin = (CheckBox) getActivity().findViewById(R.id.checkBox2);
		process_image = (ImageView)getActivity().findViewById(R.id.process_img);
		logindiv = (LinearLayout)getActivity().findViewById(R.id.login_div);
		
		
		if (autoLogined) {
			success(sp.getString("username", ""), sp.getString("password", ""),
					true, true);
			autoLogin.setChecked(true);
		}
		button.setVisibility(View.GONE);
		title.setText("登录");

		remembered = sp.getBoolean("remenmbered", false);
		if (remembered) {
			username.setText(sp.getString("username", ""));
			password.setText(sp.getString("password", ""));
			remenPass.setChecked(true);
		}

		loginbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				success(username.getText().toString(), password.getText()
						.toString(), remenPass.isChecked(), autoLogin
						.isChecked());

			}
		});

		registerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				register_fragment r = new register_fragment();
				// TODO Auto-generated method stub
				getFragmentManager().beginTransaction().addToBackStack(null)
						.replace(R.id.active_main, r).commit();
			}
		});

		remenPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!isChecked) {
					editor.putString("username", "");
					editor.putString("password", "");
					editor.putBoolean("remenmbered", false);
					editor.commit();
				} else if (isChecked) {
				}
			}
		});
		autoLogin.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (!isChecked) {
					editor.putBoolean("autologin", false);
					editor.commit();
				}if(isChecked){
					remenPass.setChecked(true);
				}
			}
		});
		LoginHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					String returnM = msg.obj.toString();
					Toast.makeText(getActivity(), returnM, Toast.LENGTH_LONG).show();
					Boolean isRemPass = remenPass.isChecked();
					Boolean loginChecked = autoLogin.isChecked();
					if (!returnM.equals("false") && returnM != null) {
						if (isRemPass) {
							editor.putString("username", username.getText()
									.toString());
							editor.putString("password", password.getText()
									.toString());
							editor.putBoolean("remenmbered", true);
							editor.commit();

							if (loginChecked) {
								editor.putBoolean("autologin", true);
								editor.commit();
							}
							process_image.clearAnimation();
						}
						// Toast.makeText(getActivity(),String.valueOf(isRemPass)+String.valueOf(sp.getBoolean("autologin",false))+sp.getString("username",""),Toast.LENGTH_SHORT).show();
						mycallback.onLoginSuccess(returnM, username.getText()
								.toString());

					} else if (returnM.equals("false") || returnM == null) {
						Toast toast = Toast.makeText(getActivity(),
								"用户名或者密码错误", Toast.LENGTH_SHORT);
						toast.show();
					}
					removeCallbacks(checklogin);
//					logindiv.setVisibility(View.VISIBLE);
//					title.setText("登录");
					break;
				case 2:
					Toast.makeText(getActivity(),
							"连接超时", Toast.LENGTH_SHORT).show();;
					removeCallbacks(checklogin);
					logindiv.setVisibility(View.VISIBLE);
					title.setText("登录");
					process_image.clearAnimation();
					break;
				default:
					break;
				}

			}
		};

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// Toast.makeText(getActivity(), "Destroy", Toast.LENGTH_LONG).show();

		this.LoginHandler.removeCallbacks(checklogin);
		super.onDestroy();
	}

	Runnable checklogin = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
//			envirCheck.check();
			String returnM = "";
			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 20000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 20000);
			
			HttpPost post = new HttpPost("http://findmeweb.sinaapp.com/servlet/logincheck");
			List<NameValuePair> list = new ArrayList<>();
			list.add(new BasicNameValuePair("u", username.getText()
					.toString()));
			list.add(new BasicNameValuePair("p", password.getText()
					.toString()));
			Log.i("Tells1", returnM);
			Message message = new Message();
			try {
				post.setEntity(new UrlEncodedFormEntity(list));
				HttpResponse response = client.execute(post);
				returnM = EntityUtils.toString(response.getEntity());
				Log.i("Tells1", returnM);
				message.what = 1;
				Log.i("Tells", returnM);
				message.obj = returnM;
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				message.what = 2;
				e.printStackTrace();
			}catch (SocketTimeoutException e) {
				// TODO: handle exception
				e.printStackTrace();
				message.what = 2;
			} catch (ConnectTimeoutException e) {
				// TODO: handle exception
				e.printStackTrace();
				message.what = 2;
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				message.what = 2;
			}
			
			
			Looper.prepare();
			login_fragment.this.LoginHandler.sendMessage(message);

		}
	};

	public void success(String username_, String password_, Boolean isRemPass,
			Boolean loginChecked) {
		// MainActivity mains =new MainActivity();
		// this.setContentView(R.layout.activity_main);
		logindiv.setVisibility(View.INVISIBLE);
		process_image.startAnimation(animation);
		title.setText("登录中");
		new Thread(checklogin).start();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
}
