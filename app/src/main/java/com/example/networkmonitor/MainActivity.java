package com.example.networkmonitor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkChangeListener {

    private NetworkChangeReceiver networkChangeReceiver;
    private ConstraintLayout mainLayout;
    private TextView statusText;
    private ImageView statusIcon;
    private Button settingsButton;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        mainLayout = findViewById(R.id.main_layout);
        statusText = findViewById(R.id.status_text);
        statusIcon = findViewById(R.id.status_icon);
        settingsButton = findViewById(R.id.settings_button);

        // Set initial network status
        isConnected = NetworkUtil.isNetworkConnected(this);
        updateUI(isConnected);

        // Initialize network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this);

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

    private void updateUI(boolean isConnected) {
        if (isConnected) {
            statusText.setText(R.string.internet_connected);
            statusIcon.setImageResource(R.drawable.ic_network_check);
        } else {
            statusText.setText(R.string.no_internet);
            statusIcon.setImageResource(R.drawable.ic_network_error);
        }
    }

    private void showNetworkSnackbar() {
        Snackbar snackbar = Snackbar.make(mainLayout, R.string.no_internet, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check network status again
                        boolean isNowConnected = NetworkUtil.isNetworkConnected(MainActivity.this);
                        updateUI(isNowConnected);

                        if (!isNowConnected) {
                            // Still not connected, show dialog
                            showNetworkDialog();
                        }
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

