package com.github.goldfish07.hydra.vpn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.github.goldfish07.hydra.vpn.util.CountriesNames;
import com.github.goldfish07.hydra.vpn.utils.Converter;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import unified.vpn.sdk.Callback;
import unified.vpn.sdk.CompletableCallback;
import unified.vpn.sdk.FireshieldCategory;
import unified.vpn.sdk.FireshieldConfig;
import unified.vpn.sdk.HydraTransport;
import unified.vpn.sdk.HydraVpnTransportException;
import unified.vpn.sdk.NetworkRelatedException;
import unified.vpn.sdk.OpenVpnTransport;
import unified.vpn.sdk.PartnerApiException;
import unified.vpn.sdk.SessionConfig;
import unified.vpn.sdk.SessionInfo;
import unified.vpn.sdk.TrackingConstants;
import unified.vpn.sdk.TrafficRule;
import unified.vpn.sdk.TrafficStats;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnPermissionDeniedException;
import unified.vpn.sdk.VpnPermissionRevokedException;
import unified.vpn.sdk.VpnState;
import unified.vpn.sdk.VpnStateListener;

public class ServerActivity extends AppCompatActivity implements VpnStateListener {
    private String currentServer;

    public static boolean statusConnection = false;
    private Map<String, String> localeCountries;
    public static boolean isConnected;
    private Snackbar snackbar;
    private boolean isVPNConnected;

    LinearProgressIndicator connectingProgress;
    Button serverConnectBtn;
    TextView lastLog;
    TextView trafficIn;
    TextView trafficOut;
    ImageView serverFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        connectingProgress = findViewById(R.id.serverConnectingProgress);
        serverConnectBtn = findViewById(R.id.serverConnect);
        lastLog = findViewById(R.id.serverStatus);
        trafficIn = findViewById(R.id.serverTrafficIn);
        trafficOut = findViewById(R.id.serverTrafficOut);
        serverFlag = findViewById(R.id.serverFlag);

