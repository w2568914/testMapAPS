package com.example.testmapaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {
    //地图样式参数
    private static final int WRITE_COARSE_LOCATION_REQUEST_CODE = 0;
    MapView mMapView=null;
    AMap aMap=null;
    MyLocationStyle myLocationStyle=null;
    //定位需要的声明
    //定位发起端
    private AMapLocationClient mLocationClient = null;
    //定位参数
    private AMapLocationClientOption mLocationOption = null;
    //定位监听器
    private LocationSource.OnLocationChangedListener mListener = null;
    //定位标志
    private boolean isFirstLoc=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图组件
        init();
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
            CameraUpdate cu = CameraUpdateFactory.zoomTo(20);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
            // 创建一个更改地图倾斜度的CameraUpdate
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            // 改变地图的倾斜度
            aMap.moveCamera(tiltUpdate);
        }
        // 开启定位
        initLoc();
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
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
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
}

