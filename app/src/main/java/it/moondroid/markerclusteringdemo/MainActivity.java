package it.moondroid.markerclusteringdemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {

    private GoogleMap mMap;
    private ClusterManager<MyItem> mClusterManager;

    /*
            * This matches only once in whole input,
            * so Scanner.next returns whole InputStream as a String.
    * http://stackoverflow.com/a/5445161/2183804
            */
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            startDemo();
        }
    }

    private void startDemo(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new ClusterManager<MyItem>(this, mMap);
        mMap.setOnCameraChangeListener(mClusterManager);

        try {
            readItems();
            setupListeners();

        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.items);
        List<MyItem> items = readItems(inputStream);
        mClusterManager.addItems(items);
    }

    private List<MyItem> readItems(InputStream inputStream) throws JSONException {
        List<MyItem> items = new ArrayList<>();
        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            items.add(new MyItem(lat, lng));
        }
        return items;
    }

    private void setupListeners(){
        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> myItemCluster) {
                ArrayList<MyItem> items =
                        new ArrayList<MyItem>(myItemCluster.getItems());

                Toast.makeText(MainActivity.this, myItemCluster.getSize()+" items", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                LatLng position = myItem.getPosition();
                Toast.makeText(MainActivity.this, position.latitude+", "+position.longitude, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

}
