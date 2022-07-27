package com.example.wifidirect;



import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Button btnDiscover, btnSend, btnDisconnect;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText editText;

    // WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
    }


    private void initialWork() {
        btnDiscover = findViewById(R.id.discover);
        btnSend = findViewById(R.id.sendButton);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        listView = findViewById(R.id.peerListView);
        read_msg_box = findViewById(R.id.readMsg);
        connectionStatus = findViewById(R.id.connectionStatus);
        editText = findViewById(R.id.inputField);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0){
                Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
            }
        }
    };



    @SuppressLint("MissingPermission")
    private void exqListener(){
        btnDiscover.setOnClickListener(v -> mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                connectionStatus.setText("Discovery Started");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(int i) {
                connectionStatus.setText("Discovery Starting Failed!");
            }
        }));

        btnDisconnect.setOnClickListener(v -> {
            if (mManager != null && mChannel != null){
                mManager.requestGroupInfo(mChannel,
                        group -> {
                            if (group != null && mManager != null && mChannel != null){
                                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.v("Removed current group? ", "SUCCESS");
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.v("Removed current group? ", "FAILURE - " + reason);
                                    }
                                });
                            }
                        });
            }
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            final WifiP2pDevice device = deviceArray[i];
            WifiP2pConfig config = new WifiP2pConfig();
            // make host
            config.groupOwnerIntent = 15;
            config.deviceAddress = device.deviceAddress;

            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Group created!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "P2P group creation failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSend.setOnClickListener(v -> {
            String msg = editText.getText().toString();
            Log.v("Message: ", Arrays.toString(msg.getBytes()));
        });
    }


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            Log.v("group formed? ", "" + wifiP2pInfo.groupFormed);

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connectionStatus.setText("Host/Server");
                String stringGroupOwnerAddress = groupOwnerAddress.getHostAddress();
                Log.v("GO Address: ", stringGroupOwnerAddress);
                try {
                    new ServerAsyncTask(5000).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (wifiP2pInfo.groupFormed){
                connectionStatus.setText("Client");
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}