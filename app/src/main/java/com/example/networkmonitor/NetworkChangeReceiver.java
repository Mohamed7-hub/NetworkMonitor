package com.example.networkmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkChangeListener networkChangeListener;

    public NetworkChangeReceiver(NetworkChangeListener listener) {
        this.networkChangeListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            boolean isConnected = NetworkUtil.isNetworkConnected(context);

            if (networkChangeListener != null) {
                networkChangeListener.onNetworkChanged(isConnected);
            }
        }
    }

    public interface NetworkChangeListener {
        void onNetworkChanged(boolean isConnected);
    }
}

