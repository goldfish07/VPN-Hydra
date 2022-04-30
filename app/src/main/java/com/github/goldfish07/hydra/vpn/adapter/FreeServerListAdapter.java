package com.github.goldfish07.hydra.vpn.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.anchorfree.partner.api.data.Country;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.anchorfree.vpnsdk.vpnservice.VPNState;
import com.github.goldfish07.hydra.vpn.BuildConfig;
import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.ServerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FreeServerListAdapter extends RecyclerView.Adapter<FreeServerListAdapter.ViewHolder> {
    private Context context;
    private List<Country> regions;

    public FreeServerListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_free_server, viewGroup, false));
    }

    @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        String country = this.regions.get(i).getCountry();

        Drawable drawableFromAssets = getDrawableFromAssets(country);
        if (this.regions.get(i).getCountry() != null) {
            viewHolder.regionTitle.setText(getCountryName(country));
            viewHolder.imgNetwork.setVisibility(View.VISIBLE);
        }
        viewHolder.imgFlag.setImageDrawable(drawableFromAssets);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(view.getContext(), String.valueOf(FreeServerListAdapter.this.regions.get(viewHolder.getAdapterPosition())), Toast.LENGTH_LONG).show();
                }
                UnifiedSDK.getVpnState(new Callback<VPNState>() {
                    @Override
                    public void success(@NonNull VPNState vpnState) {
                        if (vpnState == VPNState.CONNECTING_VPN||vpnState==VPNState.CONNECTING_CREDENTIALS||vpnState== VPNState.CONNECTING_PERMISSIONS) {
                            Toast.makeText(context, "Please wait while we connecting...", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(view.getContext(), ServerActivity.class);
                            intent.putExtra("country", String.valueOf(FreeServerListAdapter.this.regions.get(viewHolder.getAdapterPosition()).getCountry()));
                            view.getContext().startActivity(intent);
                        }
                    }

                    @Override
                    public void failure(@NonNull VpnException e) {

                    }


                });

            }
        });
        if (viewHolder.getAdapterPosition() == 0) {
            viewHolder.itemView.setVisibility(View.GONE);
            viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

    }


    public int getItemCount() {
        if (this.regions != null) {
            return this.regions.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFlag;
        ImageView imgNetwork;
        TextView regionTitle;
        ImageView connecting;
        ImageView connected;

        public ViewHolder(View view) {
            super(view);
            this.imgFlag = view.findViewById(R.id.imageFlag);
            this.imgNetwork = view.findViewById(R.id.imageConnect);
            this.regionTitle = view.findViewById(R.id.textCountry);
        }
    }

    public void setRegions(List<Country> list) {
        this.regions = new ArrayList();
       // this.regions.add(new Country());
        this.regions.addAll(list);
        notifyDataSetChanged();
    }

    private String getCountryName(String str) {
        return new Locale("", str.toUpperCase()).getDisplayCountry();
    }

    private Drawable getDrawableFromAssets(String str) {
        if (str != null) {
            try {
                AssetManager assets = this.context.getAssets();
                return Drawable.createFromStream(assets.open(str.toLowerCase() + ".png"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.context.getResources().getDrawable(R.drawable.default_flag);
    }

}
