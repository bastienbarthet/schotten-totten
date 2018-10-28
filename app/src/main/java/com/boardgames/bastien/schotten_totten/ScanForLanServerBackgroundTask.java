package com.boardgames.bastien.schotten_totten;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.boardgames.bastien.schotten_totten.server.RestGameClient;
import com.boradgames.bastien.schotten_totten.core.model.PlayingPlayerType;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanForLanServerBackgroundTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog waitingDialog;
    private LauncherActivity activity;

    public ScanForLanServerBackgroundTask(final LauncherActivity activity) {
        waitingDialog = new ProgressDialog(activity);
        waitingDialog.setTitle(activity.getString(R.string.contacting_server));
        waitingDialog.setMessage(activity.getString(R.string.please_wait));
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        final String serverIp = result;
        // start game
        if (!serverIp.isEmpty()) {
            final Intent joinIntent = new Intent(activity, ServerGameActivity.class);
            joinIntent.putExtra(activity.getString(R.string.server_url_key), "http://" + serverIp + ":8080");
            joinIntent.putExtra(activity.getString(R.string.game_name_key), activity.getString(R.string.lan_game));
            joinIntent.putExtra(activity.getString(R.string.type_key), PlayingPlayerType.TWO.toString());
            activity.startActivity(joinIntent);
        } else {
            // no server found
           activity.showError(activity.getString(R.string.no_local_server_title), activity.getString(R.string.no_local_server_message));
        }
        if (waitingDialog.isShowing()) {
            waitingDialog.dismiss();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        // get ip
        String serverIp = "";
        try {
            final String myIp = getIPAddress();
            final String mySubLan = myIp.substring(0, myIp.lastIndexOf('.') + 1);
            // scan
            for (int i = 1; i < 250; i++) {
                final String ipToScan = mySubLan + i;
                final RestGameClient restGameClient =
                        new RestGameClient("http://" + ipToScan + ":8080", "lanGame");
                try {
                    final String pingResult = restGameClient.ping();
                    if (pingResult.contains("SCHOTTEN")) {
                        serverIp = ipToScan;
                        break;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    // timeout, not the right host
                }
            }
        } catch (final UnknownHostException e) {
           activity.showError(e);
        }

        return serverIp;
    }

    protected String getIPAddress() throws UnknownHostException {
        try {
            final List<NetworkInterface> interfaces =
                    Collections.list(NetworkInterface.getNetworkInterfaces());

            // find vpn
            for (final NetworkInterface i : interfaces) {
                if (i.getName().equals("ppp0")) {
                    for (final InetAddress a : Collections.list(i.getInetAddresses())) {
                        if (!a.isLoopbackAddress()
                                && a.getHostAddress().matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                            return a.getHostAddress();
                        }
                    }
                }
            }
            // find wifi
            for (final NetworkInterface i : interfaces) {
                if (i.getName().equals("wlan0")) {
                    for (final InetAddress a : Collections.list(i.getInetAddresses())) {
                        if (!a.isLoopbackAddress()
                                && a.getHostAddress().matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                            return a.getHostAddress();
                        }
                    }
                }
            }

        } catch (final Exception ex) {
            activity.showError(ex);
        }
        throw new UnknownHostException();
    }

}