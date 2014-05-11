package com.hongqi.findme_ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hongqi.findmeHttp.HttpUtil;
import com.hongqi.findmeHttp.envirCheck;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class register_fragment extends Fragment{
	EditText username,password,repassword,name;
	Button registerbtn,quitbtn;
	TextView unameInfo,nameInfo,passInfo,repassInfo;
	Boolean registerInfoPass = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.register_fragment, container, false);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();
		username = (EditText)activity.findViewById(R.id.regusername);
        password = (EditText)activity.findViewById(R.id.registerPassword);
        repassword = (EditText)activity.findViewById(R.id.repeatPassword);
        name = (EditText)activity.findViewById(R.id.regname);
        Spinner spinner = (Spinner)getActivity().findViewById(R.id.right_btn);
		spinner.setVisibility(View.GONE);

        registerbtn = (Button)activity.findViewById(R.id.register);
        quitbtn = (Button)activity.findViewById(R.id.returnregister);
        unameInfo = (TextView)activity.findViewById(R.id.re_usernameInfo);
        nameInfo = (TextView)activity.findViewById(R.id.re_nameInfo);
        passInfo = (TextView)activity.findViewById(R.id.re_passInfo);
        repassInfo = (TextView)activity.findViewById(R.id.re_repassInfo);
        username.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus){
                    if(!isEmail(username.getText().toString())||username.getText() == null){
                        Toast.makeText(getActivity(), "格式错误" +!isEmail(username.getText().toString()), Toast.LENGTH_SHORT).show();
                        registerInfoPass = false;
                        unameInfo.setTextColor(Color.RED);
                    }
                    else if (isEmail(username.getText().toString())&&username.getText() != null){
                        registerInfoPass = true;
                        unameInfo.setTextColor(Color.GREEN);
                    }
                }
            }
        });

        password.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus){
                    if (!isPassword(password.getText().toString()) || password.getText() == null){
                        registerInfoPass = false;
                        Toast.makeText(getActivity(), "密码格式不正确", Toast.LENGTH_SHORT).show();
                        passInfo.setTextColor(Color.RED);
                    }
                    else if (isPassword(password.getText().toString()) && password.getText() != null){
                        registerInfoPass = true;
                        passInfo.setTextColor(Color.GREEN);
                    }
                }
            }
        });

        repassword.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus){
                    if (!password.getText().toString().equals(repassword.getText().toString())){
                        registerInfoPass = false;
                        repassInfo.setTextColor(Color.RED);
                        Toast.makeText(getActivity(), "两次的密码不一致", Toast.LENGTH_SHORT).show();
                    }
                    else if (password.getText().toString().equals(repassword.getText().toString()) && !repassword.getText().toString().equals("")){
                        registerInfoPass = true;
                        repassInfo.setTextColor(Color.GREEN);
                    }
                }
            }
        });

        name.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus){
                    if (name.getText().toString().equals("")){
                        registerInfoPass = false;
                        nameInfo.setTextColor(Color.RED);
                        Toast.makeText(getActivity(), "请输入用户名", Toast.LENGTH_SHORT).show();
                    }
                    else if (!name.getText().toString().equals("")){
                        registerInfoPass = true;
                        nameInfo.setTextColor(Color.GREEN);
                    }
                }
            }
        });
        registerbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!username.getText().toString().equals("") && !name.getText().toString().equals("") && registerInfoPass == true && !password.getText().toString().equals("") &&
                        !repassword.getText().toString().equals(""))
                {
                    envirCheck.check();
                    Toast.makeText(getActivity(),HttpUtil.registerUser(username.getText().toString(), name.getText().toString(), password.getText().toString()),Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        quitbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getFragmentManager().beginTransaction().replace(R.id.active_main, new login_fragment()).commit();
			}
		});
    }
    public static boolean isEmail(String strEmail) {

        //正则表达式确定邮箱格式
        String strPattern = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";

        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    public static boolean isPassword(String password_){

        //正则表达式确定密码格式
        String strPattern = "^[a-zA-Z]\\w{5,17}$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(password_);
        return m.matches();
    }
    
}
