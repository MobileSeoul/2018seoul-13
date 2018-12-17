package com.rollcake.indi.indiplace.activity;

import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.rollcake.indi.indiplace.AppApplication;
import com.rollcake.indi.indiplace.R;
import com.rollcake.indi.indiplace.activity.view.ConcertPlaceInfo;
import com.rollcake.indi.indiplace.utils.AppkeyManager;
import com.rollcake.indi.indiplace.utils.Logs;
import com.rollcake.indi.indiplace.utils.ServerNetworking;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.rollcake.indi.indiplace.AppApplication.ARTIST_ID;
import static com.rollcake.indi.indiplace.AppApplication.DOMAIN;
import static com.rollcake.indi.indiplace.AppApplication.GENER;

public class InsertConcertActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, DatePickerDialog.OnDateSetListener {

    private static final String TAG = InsertConcertActivity.class.getSimpleName();

    private String placeName = null;
    private Double lat, loc;

    private int year;
    private int month;
    private int dayOfMonth;

    private String gu;
    private String content;

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
        seletedPlace.setText("장소 설정 : " + mapPOIItem.getItemName());
        placeName = mapPOIItem.getItemName();
        loc = mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude;
        lat = mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude;
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

    private Button saveBtn, searchBtn, gressSearchBtn;
    private Spinner locationSpinner, startTimeSpinner, endTimeSpinner;
    private TextView seletedPlace;
    private EditText placecontent;
    private Button dateSelectBtn;
    private MapView mapView;

