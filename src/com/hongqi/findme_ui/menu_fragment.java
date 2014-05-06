package com.hongqi.findme_ui;

import com.hongqi.findmeHttp.HttpUtil;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class menu_fragment extends Fragment{
	
	FrameLayout framelayout;
	Button addFriBtn;
	EditText addFriText;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.menu_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		framelayout = (FrameLayout)getActivity().findViewById(R.id.pop_menu);
		TextView title = (TextView)getActivity().findViewById(R.id.title);
		addFriBtn = (Button)getActivity().findViewById(R.id.addFri_btn);
		addFriText = (EditText)getActivity().findViewById(R.id.addFri_text);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		framelayout.setLayoutParams(params);
		framelayout.getBackground().setAlpha(100);;
		framelayout.setVisibility(View.VISIBLE);
		framelayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				framelayout.setVisibility(View.GONE);
				return true;
			}
		});
		
		addFriBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String friendID = addFriText.getText().toString();
				TextView uid = (TextView)getActivity().findViewById(R.id.title);
				Toast.makeText(getActivity(),friendID+ HttpUtil.addFriend(uid.getText().toString(), friendID), Toast.LENGTH_SHORT).show();
			}
		});
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		framelayout.setVisibility(View.GONE);
		super.onDestroy();
	}
}
