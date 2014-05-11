package com.hongqi.findme_ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hongqi.findmeHttp.HttpUtil;
import com.hongqi.findmeHttp.envirCheck;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
		sp = getActivity().getSharedPreferences("UserInfo",
				Context.MODE_PRIVATE);
		editor = sp.edit();
		autoLogined = sp.getBoolean("autologin", false);
		Toast.makeText(getActivity(),
				String.valueOf(sp.getString("username", "")),
				Toast.LENGTH_SHORT).show();
		if (autoLogined) {
			success(sp.getString("username", ""), sp.getString("password", ""),
					true, true);
		}

		title = (TextView) getActivity().findViewById(R.id.title);
		username = (TextView) getActivity().findViewById(R.id.username);
		password = (TextView) getActivity().findViewById(R.id.password);
		registerBtn = (Button) getActivity().findViewById(R.id.registerbtn);
		loginbtn = (Button) getActivity().findViewById(R.id.loginbtn);
		button = (Button) getActivity().findViewById(R.id.back);
		remenPass = (CheckBox) getActivity().findViewById(R.id.checkBox1);
		autoLogin = (CheckBox) getActivity().findViewById(R.id.checkBox2);

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
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// Toast.makeText(getActivity(), "Destroy", Toast.LENGTH_LONG).show();
		super.onDestroy();
	}

	public void success(String username_, String password_, Boolean isRemPass,
			Boolean loginChecked) {
		// MainActivity mains =new MainActivity();
		// this.setContentView(R.layout.activity_main);

		envirCheck.check();
		String returnM = HttpUtil.queryStringForPost(username_, password_);
		if (!returnM.equals("false") && returnM != null) {
			// Boolean isRemPass = remenPass.isChecked();
			// Boolean loginChecked = autoLogin.isChecked();
			if (isRemPass) {
				editor.putString("username", username_);
				editor.putString("password", password_);
				editor.putBoolean("remenmbered", true);
				editor.commit();

				if (loginChecked) {
					editor.putBoolean("autologin", true);
					editor.commit();
				}
			}
			// Toast.makeText(getActivity(),String.valueOf(isRemPass)+String.valueOf(sp.getBoolean("autologin",false))+sp.getString("username",""),Toast.LENGTH_SHORT).show();
			mycallback.onLoginSuccess(returnM, username_);

		} else if (returnM.equals("false") || returnM == null) {
			Toast toast = Toast.makeText(getActivity(), "用户名或者密码错误",
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}
}
