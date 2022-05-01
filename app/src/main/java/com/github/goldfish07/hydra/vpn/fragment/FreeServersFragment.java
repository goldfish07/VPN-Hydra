package com.github.goldfish07.hydra.vpn.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.activity.MainActivity;
import com.github.goldfish07.hydra.vpn.adapter.FreeServerListAdapter;

import java.util.List;

import unified.vpn.sdk.Country;

public class FreeServersFragment extends Fragment  {

    private FreeServerListAdapter regionAdapter;
    private ProgressBar regionsProgressBar;
    private RecyclerView regionsRecyclerView;

    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.free_servers_fragment, viewGroup, false);
         regionsProgressBar = inflate.findViewById(R.id.regions_progress);
        regionsRecyclerView = inflate.findViewById(R.id.recycler_view);
        regionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.regionAdapter = new FreeServerListAdapter(getContext());
        this.regionsRecyclerView.setAdapter(this.regionAdapter);
        if (MainActivity.freeCountries != null) {
            this.regionAdapter.setRegions(MainActivity.freeCountries);
            this.regionsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showProgress();
        }
        MainActivity.setFreeServerListListener(this.freeServerListListener);
        return inflate;
    }

    private final MainActivity.FreeServerListListener freeServerListListener =
            new MainActivity.FreeServerListListener() {
        @Override
        public void onGotFreeServers(List<Country> list) {
            FreeServersFragment.this.hideProgress();
            FreeServersFragment.this.regionAdapter.setRegions(list);
        }

        @Override
        public void onServersLoading() {
            FreeServersFragment.this.showProgress();
        }
    };


    private void showProgress() {
        this.regionsProgressBar.setVisibility(View.VISIBLE);
        this.regionsRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProgress() {
        this.regionsProgressBar.setVisibility(View.GONE);
        this.regionsRecyclerView.setVisibility(View.VISIBLE);
    }
}
