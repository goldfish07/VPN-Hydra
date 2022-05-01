package com.github.goldfish07.hydra.vpn.adapter;

import static com.github.goldfish07.hydra.vpn.util.CountriesNames.getDrawableFromAssets;

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



import com.github.goldfish07.hydra.vpn.BuildConfig;
import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.ServerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import unified.vpn.sdk.Callback;
import unified.vpn.sdk.Country;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnState;

public class FreeServerListAdapter extends RecyclerView.Adapter<FreeServerListAdapter.ViewHolder> {
    private final Context context;
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

        Drawable drawableFromAssets = getDrawableFromAssets(context,country);
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
                UnifiedSdk.getVpnState(new Callback<VpnState>() {
                    @Override
                    public void success(@NonNull VpnState vpnState) {
                        if (vpnState == VpnState.CONNECTING_VPN||vpnState==VpnState.CONNECTING_CREDENTIALS||vpnState== VpnState.CONNECTING_PERMISSIONS) {
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
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

    @SuppressLint("NotifyDataSetChanged")
    public void setRegions(List<Country> list) {
        this.regions = new ArrayList();
        this.regions.addAll(list);
        notifyDataSetChanged();
    }

    private String getCountryName(String str) {
        return new Locale("", str.toUpperCase()).getDisplayCountry();
    }


}
