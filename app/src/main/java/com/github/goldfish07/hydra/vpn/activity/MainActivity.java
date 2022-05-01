package com.github.goldfish07.hydra.vpn.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.github.goldfish07.hydra.vpn.BuildConfig;
import com.github.goldfish07.hydra.vpn.MainApplication;
import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.fragment.FreeServersFragment;
import com.github.goldfish07.hydra.vpn.fragment.PaidServerFragment;
import com.github.goldfish07.hydra.vpn.utils.CountriesNames;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import unified.vpn.sdk.AuthMethod;
import unified.vpn.sdk.AvailableCountries;
import unified.vpn.sdk.Callback;
import unified.vpn.sdk.CompletableCallback;
import unified.vpn.sdk.Country;
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
import unified.vpn.sdk.TrafficListener;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.User;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnPermissionDeniedException;
import unified.vpn.sdk.VpnPermissionRevokedException;
import unified.vpn.sdk.VpnState;
import unified.vpn.sdk.VpnStateListener;

public class MainActivity extends AppCompatActivity implements TrafficListener, VpnStateListener {

    private String selectedCountry = "";

    private static FreeServerListListener freeServerListListener;
    private static PaidServerListListener paidServerListListener;

    public MainApplication myData = MainApplication.getInstance();
    public static ArrayList freeCountries;
    public static ArrayList paidCountries;

    ViewPager viewPager;

    SegmentTabLayout tabLayout;

    public interface FreeServerListListener {
        void onGotFreeServers(List<Country> list);

        void onServersLoading();
    }

    public interface PaidServerListListener {
        void onGotPaidServers(List<Country> list);

        void onServersLoading();
    }
    Button connectBtn;

    ImageView flag;

    TextView txtStatus;

    LinearProgressIndicator serverConnectingProgress;

    private final ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {"Free Server", "Premium Server"};

    Map<String, String> localeCountries;

