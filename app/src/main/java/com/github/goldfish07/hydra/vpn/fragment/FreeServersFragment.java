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

;

import com.anchorfree.partner.api.data.Country;
import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.activity.MainActivity;
import com.github.goldfish07.hydra.vpn.adapter.FreeServerListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FreeServersFragment extends Fragment  {

    private FreeServerListAdapter regionAdapter;

    @BindView(R.id.regions_progress)
    ProgressBar regionsProgressBar;

    @BindView(R.id.regions_recycler_view)
    RecyclerView regionsRecyclerView;


    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.free_servers_fragment, viewGroup, false);
        defineIds(inflate);
        return inflate;
    }


    private void defineIds(View view) {
        ButterKnife.bind(this,view);
        this.regionsRecyclerView.setHasFixedSize(true);
        this.regionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.regionAdapter = new FreeServerListAdapter(getContext());
        this.regionsRecyclerView.setAdapter(this.regionAdapter);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (MainActivity.freeCountries != null) {
            this.regionAdapter.setRegions(MainActivity.freeCountries);
            this.regionsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showProgress();
        }
        MainActivity.setFreeServerListListener(this.freeServerListListener);
    }

    private MainActivity.FreeServerListListener freeServerListListener = new MainActivity.FreeServerListListener() {

        @Override
        public void onGotFreeServers(List<Country> list) {
            FreeServersFragment.this.hideProress();
            FreeServersFragment.this.regionAdapter.setRegions(list);
        }

        @Override
        public void onServersLoding() {
            FreeServersFragment.this.showProgress();
        }
    };


    private void showProgress() {
        this.regionsProgressBar.setVisibility(View.VISIBLE);
        this.regionsRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProress() {
        this.regionsProgressBar.setVisibility(View.GONE);
        this.regionsRecyclerView.setVisibility(View.VISIBLE);
    }

}
