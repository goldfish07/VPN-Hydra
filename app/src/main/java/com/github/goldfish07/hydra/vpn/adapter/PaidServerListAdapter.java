package com.github.goldfish07.hydra.vpn.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.github.goldfish07.hydra.vpn.util.CountriesNames;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaidServerListAdapter extends RecyclerView.Adapter<PaidServerListAdapter.ViewHolder> {

    private final Context context;
    private final List<Country> regions;

    public PaidServerListAdapter(Context context) {
        this.context = context;
        this.regions = new ArrayList();
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_paid_server, viewGroup, false));
    }

    @SuppressLint("SetTextI18n")
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        String country = this.regions.get(i).getCountry();

        Drawable flag = CountriesNames.getDrawableFromAssets(country,context);
        if (this.regions.get(i).getCountry() != null) {
            viewHolder.regionTitle.setText(getCountryName(country));
            viewHolder.imgNetwork.setVisibility(View.VISIBLE);
        }
        viewHolder.imgFlag.setImageDrawable(flag);
        viewHolder.itemView.setOnClickListener(view -> {
            if (BuildConfig.DEBUG) {
                Toast.makeText(view.getContext(), String.valueOf(PaidServerListAdapter.this.regions.get(viewHolder.getAdapterPosition())), Toast.LENGTH_LONG).show();
            }
            UnifiedSDK.getVpnState(new Callback<VPNState>() {
                @Override
                public void success(@NonNull VPNState vpnState) {
                    if (vpnState == VPNState.CONNECTING_VPN||vpnState==VPNState.CONNECTING_CREDENTIALS||vpnState== VPNState.CONNECTING_PERMISSIONS) {
                        Toast.makeText(context, "Please wait while we connecting...", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(view.getContext(), ServerActivity.class);
                        intent.putExtra("country", String.valueOf(PaidServerListAdapter.this.regions.get(viewHolder.getAdapterPosition()).getCountry()));
                        view.getContext().startActivity(intent);
                    }
                }

                @Override
                public void failure(@NonNull VpnException e) {

                }
            });

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

        public ViewHolder(View view) {
            super(view);
            this.imgFlag = view.findViewById(R.id.imageFlag);
            this.imgNetwork = view.findViewById(R.id.imageConnect);
            this.regionTitle = view.findViewById(R.id.textCountry);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRegions(List<Country> list) {
        this.regions.addAll(list);
        notifyDataSetChanged();
    }

    private String getCountryName(String str) {
        return new Locale("", str.toUpperCase()).getDisplayCountry();
    }

}