    private ArrayList<String> locationList, timeList;
    private ArrayAdapter<String> locationAdapter, timeAdapter;
    private int page = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inser_concert);
        searchBtn = findViewById(R.id.searchBtn);
        gressSearchBtn = findViewById(R.id.gressSearchBtn);
        saveBtn = findViewById(R.id.saveBtn);
        locationSpinner = findViewById(R.id.search);
        startTimeSpinner = findViewById(R.id.startTimeSpinner);
        endTimeSpinner = findViewById(R.id.endTimeSpinner);
        seletedPlace = findViewById(R.id.seleted_place);
        dateSelectBtn = findViewById(R.id.date_selectBtn);
        placecontent = findViewById(R.id.placecontent);

        locationList = new ArrayList<>();
        timeList = new ArrayList<>();

        addLocation(locationList);
        addTimeList(timeList);

        locationAdapter = new ArrayAdapter<String>(this, R.layout.simple_1line, locationList);
        timeAdapter = new ArrayAdapter<String>(this, R.layout.simple_1line, timeList);

        locationSpinner.setAdapter(locationAdapter);
        startTimeSpinner.setAdapter(timeAdapter);
        endTimeSpinner.setAdapter(timeAdapter);

        mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        mapView.setMapViewEventListener(this); // this에 MapView.MapViewEventListener 구현.
        mapView.setPOIItemEventListener(this);

        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);

        mapView.setZoomLevel(7, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);



        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dateSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog();
            }
        });

        Calendar now = Calendar.getInstance();
        dateSelectBtn.setText("날짜 : " + now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH));

        this.year = now.get(Calendar.YEAR);
        this.month = now.get(Calendar.MONTH);
        this.dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        gressSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.removeAllPOIItems();
                searchBtn.setEnabled(false);
                gressSearchBtn.setEnabled(false);
                String url = "/grass";

                try {
                    ServerNetworking.sendToMobileServer(InsertConcertActivity.this, Request.Method.GET, AppApplication.DOMAIN + url + "?page=" + page, null, null, new ServerNetworking.getResult() {
                        @Override
                        public void getResultText(String text) {
                            Logs.e(TAG, text);

                            try {
                                JSONObject resultObj = new JSONObject(text);
                                if (resultObj.getBoolean("key")) {
                                    ArrayList<ConcertPlaceInfo> info = new ArrayList();

                                    JSONArray arrayList = resultObj.getJSONArray("message");

                                    if (arrayList.length() == 0) {
                                        Toast.makeText(InsertConcertActivity.this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Logs.e(TAG, "arrayList.length() : " + arrayList.toString());

                                        for (int i = 0; i < arrayList.length(); i++) {
                                            String name = arrayList.getJSONObject(i).getString("name");
//                                            String content = arrayList.getJSONObject(i).getJSONObject("content").toString();
//                                            String image = arrayList.getJSONObject(i).getJSONObject("image").toString();
//                                            String address = arrayList.getJSONObject(i).getJSONObject("address").toString();
//                                            String gu = arrayList.getJSONObject(i).getJSONObject("gu").toString();
                                            double lot = arrayList.getJSONObject(i).getDouble("lot");
                                            double lat = arrayList.getJSONObject(i).getDouble("lat");
//                                            info.add(new ConcertPlaceInfo(name, content, image, address, gu, lat, lot));

                                            MapPOIItem marker = new MapPOIItem();
                                            marker.setItemName(name);
                                            marker.setTag(0);
                                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lot));
                                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//                                            marker.setCustomImageAutoscale(true); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
//                                            marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

                                            mapView.addPOIItem(marker);
                                        }
                                        page++;
                                        if(page > 90)
                                            page = 1;
                                        gressSearchBtn.setText("다음 잔디");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            searchBtn.setEnabled(true);
                            gressSearchBtn.setEnabled(true);
                        }

                        @Override
                        public void fail(String Error) {
                            searchBtn.setEnabled(true);
                            gressSearchBtn.setEnabled(true);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.removeAllPOIItems();
                searchBtn.setEnabled(false);
                gressSearchBtn.setEnabled(false);
                String url = "/place";

                try {
                    ServerNetworking.sendToMobileServer(InsertConcertActivity.this, Request.Method.GET, AppApplication.DOMAIN + url + "?keyword=" + locationList.get(locationSpinner.getSelectedItemPosition()), null, null, new ServerNetworking.getResult() {
                        @Override
                        public void getResultText(String text) {
                            try {
                                JSONObject resultObj = new JSONObject(text);
                                if (resultObj.getBoolean("key")) {
                                    ArrayList<ConcertPlaceInfo> info = new ArrayList();

                                    JSONArray arrayList = resultObj.getJSONArray("message");

                                    if (arrayList.length() == 0) {
                                        Toast.makeText(InsertConcertActivity.this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Logs.e(TAG, "arrayList.length() : " + arrayList.getJSONObject(0).getDouble("lot"));

                                        for (int i = 0; i < arrayList.length(); i++) {
                                            String name = arrayList.getJSONObject(i).getString("name");
//                                            String content = arrayList.getJSONObject(i).getJSONObject("content").toString();
//                                            String image = arrayList.getJSONObject(i).getJSONObject("image").toString();
//                                            String address = arrayList.getJSONObject(i).getJSONObject("address").toString();
//                                            String gu = arrayList.getJSONObject(i).getJSONObject("gu").toString();
                                            double lot = arrayList.getJSONObject(i).getDouble("lot");
                                            double lat = arrayList.getJSONObject(i).getDouble("lat");
//                                            info.add(new ConcertPlaceInfo(name, content, image, address, gu, lat, lot));

                                            MapPOIItem marker = new MapPOIItem();
                                            marker.setItemName(name);
                                            marker.setTag(0);
                                            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lot));
                                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//                                            marker.setCustomImageAutoscale(true); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
//                                            marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

                                            mapView.addPOIItem(marker);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            searchBtn.setEnabled(true);
                            gressSearchBtn.setEnabled(true);
                        }

                        @Override
                        public void fail(String Error) {
                            searchBtn.setEnabled(true);
                            gressSearchBtn.setEnabled(true);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeName == null) {
                    Toast.makeText(InsertConcertActivity.this, "장소 정보가 없습니다 장소를 선택해주세요", Toast.LENGTH_LONG).show();
                } else if (placecontent.getText().toString().length() == 0) {
                    Toast.makeText(InsertConcertActivity.this, "소개글이 없어요 작성 부탁드려요", Toast.LENGTH_LONG).show();
                } else if (timeCheck()) {
                    showArtistdialog("한번 등록 후에는 수정 및 삭제가 불가능 합니다 공연을 등록하시겠습니까?");
                } else {
                    Toast.makeText(InsertConcertActivity.this, "시간이 맞지 않습니다. 다시 등록해주세요", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean timeCheck() {
        if (startTimeSpinner.getSelectedItemPosition() < endTimeSpinner.getSelectedItemPosition()) {
            return true;
        } else {
            return false;
        }
    }

    private void addTimeList(ArrayList<String> timeList) {
        timeList.add("08");
        timeList.add("09");
        timeList.add("10");
        timeList.add("11");
        timeList.add("12");
        timeList.add("13");
        timeList.add("14");
        timeList.add("15");
        timeList.add("16");
        timeList.add("17");
        timeList.add("18");
        timeList.add("19");
        timeList.add("20");
        timeList.add("21");
        timeList.add("22");
    }

    protected void addLocation(ArrayList<String> locationList) {
        locationList.add("강남구");
        locationList.add("강동구");
        locationList.add("강북구");
        locationList.add("강서구");
        locationList.add("관악구");
        locationList.add("광진구");
        locationList.add("구로구");
        locationList.add("금천구");
        locationList.add("노원구");
        locationList.add("도봉구");
        locationList.add("동대문구");
        locationList.add("동작구");
        locationList.add("마포구");
        locationList.add("서대문구");
        locationList.add("서초구");
        locationList.add("성동구");
        locationList.add("성북구");
        locationList.add("송파구");
        locationList.add("양천구");
        locationList.add("영등포구");
        locationList.add("용산구");
        locationList.add("은평구");
        locationList.add("종로구");
        locationList.add("중구");
        locationList.add("중랑구");
    }

    private void showArtistdialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InsertConcertActivity.this);
        builder.setTitle("인디플레이스");
        builder.setMessage(message);
        builder.setPositiveButton("네 확인했습니다", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String[] key = {"artistId", "genre", "startTime", "endTime", "location", "lat", "lot", "place"};
                String[] value = {AppkeyManager.Companion.getKey(InsertConcertActivity.this, ARTIST_ID, "0"),
                        String.valueOf(getGenreToInt(AppkeyManager.Companion.getKey(InsertConcertActivity.this, GENER, ""))),
                        getTimeToString(startTimeSpinner), getTimeToString(endTimeSpinner), locationList.get(locationSpinner.getSelectedItemPosition()), lat.toString(), loc.toString()
                        , placecontent.getText().toString()
                };

                try {
                    ServerNetworking.sendToMobileServer(InsertConcertActivity.this, Request.Method.POST, DOMAIN + "/performance", key, value, new ServerNetworking.getResult() {
                        @Override
                        public void getResultText(String text) {
                            Logs.e(TAG, text);
                            try {
                                JSONObject resultObj = new JSONObject(text);
                                if (resultObj.getBoolean("key")) {
                                    Toast.makeText(InsertConcertActivity.this, "콘서트 등록이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } catch (JSONException exception) {
                                Logs.e(TAG, exception.getMessage());
                                Toast.makeText(InsertConcertActivity.this, "서버 에러입니다. 나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        }

                        @Override
                        public void fail(String Error) {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getGenreToInt(String key) {
        if (key.equals("기악")) {
            return 1;
        } else if (key.equals("음악")) {
            return 2;
        } else if (key.equals("퍼포먼스")) {
            return 3;
        } else if (key.equals("전통")) {
            return 4;
        } else {
            return 0;
        }
    }


    private String getTimeToString(Spinner spinner) {
        return year + "-" + month + "-" + dayOfMonth + " " + timeList.get(spinner.getSelectedItemPosition()) + ":00:00";
    }

    private void showDateDialog() {
        Calendar now = Calendar.getInstance();
        Calendar after = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setMinDate(now);
        after.add(Calendar.MONTH, 1);
        dpd.setMaxDate(after);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        dateSelectBtn.setText("날짜 : " + year + "-" + monthOfYear + 1 + "-" + dayOfMonth);
        this.year = year;
        this.month = monthOfYear + 1;
        this.dayOfMonth = dayOfMonth;
    }
}
