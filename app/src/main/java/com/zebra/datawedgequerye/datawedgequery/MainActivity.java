package com.zebra.datawedgequerye.datawedgequery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private String activeProfile;
    private String selectedScanner;
    private String scanner_selection;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createProfile();
        datawedgeQuery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReceivers();
    }

// Register/unregister broadcast receiver and filter results

    void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION");
        filter.addAction("com.symbol.datawedge.api.ACTION_ENUMERATEDSCANNERLIST");
        filter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(myBroadcastReceiver, filter);
    }


    private void enumerateScanner()
    {
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.ENUMERATE_SCANNERS", "");
        this.sendBroadcast(i);
    }

    private void createProfile() {
        Bundle bMain = new Bundle();
        bMain.putString("PROFILE_NAME", "DataWedgeQuery");
        bMain.putString("PROFILE_ENABLED","true");
        bMain.putString("CONFIG_MODE","CREATE_IF_NOT_EXIST");


        Bundle bundleApp = new Bundle();
        bundleApp.putString("PACKAGE_NAME","com.zebra.datawedgequerye.datawedgequery");
        bundleApp.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        bMain.putParcelableArray("APP_LIST", new Bundle[]{
                bundleApp
        });

        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
        this.sendBroadcast(i);

    }

    void unRegisterReceivers(){
        unregisterReceiver(myBroadcastReceiver);
    }


    private void getConfig() {

                Bundle bMain = new Bundle();
                bMain.putString("PROFILE_NAME", activeProfile);
                Bundle bConfig = new Bundle();
                ArrayList<String> pluginName = new ArrayList<>();
                pluginName.add("BARCODE");

                bConfig.putStringArrayList("PLUGIN_NAME", pluginName);
                bMain.putBundle("PLUGIN_CONFIG", bConfig);


                Intent i = new Intent();
                i.putExtra("SEND_RESULT", "true");
                i.setAction("com.symbol.datawedge.api.ACTION");
                i.putExtra("com.symbol.datawedge.api.GET_CONFIG", bMain);
                sendBroadcast(i);


    }

    private void datawedgeQuery() {
        //Sending the intent

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent();
                i.setAction("com.symbol.datawedge.api.ACTION");
                i.putExtra("com.symbol.datawedge.api.GET_ACTIVE_PROFILE", "");
                sendBroadcast(i);

            }
        });



    }


    //Receiving the result
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE")) {
                activeProfile = intent.getStringExtra("com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE");
                Toast.makeText(getApplicationContext(), "Datawedge status is: " + activeProfile, Toast.LENGTH_LONG).show();
                Log.d("Profile Name:", activeProfile);
                getConfig();
            }



            if (intent.hasExtra("com.symbol.datawedge.api.RESULT_GET_CONFIG")) {
                Bundle result = intent.getBundleExtra("com.symbol.datawedge.api.RESULT_GET_CONFIG");
                ArrayList<Bundle> pluginConfig = result.getParcelableArrayList("PLUGIN_CONFIG");
                //  In the call to Get_Config we only requested the barcode plugin config (which will be index 0)
                Bundle barcodeProps = pluginConfig.get(0).getBundle("PARAM_LIST");
                scanner_selection = barcodeProps.getString("scanner_selection");
                Log.d("Scanner Selection:", scanner_selection);
                enumerateScanner();
            }

            if (intent.hasExtra("com.symbol.datawedge.api.RESULT_ENUMERATE_SCANNERS")) {
                //  6.3 API to EnumerateScanners.  Note the format is very different from 6.0.
                ArrayList scanner_list_arraylist = b.getParcelableArrayList("com.symbol.datawedge.api.RESULT_ENUMERATE_SCANNERS");
                //  Enumerate Scanners (6.3) returns a bundle array.  Each bundle has the following keys:
                //  SCANNER_CONNECTION_STATE
                //  SCANNER_NAME
                //  SCANNER_INDEX
                String[] scanner_list = new String[scanner_list_arraylist.size()];
                String[] scanner_list_with_index = new String[scanner_list_arraylist.size()];
                String userFriendlyScanners = "";
                for (int i = 0; i < scanner_list_arraylist.size(); i++) {
                    String scannerName = (String) ((Bundle) scanner_list_arraylist.get(i)).get("SCANNER_NAME");
                    Integer scannerIndex = (Integer) ((Bundle) scanner_list_arraylist.get(i)).get("SCANNER_INDEX");
                    scanner_list[i] = scannerName;
                    scanner_list_with_index[i] = scannerName + " [" + scannerIndex.intValue() + "]";
                    userFriendlyScanners += "{" + scannerName + "} ";
                }
                selectedScanner = scanner_list[Integer.parseInt(scanner_selection)];
                Toast.makeText(getApplicationContext(), "Scanner:" + selectedScanner, Toast.LENGTH_LONG).show();
            }
        }
    };

}

