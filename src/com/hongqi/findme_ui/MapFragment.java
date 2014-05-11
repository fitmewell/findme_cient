package com.hongqi.findme_ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.map.TransitOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKCityListInfo;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.hongqi.findmeHttp.HttpUtil;
import com.hongqi.findmeHttp.envirCheck;
import com.hongqi.findmeHttp.getFriendList;

public class MapFragment extends Fragment {
	ImageView dingwei;

	// 浏览路线节点相关
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	Button titleBtn;
	Spinner spinner = null;// 下拉菜单
	TextView simpleWay, shortestWay, timelessWay, Title;
	LinearLayout titlebar;

	int nodeIndex = -2;// 节点索引,供浏览节点时使用
	MKRoute route = null;// 保存驾车/步行路线数据的变量，供浏览节点时使用
	TransitOverlay transitOverlay = null;// 保存公交路线图层数据的变量，供浏览节点时使用
	RouteOverlay routeOverlay = null;
	boolean useDefaultIcon = false;
	int searchType = -1;// 记录搜索的类型，区分驾车/步行和公交
	private PopupOverlay pop = null;// 弹出泡泡图层，浏览节点时使用
	private TextView popupText = null;// 泡泡view
	private View viewCache = null;
	private MKPlanNode stNode, edNode;
	private GeoPoint stGeoPoint, edGeoPoint;
	private MapController mapController;
	private LocationClient locationClient;
	private MyLocationListenner myLocationListenner;
	private static final String[] m = { " 驾车 ", " 步行 ", " 公交 " };
	private ArrayAdapter<String> adapter;
	BMapManager bMapManager;

	private String username, friendID, city;