        trafficIn.setText("0 B");
        trafficOut.setText("0 B");
        localeCountries = CountriesNames.getCountries();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lastLog.setText(R.string.not_connected);
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.connected), Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackbarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#00e676"));
        initView(getIntent());
        UnifiedSdk.addVpnStateListener(this);
    }

    private void initView(Intent intent) {
        currentServer = intent.getStringExtra("country");
        serverFlag.setImageDrawable(getDrawableFromAssets(currentServer));
        String name = localeCountries.get(currentServer.toUpperCase());
        ((TextView) findViewById(R.id.serverCountry)).setText(name);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getDrawableFromAssets(String str) {
        if (str != null) {
            try {
                AssetManager assets = this.getAssets();
                return Drawable.createFromStream(assets.open(str.toLowerCase() + ".png"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.getResources().getDrawable(R.drawable.unknown);
    }


    private void prepareVpn() {
        connectingProgress.setVisibility(View.VISIBLE);
        serverConnectBtn.setText(getString(R.string.disconnect));
        connectToVpn();
    }

    public void serverOnClick(View view) {
        if (view.getId() == R.id.serverConnect) {
            UnifiedSdk.getVpnState(new Callback<VpnState>() {
                @Override
                public void success(@NonNull VpnState state) {
                    if (state == VpnState.CONNECTED || state == VpnState.CONNECTING_VPN || state == VpnState.CONNECTING_PERMISSIONS || state == VpnState.CONNECTING_CREDENTIALS) {
                        UnifiedSdk.getStatus((new Callback<SessionInfo>() {
                            @Override
                            public void success(@NonNull SessionInfo sessionInfo) {
                                if (sessionInfo.getSessionConfig().getCountry().toLowerCase().equals(currentServer)) {
                                    prepareStopVPN();
                                } else {
                                    isVPNConnected = true;
                                    prepareStopVPN();
                                }
                            }

                            @Override
                            public void failure(@NonNull VpnException e) {

                            }
                        }));
                    } else if (state == VpnState.IDLE) {
                        prepareVpn();
                    }
                }

                @Override
                public void failure(@NonNull VpnException e) {
                }
            });
        }
    }

    private void prepareStopVPN() {
        statusConnection = false;
        connectingProgress.setVisibility(View.GONE);
        if (snackbar.isShown()) {
            snackbar.dismiss();
        }
        lastLog.setText(R.string.disconnecting);
        serverConnectBtn.setText(getString(R.string.connect));
        disconnectFromVnp();
    }


    protected void connectToVpn() {
        if (UnifiedSdk.getInstance().getBackend().isLoggedIn()) {
            List<String> fallbackOrder = new ArrayList<>();
            fallbackOrder.add(HydraTransport.TRANSPORT_ID);
            fallbackOrder.add(OpenVpnTransport.TRANSPORT_ID_TCP);
            fallbackOrder.add(OpenVpnTransport.TRANSPORT_ID_UDP);
            List<String> bypassDomains = new LinkedList<>();
            bypassDomains.add("*facebook.com");
            bypassDomains.add("*wtfismyip.com");
            UnifiedSdk.getInstance().getVpn().start(new SessionConfig.Builder()
                    .withReason(TrackingConstants.GprReasons.M_UI)
                    .withTransportFallback(fallbackOrder)
                    .withTransport(HydraTransport.TRANSPORT_ID)
                    .withVirtualLocation(currentServer)
                    .withFireshieldConfig(createFireshieldConfig())
                    .addDnsRule(TrafficRule.Builder.bypass().fromDomains(bypassDomains))
                    .build(), new CompletableCallback() {
                @Override
                public void complete() {
                    //we are using VpnStateListener
                }

                @Override
                public void error(@NonNull VpnException e) {

                }
            });
        }
        else {
            // showMessage("Login please");
        }
    }

    FireshieldConfig createFireshieldConfig() {
        FireshieldConfig.Builder builder = new FireshieldConfig.Builder();
        builder.enabled(true);
        builder.addService(FireshieldConfig.Services.IP);
        builder.addService(FireshieldConfig.Services.SOPHOS);
        builder.addCategory(FireshieldCategory.Builder.vpn(FireshieldConfig.Categories.SAFE));//need to add safe category to allow safe traffic
        return builder.build();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void vpnStateChanged(@NonNull VpnState vpnState) {
        switch (vpnState) {
            case CONNECTED:
                UnifiedSdk.getStatus(new Callback<SessionInfo>() {
                    @Override
                    public void success(@NonNull SessionInfo sessionInfo) {
                        //
                        // Toast.makeText(ServerActivity.this, sessionInfo.getSessionConfig().getVirtualLocation(), Toast.LENGTH_LONG).show();
                        if (sessionInfo.getSessionConfig().getCountry().toLowerCase().equals(currentServer)) {
                            snackbar.show();
                            isConnected = true;
                            connectingProgress.setVisibility(View.GONE);
                            getTraffic();
                            serverConnectBtn.setText(getString(R.string.disconnect));
                            lastLog.setText(R.string.connected);
                            getSharedPreferences("VPN_SERVER", 0).edit().putString("server", currentServer).apply();
                        } else {
                            serverConnectBtn.setText(getString(R.string.connect));
                        }
                    }
                    @Override
                    public void failure(@NonNull VpnException e) {

                    }
                });

                break;
            case IDLE:
                lastLog.setText(R.string.not_connected);
                serverConnectBtn.setText(getString(R.string.connect));
                if (snackbar.isShown()) {
                    snackbar.dismiss();
                }
                break;
            case CONNECTING_PERMISSIONS:
            case CONNECTING_CREDENTIALS:
            case CONNECTING_VPN:
                lastLog.setText(R.string.connecting);
                serverConnectBtn.setText(getString(R.string.disconnect));
                connectingProgress.setVisibility(View.VISIBLE);
                break;
            case DISCONNECTING:
                lastLog.setText(R.string.disconnecting);
                break;
        }
    }

    @Override
    public void vpnError(@NonNull VpnException e) {
        if (e instanceof NetworkRelatedException) {
            showMessage("Check internet connection");
        } else if (e instanceof VpnException) {
            if (e instanceof VpnPermissionRevokedException) {
                showMessage("User revoked vpn permissions");
            } else if (e instanceof VpnPermissionDeniedException) {
                showMessage("User canceled to grant vpn permissions");
            } else if (e instanceof HydraVpnTransportException) {
                HydraVpnTransportException hydraVpnTransportException = (HydraVpnTransportException) e;
                if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_ERROR_BROKEN) {
                    showMessage("Connection with vpn server was lost");
                } else if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_DCN_BLOCKED_BW) {
                    showMessage("Client traffic exceeded");
                } else {
                    showMessage("Error in VPN transport");
                }
            } else {
                showMessage("Error in VPN Service");
            }
        } else if (e instanceof PartnerApiException) {
            switch (((PartnerApiException) e).getContent()) {
                case PartnerApiException.CODE_NOT_AUTHORIZED:
                    showMessage("User unauthorized");
                    break;
                case PartnerApiException.CODE_TRAFFIC_EXCEED:
                    showMessage("Server unavailable");
                    break;
                default:
                    showMessage("Other error. Check PartnerApiException constants");
                    break;
            }
        }
    }

    protected void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void getTraffic() {
        UnifiedSdk.getTrafficStats(new Callback<TrafficStats>() {
            @Override
            public void success(@NonNull TrafficStats trafficStats) {
                if (isConnected) {
                    trafficIn.setText(Converter.humanReadableByteCountOld(trafficStats.getBytesRx(), false));
                    trafficOut.setText(Converter.humanReadableByteCountOld(trafficStats.getBytesTx(), false));
                } else {
                    trafficOut.setText("");
                    trafficIn.setText("");
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
            }
        });
    }


    protected void disconnectFromVnp() {
        UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
            @Override
            public void complete() {
                lastLog.setText(R.string.not_connected);
                if (isVPNConnected) {
                    connectToVpn();
                }
                isVPNConnected = false;
            }

            @Override
            public void error(@NonNull VpnException e) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isConnected = false;
    }
}
