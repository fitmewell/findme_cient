package com.hongqi.findme_ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.location.LocationClient;
import com.hongqi.findmeHttp.HttpUtil;
import com.hongqi.findmeHttp.envirCheck;
import com.hongqi.findmeHttp.getFriendList;
import com.hongqi.findme_ui.login_fragment.onLoginSuccessListener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class friendlist_fragment extends Fragment{
	String username,uid;
	ShowTheWay showTheWay;
	
	public interface ShowTheWay{
		public void translateInfo(String username,String friendname,String friendID,String localX,String localY);

	}
	
	
	public void updateUserInfo(String id,String uname){
		username = uname;
		uid = id;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		try{
			showTheWay = (ShowTheWay)activity;
    	}catch (Exception e){
    		e.printStackTrace();
    		Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
    	}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.friendlist_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Button button = (Button)getActivity().findViewById(R.id.back);
		TextView title = (TextView)getActivity().findViewById(R.id.title);
		ListView listview = (ListView)getActivity().findViewById(R.id.listview);
		Spinner spinner = (Spinner)getActivity().findViewById(R.id.right_btn);
		spinner.setVisibility(View.GONE);
		button.setVisibility(View.VISIBLE);
		button.setText("|");
		title.setGravity(Gravity.CENTER);
		title.setText("好友列表");
		envirCheck.check();
//		Toast.makeText(getActivity(), uid, Toast.LENGTH_LONG).show();
	    SimpleAdapter adapter = new SimpleAdapter(getActivity(),new getFriendList().getFriendList(HttpUtil.getFriendList(uid)),
	                R.layout.friends,
	                new String[]{"uid","name","online"},
	                new int[]{R.id.uid,R.id.name,R.id.online});
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView uid_ = (TextView)view.findViewById(R.id.uid);
	            TextView name_ = (TextView)view.findViewById(R.id.name);
	            TextView online = (TextView)view.findViewById(R.id.online);
	            String x = new getFriendList().getFriendLocal(uid_.getText().toString()).get("x").toString();
	            String y = new getFriendList().getFriendLocal(uid_.getText().toString()).get("y").toString();
	            if(!online.getText().toString().equals("离线")){
	               showTheWay.translateInfo(username, name_.getText().toString(), uid_.getText().toString(), x, y);
	            }
	            else
	            {
	                Toast toast = Toast.makeText(getActivity(), "好友不在线", Toast.LENGTH_SHORT);
	                toast.show();
	            }
				
			}
		});;
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getFragmentManager().beginTransaction().addToBackStack(null).add(R.id.pop_menu, new menu_fragment()).commit();
			}
		});
	}
}