	// 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	MapView mMapView = null; // 地图View
	// 搜索相关
	MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	public void init(String username, String friendname, String friendID,
			String localX, String localY) {
		this.username = username;
		this.friendID = friendID;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		bMapManager = new BMapManager(getActivity().getApplication());
		bMapManager.init(null);
		return inflater.inflate(R.layout.map_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		locationClient = new LocationClient(getActivity());
		stNode = new MKPlanNode();
		edNode = new MKPlanNode();
		myLocationListenner = new MyLocationListenner();
		locationClient.registerLocationListener(myLocationListenner);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		locationClient.setLocOption(option);

		locationClient.start();

		Title = (TextView) getActivity().findViewById(R.id.title);
		titleBtn = (Button) getActivity().findViewById(R.id.back);
		spinner = (Spinner) getActivity().findViewById(R.id.right_btn);
		Title.setText("地图");
		titleBtn.setText(" 定位 ");
		titleBtn.setVisibility(View.VISIBLE);

		mMapView = (MapView) getActivity().findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(false);
		mapController = mMapView.getController();
		mapController.setZoom(12);
		mapController.enableClick(true);
		adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_xml,
				m);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setVisibility(View.VISIBLE);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getActivity(),
						"Position:" + position + " id:" + id, Toast.LENGTH_LONG)
						.show();
				SearchButtonProcess(parent, position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				// 发起搜索
				mapController.animateTo(stNode.pt);
			}
		};
		OnClickListener nodeClickListener = new OnClickListener() {
			public void onClick(View v) {
				// 浏览路线节点
				nodeClick(v);
			}
		};

		// dingwei.setOnClickListener(clickListener);
		titleBtn.setOnClickListener(clickListener);
		// 创建 弹出泡泡图层
		createPaopao();

		// 地图点击事件处理
		mMapView.regMapTouchListner(new MKMapTouchListener() {

			@Override
			public void onMapClick(GeoPoint point) {
				// 在此处理地图点击事件
				// 消隐pop
				if (pop != null) {
					pop.hidePop();
				}
			}

			@Override
			public void onMapDoubleClick(GeoPoint point) {

			}

			@Override
			public void onMapLongClick(GeoPoint point) {

			}

		});
		// 初始化搜索模块，注册事件监听
		mSearch = new MKSearch();
		mSearch.init(bMapManager, new MKSearchListener() {

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					// ArrayList<MKPoiInfo> stPois =
					// res.getAddrResult().mStartPoiList;
					// ArrayList<MKPoiInfo> enPois =
					// res.getAddrResult().mEndPoiList;
					// ArrayList<MKCityListInfo> stCities =
					// res.getAddrResult().mStartCityList;
					// ArrayList<MKCityListInfo> enCities =
					// res.getAddrResult().mEndCityList;
					return;
				}
				// 错误号可参考MKEvent中的定义
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				routeOverlay = new RouteOverlay(getActivity(), mMapView);
				searchType = 0;

				// routeOverlay = new RouteOverlay(getMyFriend.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(routeOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 将路线数据保存给全局变量
				route = res.getPlan(0).getRoute(0);
				// 重置路线节点索引，节点浏览时使用
				// nodeIndex = -1;
				// mBtnPre.setVisibility(View.VISIBLE);
				// mBtnNext.setVisibility(View.VISIBLE);
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					ArrayList<MKPoiInfo> stPois = res.getAddrResult().mStartPoiList;
					ArrayList<MKPoiInfo> enPois = res.getAddrResult().mEndPoiList;
					ArrayList<MKCityListInfo> stCities = res.getAddrResult().mStartCityList;
					ArrayList<MKCityListInfo> enCities = res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 1;
				transitOverlay = new TransitOverlay(getActivity(), mMapView);
				// 此处仅展示一个方案作为示例
				transitOverlay.setData(res.getPlan(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(transitOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						transitOverlay.getLatSpanE6(),
						transitOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = 0;
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					ArrayList<MKPoiInfo> stPois = res.getAddrResult().mStartPoiList;
					ArrayList<MKPoiInfo> enPois = res.getAddrResult().mEndPoiList;
					ArrayList<MKCityListInfo> stCities = res.getAddrResult().mStartCityList;
					ArrayList<MKCityListInfo> enCities = res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 2;
				routeOverlay = new RouteOverlay(getActivity(), mMapView);
				Toast.makeText(getActivity(),
						String.valueOf(res.getPlan(0).getRoute(0)),
						Toast.LENGTH_SHORT).show();
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(routeOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 将路线数据保存给全局变量
				route = res.getPlan(0).getRoute(0);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = -1;
				//

			}

			public void onGetPoiResult(MKPoiResult res, int arg1, int arg2) {
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
			}

			@Override
			public void onGetPoiDetailSearchResult(int type, int iError) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetAddrResult(MKAddrInfo res, int error) {
				// TODO Auto-generated method stub
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}
				city = res.addressComponents.city.toString();
			}
		});
	}

	void SearchButtonProcess(View v, int type) {
		// 重置浏览节点的路线数据
		route = null;
		routeOverlay = null;
		transitOverlay = null;

		Toast.makeText(getActivity(), "City:" + city, Toast.LENGTH_SHORT)
				.show();

		switch (type) {
		case (0):
			mSearch.drivingSearch(city, stNode, city, edNode);

			// Toast.makeText(getMyFriend.this,"drive",Toast.LENGTH_SHORT).show();
			break;
		case (1):
			mSearch.walkingSearch(city, stNode, city, edNode);
			// Toast.makeText(getMyFriend.this,"walk",Toast.LENGTH_SHORT).show();
			break;
		case (2):
			mSearch.transitSearch(city, stNode, edNode);

			// Toast.makeText(getMyFriend.this,"transite",Toast.LENGTH_SHORT).show();

			break;
		default:
			break;
		}
		// if (mBtnDrive.equals(v)) {
		// mSearch.drivingSearch(city, stNode, city, edNode);
		// } else if (mBtnTransit.equals(v)) {
		// mSearch.transitSearch(city, stNode, edNode);
		// } else if (mBtnWalk.equals(v)) {
		// mSearch.walkingSearch(city, stNode, city, edNode);
		// }else if (dingwei.equals(v)) {
		// mSearch.walkingSearch(city, stNode, city, edNode);
		// }
	}

	/**
	 * 节点浏览示例
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		viewCache = getActivity().getLayoutInflater().inflate(
				R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		if (searchType == 0 || searchType == 2) {
			// 驾车、步行使用的数据结构相同，因此类型为驾车或步行，节点浏览方法相同
			if (nodeIndex < -1 || route == null
					|| nodeIndex >= route.getNumSteps())
				return;

			// 上一个节点
			if (mBtnPre.equals(v) && nodeIndex > 0) {
				// 索引减
				nodeIndex--;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						route.getStep(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(route.getStep(nodeIndex).getContent());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText), route
						.getStep(nodeIndex).getPoint(), 5);
			}
			// 下一个节点
			if (mBtnNext.equals(v) && nodeIndex < (route.getNumSteps() - 1)) {
				// 索引加
				nodeIndex++;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						route.getStep(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(route.getStep(nodeIndex).getContent());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText), route
						.getStep(nodeIndex).getPoint(), 5);
			}
		}
		if (searchType == 1) {
			// 公交换乘使用的数据结构与其他不同，因此单独处理节点浏览
			if (nodeIndex < -1 || transitOverlay == null
					|| nodeIndex >= transitOverlay.getAllItem().size())
				return;

			// 上一个节点
			if (mBtnPre.equals(v) && nodeIndex > 1) {
				// 索引减
				nodeIndex--;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						transitOverlay.getItem(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(transitOverlay.getItem(nodeIndex).getTitle());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						transitOverlay.getItem(nodeIndex).getPoint(), 5);
			}
			// 下一个节点
			if (mBtnNext.equals(v)
					&& nodeIndex < (transitOverlay.getAllItem().size() - 2)) {
				// 索引加
				nodeIndex++;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						transitOverlay.getItem(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(transitOverlay.getItem(nodeIndex).getTitle());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						transitOverlay.getItem(nodeIndex).getPoint(), 5);
			}
		}

	}

	/**
	 * 创建弹出泡泡图层
	 */
	public void createPaopao() {

		// 泡泡点击响应回调
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	/**
	 * 跳转自设路线Activity
	 * 
	 * public void intentToActivity(){ //跳转到自设路线演示demo Intent intent = new
	 * Intent(this, CustomRouteOverlayDemo.class); startActivity(intent); }
	 */
	/**
	 * 切换路线图标，刷新地图使其生效 注意： 起终点图标使用中心对齐.
	 * 
	 * protected void changeRouteIcon() { // Button btn =
	 * (Button)findViewById(R.id.customicon); if ( routeOverlay == null &&
	 * transitOverlay == null){ return ; } if ( useDefaultIcon ){ if (
	 * routeOverlay != null){ routeOverlay.setStMarker(null);
	 * routeOverlay.setEnMarker(null); } if ( transitOverlay != null){
	 * transitOverlay.setStMarker(null); transitOverlay.setEnMarker(null); }
	 * btn.setText("自定义起终点图标"); Toast.makeText(this, "将使用系统起终点图标",
	 * Toast.LENGTH_SHORT).show(); } else{ if ( routeOverlay != null){
	 * routeOverlay.setStMarker(getResources().getDrawable(R.drawable.icon_st));
	 * routeOverlay.setEnMarker(getResources().getDrawable(R.drawable.icon_en));
	 * } if ( transitOverlay != null){
	 * transitOverlay.setStMarker(getResources().
	 * getDrawable(R.drawable.icon_st));
	 * transitOverlay.setEnMarker(getResources(
	 * ).getDrawable(R.drawable.icon_en)); } btn.setText("系统起终点图标");
	 * Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show(); }
	 * useDefaultIcon = !useDefaultIcon; mMapView.refresh();
	 * 
	 * }
	 */

	@Override
	public void onPause() {
		mMapView.onPause();
		// Toast.makeText(getActivity(), "Pause", Toast.LENGTH_LONG).show();
		super.onPause();
	}

	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// Toast.makeText(getActivity(), "Destroy", Toast.LENGTH_LONG).show();
		mMapView.destroy();
		mSearch.destory();
		locationClient.stop();
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			envirCheck.check();
			HttpUtil.mlocalUpdate(username, location.getLatitude(),
					location.getLongitude());

			getFriendList getFriendlocal = new getFriendList();
			edGeoPoint = new GeoPoint(
					(int) (Double.parseDouble(getFriendlocal
							.getFriendLocal(friendID).get("x").toString()) * 1e6),
					(int) (Double.parseDouble(getFriendlocal
							.getFriendLocal(friendID).get("y").toString()) * 1e6));
			stGeoPoint = new GeoPoint((int) (location.getLatitude() * 1e6),
					(int) (location.getLongitude() * 1e6));

			stNode.pt = stGeoPoint;
			edNode.pt = edGeoPoint;
			mSearch.reverseGeocode(edNode.pt);

		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	OnClickListener busWay = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (simpleWay.equals(v)) {
				mSearch.setTransitPolicy(MKSearch.EBUS_TRANSFER_FIRST);
			} else if (shortestWay.equals(v)) {
				mSearch.setTransitPolicy(MKSearch.EBUS_WALK_FIRST);
			} else if (timelessWay.equals(v)) {
				mSearch.setTransitPolicy(MKSearch.EBUS_TIME_FIRST);
			}
		}
	};

	// protected void ShowPopMenu(){
	// View view =
	// getActivity().getLayoutInflater().inflate(R.layout.pop_menu_bus,null,false);
	// PopupWindow popupWindow = new PopupWindow(view,200,150,true);
	// popupWindow.setAnimationStyle(R.style.AnimationFade);
	//
	// simpleWay = (TextView)getActivity().findViewById(R.id.simple_way);
	// shortestWay = (TextView)getActivity().findViewById(R.id.shortest_way);
	// timelessWay = (TextView)getActivity().findViewById(R.id.timeless_way);
	//
	// simpleWay.setOnClickListener(busWay);
	// shortestWay.setOnClickListener(busWay);
	// timelessWay.setOnClickListener(busWay);
	// }
}
