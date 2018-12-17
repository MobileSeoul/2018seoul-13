package com.rollcake.indi.indiplace.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rollcake.indi.indiplace.R;
import com.rollcake.indi.indiplace.utils.Logs;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.jetbrains.annotations.Nullable;

public class ConcertInfoActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener {

    private static final String TAG = ConcertInfoActivity.class.getSimpleName();

    private String placeName = null;
    private Double lat, loc;


    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    private TextView startTimeText, placeText;
    private TextView seletedPlace, title;
    private MapView mapView;
    private kr.go.seoul.airquality.AirQualityTypeMini airQualityTypeMini;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concert_info);
        startTimeText = findViewById(R.id.startTime);
        placeText = findViewById(R.id.endTime);
        seletedPlace = findViewById(R.id.concertTitle);
        title = findViewById(R.id.artitst_title);
        airQualityTypeMini = findViewById(R.id.air_layout);

        Intent intent = getIntent();
        loc = intent.getDoubleExtra("loc", 0);
        lat = intent.getDoubleExtra("lat", 0);
        placeName = intent.getStringExtra("title");
        String start = intent.getStringExtra("start");
        String end = intent.getStringExtra("end");
        String location = intent.getStringExtra("place");
        String placecontent = intent.getStringExtra("placecontent");


        startTimeText.setText(start.substring(0, 13) + "~" + end.substring(11, 13));
        placeText.setText(location);
        title.setText("콘서트 정보 " + placeName);
        seletedPlace.setText(placecontent);

        airQualityTypeMini.setOpenAPIKey(getString(R.string.seoul_key));
        mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        mapView.setMapViewEventListener(this); // this에 MapView.MapViewEventListener 구현.
        mapView.setPOIItemEventListener(this);

        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, loc), true);

        mapView.setZoomLevel(2, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(placeName);
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, loc));
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
        marker.setCustomImageResourceId(R.drawable.ballad); // 마커 이미지.
        marker.setCustomImageAutoscale(true); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
        marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

        mapView.addPOIItem(marker);

        MapCircle circle1 = new MapCircle(
                MapPoint.mapPointWithGeoCoord(lat, loc), // center
                100, // radius
                Color.argb(128, 255, 255, 0), // strokeColor
                Color.argb(128, 255, 255, 0) // fillColor
        );
        circle1.setTag(1234);
        mapView.addCircle(circle1);


        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


}
