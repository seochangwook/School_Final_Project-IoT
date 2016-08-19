package com.example.seo.project;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seo on 2016-03-18.
 */
public class GoogleMap_Activity extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    DB_Helper db_helper; //핼퍼클래스 정의.//
    SQLiteDatabase db; //데이터베이스 기능을 사용하기 위한 SQLite 사용.//
    Cursor cursor; //탐색을 할 커서정의.//
    String sql;

    GoogleMap mMap; //구글맵 객체 사용.//
    Geocoder coder; //구글에서 제공하는 지오코딩 클래스 선언.구글맵 클래스는 맵을 나타내주는 것이고 실질적인 좌표정보는 지오코더를 이용한다.//

    private PolylineOptions polylineOptions;
    private ArrayList<LatLng> arrayPoints;

    double my_latitude = 0.0; //위도값.//
    double my_longitude = 0.0; //경도값.//
    double home_latitude; //위도값.//
    double home_longitude; //경도값.//

    String location = null;
    String user_id;

    int changed_check = 0; //처음 위치를 찾았고 안바뀌게 할려고 만든 변수.//

    private static final double EARTH_RADIUS = 6378100.0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.googlemap_activity);

        coder = new Geocoder(this); //지오코더 객체 생성.//
        db_helper = new DB_Helper(this);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("KEY_USER_ID");

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap(); //구글맵 초기화.//

        Toast.makeText(getApplicationContext(), "The position of check. Please wait..", Toast.LENGTH_SHORT).show();

        setMyPosition(); //현재 위치를 표시.//
        setMyHome_marker(); //현재 나의 집 위치를 마커로 표시.//

        //폴리라인을 적용하여 집까지의 가는 길을 이미지로 경로 표시 가능.//
        MapsInitializer.initialize(getApplicationContext());

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void setMyPosition() {
        LocationManager manager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        LocationListener locationlistener = new LocationListener() //위치정보리스너.//
                //여러 콜벡메소드를 이용하여서 위치정보에 대해서 정보를 계속 업데이트 해준다.//
                //위치가 업데이트 되면 호출되는 리스너 콜벡함수들..//
        {

            @Override
            public void onLocationChanged(Location location) //새로운 위치가 발견되면 호출된다.//
            {
                if (changed_check == 0) {
                    // TODO Auto-generated method stub
                    //현재 위치의 좌표값(위도, 경도)을 얻어온다.//
                    my_latitude = location.getLatitude();
                    my_longitude = location.getLongitude();

                    LatLng startingPoint = new LatLng(my_latitude, my_longitude);

                    Log.i("My Position :", my_latitude + "," + my_longitude);

                    //줌값은 작아질수록 지구가 보인다.//
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, 12)); //해당 위치로 카메라 이동.16은 줌 사이즈//

                    Toast.makeText(getApplicationContext(), "This location search is completed.", Toast.LENGTH_SHORT).show();

                    mMap.addCircle(new CircleOptions().center(new LatLng(my_latitude, my_longitude)).radius(350).strokeColor(Color.RED).fillColor(Color.BLUE));

                    changed_check = 1; //다시 셋팅이 안되도록 변경.//

                    arrayPoints = new ArrayList<LatLng>();
                } else {

                }
            }

            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
                Log.i("My Position1 :", my_latitude + "," + my_longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                Log.i("My Position2 :", my_latitude + "," + my_longitude);
            }
        };

        //요청을 두개할시 GPS,Network기반으로 동시에 찾는다.//
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //위치정보 업데이트를 요청한다 --> 마지막에 locationlistener을 주어 리스너가 위치가 변경될시 마다 업데이트 콜벡 메소드를 호출.//
        //0,0은 자주 업데이트를 한다는 의미이다. GPS_PROVIDER이므로 GPS기반으로 위치정보를 가져온다는 의미.//
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationlistener);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationlistener);
    }

    public void setMyHome_marker() {
        location = get_Enroll_location(user_id); //사용자가 등록한 주소의 정보가 적용되면 된다.//

        List<Address> list = null; //주소를 저장할 List JCF설정.//

        try {
            list = coder.getFromLocationName(location, 10); //getFromLocationName이므로 역지오코딩,
            //주소나 지명을 가지고 위도,경도등 위치정보를 아는것. 지오코딩//

            Log.i("LOCATION :","LOCATION INFO : "+list.get(0));

        } catch (IOException ioe) {
            Toast.makeText(getApplicationContext(), "Location Search error", Toast.LENGTH_SHORT).show();
        }

        if (list != null) {
            //주소를 가지고 (위도,경도)를 뽑아낼 수 있는 지오코딩을 이용하여 (getFromLocationName(주소, 최대조사수)) SQLiteDatabase를 이용하면 원하는 위치를 지도로
            //계속 알아낼 수 있다.//
            home_latitude = list.get(0).getLatitude(); //위도값을 가져온다.//
            home_longitude = list.get(0).getLongitude(); //경도값 가져오기.//

            MarkerOptions marker = new MarkerOptions(); //마커를 위해서 객체생성.//

            marker.position(new LatLng(home_latitude, home_longitude));
            marker.title("My Home");
            marker.draggable(false);
            //마커의 아이콘을 사용자 임의 이미지로 지정.//
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.home_marker));

            mMap.addMarker(marker); //지도에 마커를 추가.//
        }
    }

    private String get_Enroll_location(String user_id) {
        String location_name = null;

        db = db_helper.getWritableDatabase(); //데이터베이스를 쓰기모드로 개방.//

        sql = "SELECT user_address FROM home_info WHERE user_id = '" + user_id + "';"; //전체출력 SQL.//

        cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            location_name = cursor.getString(cursor.getColumnIndex("user_address"));

            //Toast.makeText(getActivity(), "check 1 : "+check_value, Toast.LENGTH_SHORT).show();
        }

        return location_name;
    }

    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    public void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(false);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //add marker
        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);
        mMap.addMarker(marker);

        double loh = latLng.latitude;
        double lo2 = latLng.longitude;

        Log.i("LOCATION P :", "LOCATION position : " + loh+"/"+lo2);

        // 맵셋팅
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        arrayPoints.add(latLng);
        polylineOptions.addAll(arrayPoints);
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        arrayPoints.clear();


        setMyHome_marker();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GoogleMap_ Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.seo.project/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GoogleMap_ Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.seo.project/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
