package com.example.testmapaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.RouteOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.tencent.bugly.Bugly;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, RouteSearch.OnRouteSearchListener {

    //地图样式参数
    private static final int WRITE_COARSE_LOCATION_REQUEST_CODE = 0;
    MapView mMapView=null;
    AMap aMap=null;
    MyLocationStyle myLocationStyle=null;

    //定位需要的声明
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private LocationSource.OnLocationChangedListener mListener = null;
    private boolean isFirstLoc=true;

    //路线规划参数
    private com.amap.api.services.core.LatLonPoint user_loc=null;
    private com.amap.api.services.core.LatLonPoint goal_loc=null;
    private RouteSearch.FromAndTo fromAndTo=null;
    private RouteSearch routeSearch=null;

    //用户界面元素
    private Button Select_btn_bom=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //buggly初始化
        Bugly.init(getApplicationContext(), "3bfc25f272", true);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图组件
        init();
        //设置自定义弹窗弹窗
        //aMap.setInfoWindowAdapter(this);
    }

    // 初始化AMap对象
    void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            //设置定位点样式
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            aMap.setMyLocationStyle(myLocationStyle);
            //设置显示定位按钮 并且可以点击
            UiSettings settings = aMap.getUiSettings();
            //设置了定位的监听
            aMap.setLocationSource(this);
            // 是否显示定位按钮
            settings.setMyLocationButtonEnabled(true);
            //显示定位层并且可以触发定位,默认是flase
            aMap.setMyLocationEnabled(true);
            // 创建一个设置放大级别的CameraUpdate
            CameraUpdate cu = CameraUpdateFactory.zoomTo(14);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
            // 创建一个更改地图倾斜度的CameraUpdate
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            // 改变地图的倾斜度
            aMap.moveCamera(tiltUpdate);
            //设置点击事件
            aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return false;
                }
            });
        }
        // 开启定位
        initLoc();

        //todo 预设目的地
        this.goal_loc= new com.amap.api.services.core.LatLonPoint(30.3126719672,120.3566998243);

        Select_btn_bom=findViewById(R.id.select_button_bottom);
        Select_btn_bom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* //todo 打开aps界面
                final Intent aps_intent=new Intent(MainActivity.this,apsActivity.class);
                startActivity(aps_intent);*/
                //todo 测试绘制路线
                if(!startRouteSreach(user_loc,goal_loc,1)){
                    Toast.makeText(MainActivity.this,"进行驾车路线规划失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //进行路线规划
    private boolean startRouteSreach(com.amap.api.services.core.LatLonPoint start, com.amap.api.services.core.LatLonPoint end, int Code){
        if (routeSearch == null) {
            routeSearch = new RouteSearch(this);
        }
        if (start != null && end != null) {
            fromAndTo = new RouteSearch.FromAndTo(start, end);
        }
        routeSearch.setRouteSearchListener(this);
        //todo 分类规划
        switch(Code){
            case 1:
                RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(
                        fromAndTo, RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST_AVOID_CONGESTION, null, null, "");
                routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
                break;
            default:
                return false;
        }

        return true;
    }

    //路线绘制
    private void drawRouteToMap(RouteResult result,int code){
        //记录起点终点
        LatLng routeStart=new LatLng(result.getStartPos().getLatitude(),result.getStartPos().getLongitude());
        LatLng routeEnd=new LatLng(result.getTargetPos().getLatitude(),result.getTargetPos().getLongitude());
        //创建存储坐标点的集合
        List<LatLng> latLngs = new ArrayList<>();

        //分类记录路径
        switch (code) {
            case 1:
                //todo 路径并不唯一
                DriveRouteResult driveRouteResult=(DriveRouteResult)result;
                List<DrivePath> paths=driveRouteResult.getPaths();
                for(DrivePath mDrivePath:paths) {
                    for(DriveStep mDriveStep:mDrivePath.getSteps()){
                        for(LatLonPoint mLatLonPoint:mDriveStep.getPolyline()){
                            latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                        }
                    }
                }
                break;
            default:
                return;
        }

        //先清除一下,避免重复显示
        aMap.clear();

        //绘制起始位置和目的地marker
        aMap.addMarker(new MarkerOptions()
                .icon(null)
                .position(routeStart));
        aMap.addMarker(new MarkerOptions()
                .icon(null)
                .position(routeEnd));

        //绘制规划路径路线
        aMap.addPolyline(new PolylineOptions()
                        //路线坐标点的集合
                        .addAll(latLngs)
                        //线的宽度
                        .width(30)
                        .color(getResources().getColor(R.color.design_default_color_primary_dark)));//设置画线的颜色

                //显示完整包含所有marker地图路线
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < latLngs.size(); i++) {
            builder.include(latLngs.get(i));
        }
        //显示全部marker,第二个参数是四周留空宽度
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));

    }

    //初始化定位
    private void initLoc() {
    //  SDK在Android 6.0以上的版本需要进行运行检测的动态权限如下：
    //    Manifest.permission.ACCESS_COARSE_LOCATION,
    //    Manifest.permission.ACCESS_FINE_LOCATION,
    //    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    //    Manifest.permission.READ_EXTERNAL_STORAGE,
    //    Manifest.permission.READ_PHONE_STATE

        //动态检查定位及内存权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    //启动定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }
    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }
    //定位监听回调
    //todo 标志位可能导致定位不准
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();  // 地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();  // 国家信息
                aMapLocation.getProvince();  // 省信息
                aMapLocation.getCity();  // 城市信息
                aMapLocation.getDistrict();  // 城区信息
                aMapLocation.getStreet();  // 街道信息
                aMapLocation.getStreetNum();  // 街道门牌号信息
                aMapLocation.getCityCode();  // 城市编码
                aMapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                    //todo 记录用户当前位置
                    this.user_loc= new com.amap.api.services.core.LatLonPoint(aMapLocation.getLatitude(),
                            aMapLocation.getLongitude());
                    Toast.makeText(MainActivity.this,this.user_loc.toString(),Toast.LENGTH_SHORT).show();
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(),
                            aMapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    isFirstLoc = false;
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    //公交路线规划
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (i == AMapException.CODE_AMAP_SUCCESS) {

        }
    }
    //驾车路线规划
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        // 清理地图上的所有覆盖物
        aMap.clear();
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            //获取路径结果
            DrivePath drivePath=driveRouteResult.getPaths().get(0);
            //策略
            String strategy=drivePath.getStrategy();
            //信号灯数量
            int clights=drivePath.getTotalTrafficlights();
            //距离
            float distance=drivePath.getDistance()/1000;
            //时间
            long duration=drivePath.getDuration()/60;
            //todo 显示信息
            Log.e("test","策略："+strategy+
                    "\n交通信号灯数量/个"+clights+
                    "\n距离/公里"+distance+
                    "\n时间/分"+duration);
            //todo 调用函数进行路线绘制
            drawRouteToMap(driveRouteResult,1);
        }
        else {
            Log.e("test","路线规划失败");
        }
    }
    //步行路线规划
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (i == AMapException.CODE_AMAP_SUCCESS) {
              /*  //在地图上绘制路径：
                MyWalkRouteOverlay walkRouteOverlay = new MyWalkRouteOverlay(getBaseContext(), mMapView.getMap(), walkRouteResult.getPaths().get(0), walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
                walkRouteOverlay.setNodeIconVisibility(false);
                walkRouteOverlay.removeFromMap();
                walkRouteOverlay.addToMap();//将Overlay添加到地图上显示
                walkRouteOverlay.zoomToSpan();//调整地图能看到起点和终点
                lastWalkRouteOverlay = walkRouteOverlay;*/

        }
    }
    //骑行路线规划
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (i == AMapException.CODE_AMAP_SUCCESS) {

        }
    }
}

