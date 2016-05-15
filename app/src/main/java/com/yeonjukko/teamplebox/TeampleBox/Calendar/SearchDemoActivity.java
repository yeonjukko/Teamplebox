package com.yeonjukko.teamplebox.TeampleBox.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.yeonjukko.teamplebox.R;
import com.yeonjukko.teamplebox.TeampleBox.Calendar.search.Item;
import com.yeonjukko.teamplebox.TeampleBox.Calendar.search.OnFinishSearchListener;
import com.yeonjukko.teamplebox.TeampleBox.Calendar.search.Searcher;
import com.yeonjukko.teamplebox.libs.AndroidDBManager;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPoint.GeoCoordinate;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class SearchDemoActivity extends FragmentActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = "SearchDemoActivity";
    protected static final String TAG = "SearchGPSActivity";

    private MapView mMapView;
    private EditText mEditTextQuery;
    private Button mButtonSearch;
    private HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();

    private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    private MapPOIItem mDefaultMarker;
    private double latitude = 0;
    private double longitude = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.demo_search);
        buildGoogleApiClient();
        createLocationRequest();


        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
        //mMapView.setDaumMapApiKey("a654487408965838f8df718ced6c1047");
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());


        mEditTextQuery = (EditText) findViewById(R.id.editTextQuery); // 검색창

        mButtonSearch = (Button) findViewById(R.id.buttonSearch); // 검색버튼
        mButtonSearch.setOnClickListener(new OnClickListener() { // 검색버튼 클릭 이벤트 리스너
            @Override
            public void onClick(View v) {
                String query = mEditTextQuery.getText().toString();
                if (query == null || query.length() == 0) {
                    showToast("검색어를 입력하세요.");
                    return;
                }
                hideSoftKeyboard(); // 키보드 숨김
                //GeoCoordinate geoCoordinate = mMapView.getMapCenterPoint().getMapPointGeoCoord();
                //double latitude = geoCoordinate.latitude; // 위도
                //double longitude = geoCoordinate.longitude; // 경도
                String test = latitude + "" + longitude;
                Log.d("test", "Search버튼 후" + test);
                //Toast.makeText(SearchDemoActivity.this, "Search버튼 후"+test+"" ,Toast.LENGTH_SHORT).show();

                int radius = 1000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
                int page = 1; // 페이지 번호 (1 ~ 3). 한페이지에 15개
                String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;
                //String apikey = "a654487408965838f8df718ced6c1047";
                Searcher searcher = new Searcher(); // net.daum.android.map.openapi.search.Searcher
                searcher.searchKeyword(SearchDemoActivity.this, query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
                    @Override
                    public void onSuccess(List<Item> itemList) {
                        mMapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                        showResult(itemList); // 검색 결과 보여줌
                    }

                    @Override
                    public void onFail() {
                        showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
                    }
                });
            }
        });
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        //onConnect 다음 실행
        Log.d("service", "Attend onLocationChanged");
        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 2, true);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        stopLocationUpdates();
        Toast.makeText(this, "onLocationChanged" + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_SHORT).show();

    }

    protected void createLocationRequest() {
        Log.d("service", "Attend createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d("service", "Attend startLocationUpdates");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Log.d("service", "Attend onConnected");
        startLocationUpdates();


    }


    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            Item item = mTagItemMap.get(poiItem.getTag());
            if (item == null) return null;
            ImageView imageViewBadge = (ImageView) mCalloutBalloon.findViewById(R.id.badge);
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.title);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            imageViewBadge.setImageDrawable(createDrawableFromUrl(item.imageUrl));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextQuery.getWindowToken(), 0);
    }

    public void onMapViewInitialized(MapView mapView) {
        Log.i(LOG_TAG, "MapView had loaded. Now, MapView APIs could be called safely");

        //mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.537229, 127.005515), 2, true);

        Searcher searcher = new Searcher();
        String query = mEditTextQuery.getText().toString();
        //double latitude = 37.537229;
        //double longitude = 127.005515;
        int radius = 100000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
        int page = 1;
        String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;
        //String apikey = "a654487408965838f8df718ced6c1047";

        searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
            @Override
            public void onSuccess(final List<Item> itemList) {
                showResult(itemList);
            }

            @Override
            public void onFail() {
                showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
            }
        });
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SearchDemoActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResult(List<Item> itemList) {
        MapPointBounds mapPointBounds = new MapPointBounds();

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.title);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.drawable.map_pin_red);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);
        }

        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));

        MapPOIItem[] poiItems = mMapView.getPOIItems();
        if (poiItems.length > 0) {
            mMapView.selectPOIItem(poiItems[0], false);
        }
    }

    private Drawable createDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object fetch(String address) throws MalformedURLException, IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        Item item = mTagItemMap.get(mapPOIItem.getTag());
        StringBuilder sb = new StringBuilder();
        sb.append("title=").append(item.title).append("\n");
        sb.append("imageUrl=").append(item.imageUrl).append("\n");
        sb.append("address=").append(item.address).append("\n");
        sb.append("newAddress=").append(item.newAddress).append("\n");
        sb.append("zipcode=").append(item.zipcode).append("\n");
        sb.append("phone=").append(item.phone).append("\n");
        sb.append("category=").append(item.category).append("\n");
        sb.append("longitude=").append(item.longitude).append("\n");
        sb.append("latitude=").append(item.latitude).append("\n");
        sb.append("distance=").append(item.distance).append("\n");
        sb.append("direction=").append(item.direction).append("\n");
        Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    @Deprecated
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewSingleTapped(final MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        MapPoint.PlainCoordinate mapPointScreenLocation = mapPoint.getMapPointScreenLocation();
        mDefaultMarker = new MapPOIItem();
        mDefaultMarker.setTag(0);
        mDefaultMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude));
        //mDefaultMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mDefaultMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        MapReverseGeoCoder.ReverseGeoCodingResultListener reverseListener = new MapReverseGeoCoder.ReverseGeoCodingResultListener() {
            @Override
            public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String addressString) {
                Log.d("test", addressString + "name");
                mDefaultMarker.setItemName(addressString);
                mapView.addPOIItem(mDefaultMarker);
                mapView.selectPOIItem(mDefaultMarker, true);
            }

            @Override
            public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                Log.d("test", "failed");
            }
        };


        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY, mapPoint, reverseListener, SearchDemoActivity.this);
        reverseGeoCoder.startFindingAddress();

        //mTapTextView.setText("single tapped, point=" + String.format("lat/lng: (%f,%f) x/y: (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude, mapPointScreenLocation.x, mapPointScreenLocation.y));

        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), false);
        Log.i(LOG_TAG, String.format("MapView onMapViewSingleTapped (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
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
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
    }


}
