package com.example.networkmonitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkChangeListener {

    private NetworkChangeReceiver networkChangeReceiver;
    private CoordinatorLayout mainLayout;
    private TextView statusText;
    private TextView statusDescription;
    private ImageView statusIcon;
    private Button checkNowButton;
    private Button settingsButton;
    private TextView connectionTypeText;
    private TextView ipAddressText;
    private TextView lastCheckText;
    private MaterialCardView statusCard;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        mainLayout = findViewById(R.id.main_layout);
        statusText = findViewById(R.id.status_text);
        statusDescription = findViewById(R.id.status_description);
        statusIcon = findViewById(R.id.status_icon);
        checkNowButton = findViewById(R.id.check_now_button);
        settingsButton = findViewById(R.id.settings_button);
        connectionTypeText = findViewById(R.id.connection_type);
        ipAddressText = findViewById(R.id.ip_address);
        lastCheckText = findViewById(R.id.last_check);
        statusCard = findViewById(R.id.status_card);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set initial network status
        isConnected = NetworkUtil.isNetworkConnected(this);
        updateUI(isConnected);

        // Initialize network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this);

        // Set up check now button
        checkNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNetworkStatus();
            }
        });

        // Set up settings button
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNetworkSettings();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

        // Check network status when resuming
        checkNetworkStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the broadcast receiver
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        this.isConnected = isConnected;
        updateUI(isConnected);

        if (isConnected) {
            // Show connected toast
            Toast.makeText(this, R.string.internet_connected, Toast.LENGTH_SHORT).show();
        } else {
            // Show disconnected toast
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();

            // Show Snackbar with retry button
            showNetworkSnackbar();

            // Show dialog to enable network
            showNetworkDialog();
        }
    }

    private void checkNetworkStatus() {
        // Update the last check time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastCheckText.setText(currentTime);

        // Check if connected
        boolean isNowConnected = NetworkUtil.isNetworkConnected(this);
        updateUI(isNowConnected);

        // Update connection details
        updateConnectionDetails();
    }

    private void updateUI(boolean isConnected) {
        if (isConnected) {
            statusText.setText(R.string.internet_connected);
            statusDescription.setText("Your device is connected to the internet");
            statusIcon.setImageResource(R.drawable.ic_network_check);
            statusCard.setCardBackgroundColor(getResources().getColor(R.color.md_theme_primary, null));
        } else {
            statusText.setText(R.string.no_internet);
            statusDescription.setText("Your device is not connected to the internet");
            statusIcon.setImageResource(R.drawable.ic_network_error);
            statusCard.setCardBackgroundColor(getResources().getColor(R.color.md_theme_errorContainer, null));
        }
    }

    private void updateConnectionDetails() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            // Update connection type
            String connectionType = "Unknown";
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    connectionType = "WiFi";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    connectionType = "Mobile Data";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    connectionType = "Ethernet";
                    break;
            }
            connectionTypeText.setText(connectionType);

            // Update IP address
            String ipAddress = getIPAddress();
            ipAddressText.setText(ipAddress);
        } else {
            connectionTypeText.setText(R.string.unknown);
            ipAddressText.setText(R.string.unknown);
        }
    }

    private String getIPAddress() {
        try {
            // Try WiFi IP first
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipInt = wifiInfo.getIpAddress();
                return Formatter.formatIpAddress(ipInt);
            }

            // Try other network interfaces
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4) {
                            return sAddr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void showNetworkSnackbar() {
        Snackbar snackbar = Snackbar.make(mainLayout, R.string.no_internet, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check network status again
                        checkNetworkStatus();
                    }
                });
        snackbar.show();
    }

    private void showNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.network_dialog_title)
                .setMessage(R.string.network_dialog_message)
                .setPositiveButton(R.string.wifi, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Open WiFi settings
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.mobile_data, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Open mobile data settings
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });

        // Create and show the AlertDialog
        builder.create().show();
    }

    private void openNetworkSettings() {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }
}

