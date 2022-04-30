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

import com.anchorfree.partner.api.auth.AuthMethod;
import com.anchorfree.partner.api.data.Country;
import com.anchorfree.partner.api.response.AvailableCountries;
import com.anchorfree.partner.api.response.User;
import com.anchorfree.reporting.TrackingConstants;
import com.anchorfree.sdk.SessionConfig;
import com.anchorfree.sdk.SessionInfo;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.sdk.exceptions.PartnerApiException;
import com.anchorfree.sdk.fireshield.FireshieldCategory;
import com.anchorfree.sdk.fireshield.FireshieldConfig;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.callbacks.CompletableCallback;
import com.anchorfree.vpnsdk.callbacks.TrafficListener;
import com.anchorfree.vpnsdk.callbacks.VpnStateListener;
import com.anchorfree.vpnsdk.compat.CredentialsCompat;
import com.anchorfree.vpnsdk.exceptions.NetworkRelatedException;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.anchorfree.vpnsdk.exceptions.VpnPermissionDeniedException;
import com.anchorfree.vpnsdk.exceptions.VpnPermissionRevokedException;
import com.anchorfree.vpnsdk.transporthydra.HydraTransport;
import com.anchorfree.vpnsdk.transporthydra.HydraVpnTransportException;
import com.anchorfree.vpnsdk.vpnservice.VPNState;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.github.goldfish07.hydra.vpn.BuildConfig;
import com.github.goldfish07.hydra.vpn.MainApplication;
import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.fragment.FreeServersFragment;
import com.github.goldfish07.hydra.vpn.fragment.PaidServerFragment;
import com.github.goldfish07.hydra.vpn.util.CountriesNames;
import com.northghost.caketube.CaketubeTransport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends AppCompatActivity implements TrafficListener, VpnStateListener {

    private String selectedCountry = "";

    private static FreeServerListListener freeServerListListener;
    private static PaidServerListListener paidServerListListener;

    public MainApplication myData = MainApplication.getInstance();
    public static ArrayList freeCountries;
    public static ArrayList paidCountries;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tablayout)
    SegmentTabLayout tabLayout;

    public interface FreeServerListListener {
        void onGotFreeServers(List<Country> list);

        void onServersLoding();
    }

    public interface PaidServerListListener {
        void onGotPaidServers(List<Country> list);

        void onServersLoding();
    }
    @BindView(R.id.connectBtn)
    Button connectBtn;

    @BindView(R.id.imgFlag)
    ImageView flag;

    @BindView(R.id.txtstatus)
    TextView txtstatus;

    @BindView(R.id.serverConnectingProgress)
    MaterialProgressBar serverConnectingProgress;

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String[] mTitles = {"Free Server", "Premium Server"};

    Map<String, String> localeCountries;

    boolean isconnected = false;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        UnifiedSDK.addTrafficListener(this);
        UnifiedSDK.addVpnStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UnifiedSDK.removeVpnStateListener(this);
        UnifiedSDK.removeTrafficListener(this);
    }

    private void loadServers() {
        if (freeServerListListener != null) {
            freeServerListListener.onServersLoding();
        }
        if (paidServerListListener != null) {
            paidServerListListener.onServersLoding();
        }
        // showProgress();
        UnifiedSDK.getInstance().getBackend().countries(new Callback<AvailableCountries>() {
            @Override
            public void success(@NonNull final AvailableCountries countries) {
                // hideProress();
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
        return UnifiedSDK.getInstance().getBackend().isLoggedIn();
    }

    protected void loginToVpn() {
        AuthMethod authMethod = AuthMethod.anonymous();
        UnifiedSDK.getInstance().getBackend().login(authMethod, new Callback<User>() {
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
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {
                callback.success(vpnState == VPNState.CONNECTED);
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
            fallbackOrder.add(CaketubeTransport.TRANSPORT_ID_TCP);
            fallbackOrder.add(CaketubeTransport.TRANSPORT_ID_UDP);
            //  showConnectProgress();
            List<String> bypassDomains = new LinkedList<>();
            bypassDomains.add("*facebook.com");
            bypassDomains.add("*wtfismyip.com");
            UnifiedSDK.getInstance().getVPN().start(new SessionConfig.Builder()
                    .withReason(TrackingConstants.GprReasons.M_UI)
                    .withTransportFallback(fallbackOrder)
                    .withTransport(HydraTransport.TRANSPORT_ID)
                    .withVirtualLocation(connectToBestServer())
                    .withFireshieldConfig(createFireshieldConfig())
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

    FireshieldConfig createFireshieldConfig() {
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
            UnifiedSDK.getVpnState(new Callback<VPNState>() {
                @Override
                public void success(@NonNull VPNState vpnState) {
                    if (vpnState == VPNState.CONNECTED) {
                      disconnectFromVnp();
                    } else if (vpnState == VPNState.IDLE) {
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
        UnifiedSDK.getInstance().getVPN().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
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
    public void vpnStateChanged(@NonNull VPNState vpnState) {
        switch (vpnState) {
            case CONNECTED:
                UnifiedSDK.getStatus(new Callback<SessionInfo>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void success(@NonNull SessionInfo sessionInfo) {
                        serverConnectingProgress.setVisibility(View.GONE);
                        flag.setImageDrawable(getDrawableFromAssets(sessionInfo.getSessionConfig().getVirtualLocation().toLowerCase()));
                        txtstatus.setText(getString(R.string.connected_to) + " " + localeCountries.get(sessionInfo.getSessionConfig().getVirtualLocation().toUpperCase()));
                    }
                    @Override
                    public void failure(@NonNull VpnException e) {

                    }
                });
                serverConnectingProgress.setVisibility(View.GONE);
                connectBtn.setText(getString(R.string.disconnect));
                isconnected = true;
                /*wwweif (!current_server.isEmpty()) {
                    flag.setImageDrawable(getDrawableFromAssets(current_server));
                }*/
                break;
            case DISCONNECTING:
                connectBtn.setText(getString(R.string.disconnecting));
                txtstatus.setText(getString(R.string.disconnecting));
                break;
            case IDLE:
                flag.setImageResource(R.drawable.default_flag);
                serverConnectingProgress.setVisibility(View.GONE);
                connectBtn.setText(getString(R.string.connect));
                txtstatus.setText(getString(R.string.not_connected));
                isconnected = false;
                break;
            case CONNECTING_PERMISSIONS:
            case CONNECTING_CREDENTIALS:
            case CONNECTING_VPN:
                serverConnectingProgress.setVisibility(View.VISIBLE);
                connectBtn.setText(getString(R.string.disconnect));
                txtstatus.setText(R.string.connecting);
                break;
        }
    }




    protected void getCurrentServer(final Callback<String> callback) {
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState state) {
                if (state == VPNState.CONNECTED) {
                    UnifiedSDK.getStatus(new Callback<SessionInfo>() {
                        @Override
                        public void success(@NonNull SessionInfo sessionInfo) {
                            callback.success(CredentialsCompat.getServerCountry(sessionInfo.getCredentials()));
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

        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState state) {
                if (state == VPNState.CONNECTED) {
                    showMessage("Reconnecting to VPN with " + selectedCountry);
                    UnifiedSDK.getInstance().getVPN().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
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

    // Example of error handling
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

    private Drawable getDrawableFromAssets(String str) {
        if (str != null) {
            try {
                AssetManager assets = this.getAssets();
                return Drawable.createFromStream(assets.open(str.toLowerCase() + ".png"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.getResources().getDrawable(R.drawable.default_flag);
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
