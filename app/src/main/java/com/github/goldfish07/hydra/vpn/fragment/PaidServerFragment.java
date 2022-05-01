package com.github.goldfish07.hydra.vpn.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.goldfish07.hydra.vpn.R;
import com.github.goldfish07.hydra.vpn.activity.MainActivity;
import com.github.goldfish07.hydra.vpn.adapter.PaidServerListAdapter;

import java.util.List;

import unified.vpn.sdk.Country;

public class PaidServerFragment extends Fragment {
    private PaidServerListAdapter regionAdapter;
    private ProgressBar regionsProgressBar;
    private RecyclerView regionsRecyclerView;
    private AppCompatActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(activity==null){
            activity= (AppCompatActivity) getActivity();
        }
    }

    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_free_server, viewGroup, false);
        regionsProgressBar = inflate.findViewById(R.id.regions_progress);
        regionsRecyclerView = inflate.findViewById(R.id.recycler_view);
        this.regionsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.regionAdapter = new PaidServerListAdapter(getContext());
        this.regionsRecyclerView.setAdapter(this.regionAdapter);
        MainActivity mainActivity = (MainActivity) activity;
        if (MainActivity.paidCountries != null) {
            this.regionAdapter.setRegions(mainActivity.paidCountries);
            this.regionsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            showProgress();
        }
        MainActivity.setPaidServerListListener(this.paidServerListListener);
        return inflate;
    }


    private final MainActivity.PaidServerListListener paidServerListListener =
            new MainActivity.PaidServerListListener() {
        @Override
        public void onGotPaidServers(List<Country> list) {
            PaidServerFragment.this.hideProgress();
            PaidServerFragment.this.regionAdapter.setRegions(list);
        }

        @Override
        public void onServersLoading() {
            PaidServerFragment.this.showProgress();
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
