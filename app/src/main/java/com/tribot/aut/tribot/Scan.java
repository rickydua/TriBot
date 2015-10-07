package com.tribot.aut.tribot;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.ArrayList;
import java.util.UUID;

public class Scan extends AppCompatActivity {

    private static final String TAB = "\t\t\t\t";
    private static final String TAG = "autMyo";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayAdapter<String> listViewArray;
    public static final int REQ_ENABLE_BT = 4;
    private static BluetoothGatt gattProfile;

    public Scan(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        init();
        initScan();
    }

    public void init(){
        // Initializes a Bluetooth listViewArray.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Prompt the user to turn on Bluetooth, if disabled.
        //Requires Bluetooth permission.
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQ_ENABLE_BT);
        }

        //Setting up ListView for dynamic modification at runtime.
        bluetoothDevices = new ArrayList<>();
        listViewArray = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,new ArrayList<String>());
        ListView view = (ListView) findViewById(R.id.listView);
        view.setAdapter(listViewArray);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //obtain selected Bluetooth Device and connect to it.
                createDialog(position);
            }
        });
    }

    public void createDialog(final int position1){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
// Add the buttons
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                connectMyo();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BluetoothDevice currentlySelected = bluetoothDevices.get(position1);
                gattProfile = currentlySelected.connectGatt(Scan.this, false, new GattCallBack());
            }
        });



// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.setTitle("Would you like to connect to Myo");
        dialog.show();

    }

    public void initScan() {
        final ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
        final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                //Store found devices in ArrayList<BluetoothDevice> and ArrayAdapter<String>.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!bluetoothDevices.contains(device)) {
                            //Format = [DeviceName]  [MAC Address].
                            listViewArray.add(device.getName() + TAB + device.getAddress());
                            bluetoothDevices.add(device);
                            listViewArray.notifyDataSetChanged();
                        }
                    }
                });
            }
        };

        //Stop Scanning after 10 seconds, contributes to power saving.
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(scanCallback);
                bar.setVisibility(View.GONE);
                makeToast("Scan Finished");
            }
        }, 10000);


        //Start the scan.
        bluetoothAdapter.startLeScan(scanCallback);
    }

    public void makeToast(String title){
        Toast.makeText(this,title,Toast.LENGTH_LONG).show();
    }

    public BluetoothGatt getGattProfile(){
        return gattProfile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return true;
        //test
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

        else if(id == R.id.action_connectMyo){
            connectMyo();
        }else if(id == R.id.action_rescan){
            listViewArray.clear();
            bluetoothDevices.clear();
            initScan();
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectMyo(){
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            finish();
        }

        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);

        DeviceListener mListener = new AbstractDeviceListener() {
            @Override
            public void onConnect(Myo myo, long timestamp) {
                Toast.makeText(Scan.this, "Myo Connected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnect(Myo myo, long timestamp) {
                Toast.makeText(Scan.this, "Myo Disconnected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPose(Myo myo, long timestamp, Pose pose) {
              BluetoothGattCharacteristic blunoReadWrite = gattProfile.getServices().get(3).getCharacteristics().get(0);
                Toast.makeText(Scan.this, "Pose: " + pose, Toast.LENGTH_SHORT).show();
                if(pose.toString().equals("FIST")){
                    System.out.println(pose);
                    blunoReadWrite.setValue("0|-127|127");
                    gattProfile.writeCharacteristic(blunoReadWrite);
                }

                if (pose.toString().equals("FINGERS_SPREAD")){
                    System.out.println(pose);
                    blunoReadWrite.setValue("0|0|0");
                    gattProfile.writeCharacteristic(blunoReadWrite);
                }

                if (pose.toString().equals("WAVE_OUT")){
                    System.out.println(pose);
                    blunoReadWrite.setValue("-127|-127|-127");
                    gattProfile.writeCharacteristic(blunoReadWrite);
                }
                if (pose.toString().equals("WAVE_IN")){
                    System.out.println(pose);
                    blunoReadWrite.setValue("127|127|127");
                    gattProfile.writeCharacteristic(blunoReadWrite);
                }
            }
        };
        Hub.getInstance().addListener(mListener);
    }



    /**
     * GattCallBack InnerClass
     **/

    private class GattCallBack extends BluetoothGattCallback{
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                System.out.println("Connected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        makeToast("Connected");
                    }
                });
                //initiate discovering Services and BluetoothGattCharacteristics.
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Intent intent = new Intent(Scan.this,ControlActivity.class);
            //tent.putExtra("gatt",gatt);
            startActivity(intent);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    }
}