    boolean isConnected = false;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabLayout);
        connectBtn = findViewById(R.id.connectBtn);
        flag = findViewById(R.id.imgFlag);
        txtStatus = findViewById(R.id.txtStatus);
        serverConnectingProgress = findViewById(R.id.serverConnectingProgress);
        connectBtn.setOnClickListener(clickListener);
        localeCountries = CountriesNames.getCountries();


        mFragments.add(new FreeServersFragment());
        mFragments.add(new PaidServerFragment());
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        tabLayout.setTabData(mTitles);
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ((MainApplication) getApplication()).setNewHostAndCarrier(BuildConfig.BASE_HOST, BuildConfig.BASE_CARRIER_ID);
        if (!isLoggedIn()) {
            loginToVpn();
        } else {
            loadServers();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        UnifiedSdk.addTrafficListener(this);
        UnifiedSdk.addVpnStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UnifiedSdk.removeVpnStateListener(this);
        UnifiedSdk.removeTrafficListener(this);
    }

    private void loadServers() {
        if (freeServerListListener != null) {
            freeServerListListener.onServersLoading();
        }
        if (paidServerListListener != null) {
            paidServerListListener.onServersLoading();
        }
        // showProgress();
        UnifiedSdk.getInstance().getBackend().countries(new Callback<AvailableCountries>() {
            @Override
            public void success(@NonNull final AvailableCountries countries) {
                // hideProgress();
                // regionAdapter.setRegions(countries.getCountries());
                myData.countries = countries.getCountries();
                divideList(myData.countries);
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }



    public void divideList(List<Country> list) {
        freeCountries = new ArrayList();
        paidCountries = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            if (i < list.size() / 2) {
                freeCountries.add(list.get(i));
            } else {
                paidCountries.add(list.get(i));
            }
        }
        if (freeServerListListener != null) {
            freeServerListListener.onGotFreeServers(freeCountries);
        }
        if (paidServerListListener != null) {
            paidServerListListener.onGotPaidServers(paidCountries);
        }
    }

    public String connectToBestServer() {
        String server;
        SharedPreferences preferences = getSharedPreferences("VPN_SERVER", 0);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getString("server", "").isEmpty()) {
            server = "us";
            editor.putString("server", "us").apply();
        } else {
            server = preferences.getString("server", "");
        }

        return server;
    }


    @Override
    public void onTrafficUpdate(long bytesTx, long bytesRx) {
     //   updateTrafficStats(bytesTx, bytesRx);
    }



    @Override
    public void vpnError(@NonNull VpnException e) {
        handleError(e);
    }

    protected boolean isLoggedIn() {
        return UnifiedSdk.getInstance().getBackend().isLoggedIn();
    }

    protected void loginToVpn() {
        AuthMethod authMethod = AuthMethod.anonymous();
        UnifiedSdk.getInstance().getBackend().login(authMethod, new Callback<User>() {
            @Override
            public void success(@NonNull User user) {
                if (isLoggedIn()) {
                    loadServers();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                handleError(e);
            }
        });
    }


    protected void isConnected(Callback<Boolean> callback) {
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState vpnState) {
                callback.success(vpnState == VpnState.CONNECTED);
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.success(false);
            }
        });
    }


    protected void connectToVpn() {
        if(isLoggedIn()){
            List<String> fallbackOrder = new ArrayList<>();
            fallbackOrder.add(HydraTransport.TRANSPORT_ID);
            fallbackOrder.add(OpenVpnTransport.TRANSPORT_ID_TCP);
            fallbackOrder.add(OpenVpnTransport.TRANSPORT_ID_UDP);
            //  showConnectProgress();
            List<String> bypassDomains = new LinkedList<>();
            bypassDomains.add("*facebook.com");
            bypassDomains.add("*wtfismyip.com");
            UnifiedSdk.getInstance().getVpn().start(new SessionConfig.Builder()
                    .withReason(TrackingConstants.GprReasons.M_UI)
                    .withTransportFallback(fallbackOrder)
                    .withTransport(HydraTransport.TRANSPORT_ID)
                    .withVirtualLocation(connectToBestServer())
                    .withFireshieldConfig(createFireShieldConfig())
                    //.addDnsRule(TrafficRule.Builder.bypass().fromDomains(bypassDomains))
                    .build(), new CompletableCallback() {
                @Override
                public void complete() {
                    //  hideConnectProgress();
                    //startUIUpdateTask();
                }

                @Override
                public void error(@NonNull VpnException e) {
                    handleError(e);
                }
            });
        }
    }

    FireshieldConfig createFireShieldConfig() {
        FireshieldConfig.Builder builder = new FireshieldConfig.Builder();
        builder.enabled(true);
        builder.addService(FireshieldConfig.Services.IP);
        builder.addService(FireshieldConfig.Services.SOPHOS);
        builder.addCategory(FireshieldCategory.Builder.vpn(FireshieldConfig.Categories.SAFE));//need to add safe category to allow safe traffic

        return builder.build();
    }


    protected void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            UnifiedSdk.getVpnState(new Callback<VpnState>() {
                @Override
                public void success(@NonNull VpnState vpnState) {
                    if (vpnState == VpnState.CONNECTED) {
                      disconnectFromVnp();
                    } else if (vpnState == VpnState.IDLE) {
                        connectToVpn();
                    }
                }

                @Override
                public void failure(@NonNull VpnException e) {

                }
            });


        }
    };


    protected void disconnectFromVnp() {
        UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
            @Override
            public void complete() {
               // hideConnectProgress();
               // stopUIUpdateTask();
                //serverConnectingProgress.setVisibility(View.GONE);

            }

            @Override
            public void error(@NonNull VpnException e) {
               handleError(e);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void vpnStateChanged(@NonNull VpnState vpnState) {
        switch (vpnState) {
            case CONNECTED:
                UnifiedSdk.getStatus(new Callback<SessionInfo>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void success(@NonNull SessionInfo sessionInfo) {
                        serverConnectingProgress.setVisibility(View.GONE);
                        flag.setImageDrawable(getDrawableFromAssets(sessionInfo.getSessionConfig().getCountry().toLowerCase()));
                        txtStatus.setText(getString(R.string.connected_to) + " " + localeCountries.get(sessionInfo.getSessionConfig().getCountry().toLowerCase().toUpperCase()));
                    }
                    @Override
                    public void failure(@NonNull VpnException e) {

                    }
                });
                serverConnectingProgress.setVisibility(View.GONE);
                connectBtn.setText(getString(R.string.disconnect));
                isConnected = true;
                /*if (!current_server.isEmpty()) {
                    flag.setImageDrawable(getDrawableFromAssets(current_server));
                }*/
                break;
            case DISCONNECTING:
                connectBtn.setText(getString(R.string.disconnecting));
                txtStatus.setText(getString(R.string.disconnecting));
                break;
            case IDLE:
                flag.setImageResource(R.drawable.unknown);
                serverConnectingProgress.setVisibility(View.GONE);
                connectBtn.setText(getString(R.string.connect));
                txtStatus.setText(getString(R.string.not_connected));
                isConnected = false;
                break;
            case CONNECTING_PERMISSIONS:
            case CONNECTING_CREDENTIALS:
            case CONNECTING_VPN:
                serverConnectingProgress.setVisibility(View.VISIBLE);
                connectBtn.setText(getString(R.string.disconnect));
                txtStatus.setText(R.string.connecting);
                break;
        }
    }


    protected void getCurrentServer(final Callback<String> callback) {
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState state) {
                if (state == VpnState.CONNECTED) {
                    UnifiedSdk.getStatus(new Callback<SessionInfo>() {
                        @Override
                        public void success(@NonNull SessionInfo sessionInfo) {
                          //  callback.success(CredentialsCompat.getServerCountry(sessionInfo.getCredentials()));
                        }

                        @Override
                        public void failure(@NonNull VpnException e) {
                            callback.success(selectedCountry);
                        }
                    });
                } else {
                    callback.success(selectedCountry);
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.failure(e);
            }
        });
    }


    public void onRegionSelected(Country item) {
        selectedCountry = item.getCountry();
       // updateUI();

        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState state) {
                if (state == VpnState.CONNECTED) {
                    showMessage("Reconnecting to VPN with " + selectedCountry);
                    UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
                        @Override
                        public void complete() {
                            connectToVpn();
                        }

                        @Override
                        public void error(@NonNull VpnException e) {
                            // In this case we try to reconnect
                            selectedCountry = "";
                            connectToVpn();
                        }
                    });
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    public void handleError(Throwable e) {
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


    public static void setFreeServerListListener(FreeServerListListener freeServerListListener2) {
        freeServerListListener = freeServerListListener2;
    }

    public static void setPaidServerListListener(PaidServerListListener paidServerListListener2) {
        paidServerListListener = paidServerListListener2;
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return MainActivity.this.mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = new FreeServersFragment();
            } else if (position == 1) {
                fragment = new PaidServerFragment();
            }
            return fragment;
        }
    }
}
