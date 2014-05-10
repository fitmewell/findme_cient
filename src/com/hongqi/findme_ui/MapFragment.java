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

	// ���·�߽ڵ����
	Button mBtnPre = null;// ��һ���ڵ�
	Button mBtnNext = null;// ��һ���ڵ�
	Button titleBtn;
	Spinner spinner = null;// �����˵�
	TextView simpleWay, shortestWay, timelessWay, Title;
	LinearLayout titlebar;

	int nodeIndex = -2;// �ڵ�����,������ڵ�ʱʹ��
	MKRoute route = null;// ����ݳ�/����·�����ݵı�����������ڵ�ʱʹ��
	TransitOverlay transitOverlay = null;// ���湫��·��ͼ�����ݵı�����������ڵ�ʱʹ��
	RouteOverlay routeOverlay = null;
	boolean useDefaultIcon = false;
	int searchType = -1;// ��¼���������ͣ����ּݳ�/���к͹���
	private PopupOverlay pop = null;// ��������ͼ�㣬����ڵ�ʱʹ��
	private TextView popupText = null;// ����view
	private View viewCache = null;
	private MKPlanNode stNode, edNode;
	private GeoPoint stGeoPoint, edGeoPoint;
	private MapController mapController;
	private LocationClient locationClient;
	private MyLocationListenner myLocationListenner;
	private static final String[] m = { " �ݳ� ", " ���� ", " ���� " };
	private ArrayAdapter<String> adapter;
	BMapManager bMapManager;

	private String username, friendID, city;

	// ��ͼ��أ�ʹ�ü̳�MapView��MyRouteMapViewĿ������дtouch�¼�ʵ�����ݴ���
	// ���������touch�¼���������̳У�ֱ��ʹ��MapView����
	MapView mMapView = null; // ��ͼView
	// �������
	MKSearch mSearch = null; // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��

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
		Title.setText("��ͼ");
		titleBtn.setText(" ��λ ");
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
				// ��������
				mapController.animateTo(stNode.pt);
			}
		};
		OnClickListener nodeClickListener = new OnClickListener() {
			public void onClick(View v) {
				// ���·�߽ڵ�
				nodeClick(v);
			}
		};

		// dingwei.setOnClickListener(clickListener);
		titleBtn.setOnClickListener(clickListener);
		// ���� ��������ͼ��
		createPaopao();

		// ��ͼ����¼�����
		mMapView.regMapTouchListner(new MKMapTouchListener() {

			@Override
			public void onMapClick(GeoPoint point) {
				// �ڴ˴����ͼ����¼�
				// ����pop
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
		// ��ʼ������ģ�飬ע���¼�����
		mSearch = new MKSearch();
		mSearch.init(bMapManager, new MKSearchListener() {

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
				// �����յ������壬��Ҫѡ�����ĳ����б���ַ�б�
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// �������е�ַ
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
				// ����ſɲο�MKEvent�еĶ���
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "��Ǹ��δ�ҵ����",
							Toast.LENGTH_SHORT).show();
					return;
				}

				routeOverlay = new RouteOverlay(getActivity(), mMapView);
				searchType = 0;

				// routeOverlay = new RouteOverlay(getMyFriend.this, mMapView);
				// �˴���չʾһ��������Ϊʾ��
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// �������ͼ��
				mMapView.getOverlays().clear();
				// ���·��ͼ��
				mMapView.getOverlays().add(routeOverlay);
				// ִ��ˢ��ʹ��Ч
				mMapView.refresh();
				// ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// �ƶ���ͼ�����
				mMapView.getController().animateTo(res.getStart().pt);
				// ��·�����ݱ����ȫ�ֱ���
				route = res.getPlan(0).getRoute(0);
				// ����·�߽ڵ��������ڵ����ʱʹ��
				// nodeIndex = -1;
				// mBtnPre.setVisibility(View.VISIBLE);
				// mBtnNext.setVisibility(View.VISIBLE);
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
				// �����յ������壬��Ҫѡ�����ĳ����б���ַ�б�
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// �������е�ַ
					ArrayList<MKPoiInfo> stPois = res.getAddrResult().mStartPoiList;
					ArrayList<MKPoiInfo> enPois = res.getAddrResult().mEndPoiList;
					ArrayList<MKCityListInfo> stCities = res.getAddrResult().mStartCityList;
					ArrayList<MKCityListInfo> enCities = res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "��Ǹ��δ�ҵ����",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 1;
				transitOverlay = new TransitOverlay(getActivity(), mMapView);
				// �˴���չʾһ��������Ϊʾ��
				transitOverlay.setData(res.getPlan(0));
				// �������ͼ��
				mMapView.getOverlays().clear();
				// ���·��ͼ��
				mMapView.getOverlays().add(transitOverlay);
				// ִ��ˢ��ʹ��Ч
				mMapView.refresh();
				// ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
				mMapView.getController().zoomToSpan(
						transitOverlay.getLatSpanE6(),
						transitOverlay.getLonSpanE6());
				// �ƶ���ͼ�����
				mMapView.getController().animateTo(res.getStart().pt);
				// ����·�߽ڵ��������ڵ����ʱʹ��
				nodeIndex = 0;
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
				// �����յ������壬��Ҫѡ�����ĳ����б���ַ�б�
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// �������е�ַ
					ArrayList<MKPoiInfo> stPois = res.getAddrResult().mStartPoiList;
					ArrayList<MKPoiInfo> enPois = res.getAddrResult().mEndPoiList;
					ArrayList<MKCityListInfo> stCities = res.getAddrResult().mStartCityList;
					ArrayList<MKCityListInfo> enCities = res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(getActivity(), "��Ǹ��δ�ҵ����",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 2;
				routeOverlay = new RouteOverlay(getActivity(), mMapView);
				Toast.makeText(getActivity(),
						String.valueOf(res.getPlan(0).getRoute(0)),
						Toast.LENGTH_SHORT).show();
				// �˴���չʾһ��������Ϊʾ��
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// �������ͼ��
				mMapView.getOverlays().clear();
				// ���·��ͼ��
				mMapView.getOverlays().add(routeOverlay);
				// ִ��ˢ��ʹ��Ч
				mMapView.refresh();
				// ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// �ƶ���ͼ�����
				mMapView.getController().animateTo(res.getStart().pt);
				// ��·�����ݱ����ȫ�ֱ���
				route = res.getPlan(0).getRoute(0);
				// ����·�߽ڵ��������ڵ����ʱʹ��
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
					Toast.makeText(getActivity(), "��Ǹ��δ�ҵ����",
							Toast.LENGTH_SHORT).show();
					return;
				}
				city = res.addressComponents.city.toString();
			}
		});
	}

	void SearchButtonProcess(View v, int type) {
		// ��������ڵ��·������
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
	 * �ڵ����ʾ��
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		viewCache = getActivity().getLayoutInflater().inflate(
				R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		if (searchType == 0 || searchType == 2) {
			// �ݳ�������ʹ�õ����ݽṹ��ͬ���������Ϊ�ݳ����У��ڵ����������ͬ
			if (nodeIndex < -1 || route == null
					|| nodeIndex >= route.getNumSteps())
				return;

			// ��һ���ڵ�
			if (mBtnPre.equals(v) && nodeIndex > 0) {
				// ������
				nodeIndex--;
				// �ƶ���ָ������������
				mMapView.getController().animateTo(
						route.getStep(nodeIndex).getPoint());
				// ��������
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(route.getStep(nodeIndex).getContent());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText), route
						.getStep(nodeIndex).getPoint(), 5);
			}
			// ��һ���ڵ�
			if (mBtnNext.equals(v) && nodeIndex < (route.getNumSteps() - 1)) {
				// ������
				nodeIndex++;
				// �ƶ���ָ������������
				mMapView.getController().animateTo(
						route.getStep(nodeIndex).getPoint());
				// ��������
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(route.getStep(nodeIndex).getContent());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText), route
						.getStep(nodeIndex).getPoint(), 5);
			}
		}
		if (searchType == 1) {
			// ��������ʹ�õ����ݽṹ��������ͬ����˵�������ڵ����
			if (nodeIndex < -1 || transitOverlay == null
					|| nodeIndex >= transitOverlay.getAllItem().size())
				return;

			// ��һ���ڵ�
			if (mBtnPre.equals(v) && nodeIndex > 1) {
				// ������
				nodeIndex--;
				// �ƶ���ָ������������
				mMapView.getController().animateTo(
						transitOverlay.getItem(nodeIndex).getPoint());
				// ��������
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(transitOverlay.getItem(nodeIndex).getTitle());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						transitOverlay.getItem(nodeIndex).getPoint(), 5);
			}
			// ��һ���ڵ�
			if (mBtnNext.equals(v)
					&& nodeIndex < (transitOverlay.getAllItem().size() - 2)) {
				// ������
				nodeIndex++;
				// �ƶ���ָ������������
				mMapView.getController().animateTo(
						transitOverlay.getItem(nodeIndex).getPoint());
				// ��������
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(transitOverlay.getItem(nodeIndex).getTitle());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						transitOverlay.getItem(nodeIndex).getPoint(), 5);
			}
		}

	}

	/**
	 * ������������ͼ��
	 */
	public void createPaopao() {

		// ���ݵ����Ӧ�ص�
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	/**
	 * ��ת����·��Activity
	 * 
	 * public void intentToActivity(){ //��ת������·����ʾdemo Intent intent = new
	 * Intent(this, CustomRouteOverlayDemo.class); startActivity(intent); }
	 */
	/**
	 * �л�·��ͼ�꣬ˢ�µ�ͼʹ����Ч ע�⣺ ���յ�ͼ��ʹ�����Ķ���.
	 * 
	 * protected void changeRouteIcon() { // Button btn =
	 * (Button)findViewById(R.id.customicon); if ( routeOverlay == null &&
	 * transitOverlay == null){ return ; } if ( useDefaultIcon ){ if (
	 * routeOverlay != null){ routeOverlay.setStMarker(null);
	 * routeOverlay.setEnMarker(null); } if ( transitOverlay != null){
	 * transitOverlay.setStMarker(null); transitOverlay.setEnMarker(null); }
	 * btn.setText("�Զ������յ�ͼ��"); Toast.makeText(this, "��ʹ��ϵͳ���յ�ͼ��",
	 * Toast.LENGTH_SHORT).show(); } else{ if ( routeOverlay != null){
	 * routeOverlay.setStMarker(getResources().getDrawable(R.drawable.icon_st));
	 * routeOverlay.setEnMarker(getResources().getDrawable(R.drawable.icon_en));
	 * } if ( transitOverlay != null){
	 * transitOverlay.setStMarker(getResources().
	 * getDrawable(R.drawable.icon_st));
	 * transitOverlay.setEnMarker(getResources(
	 * ).getDrawable(R.drawable.icon_en)); } btn.setText("ϵͳ���յ�ͼ��");
	 * Toast.makeText(this, "��ʹ���Զ������յ�ͼ��", Toast.LENGTH_SHORT).show(); }
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
