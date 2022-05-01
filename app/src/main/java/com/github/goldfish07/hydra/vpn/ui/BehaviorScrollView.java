package com.github.goldfish07.hydra.vpn.ui;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


@CoordinatorLayout.DefaultBehavior(MoveUpBehavior.class)
public class BehaviorScrollView extends ScrollView {
    public BehaviorScrollView(Context context) {
        super(context);
    }

    public BehaviorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BehaviorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BehaviorScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
