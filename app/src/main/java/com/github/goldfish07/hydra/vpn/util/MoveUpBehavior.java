package com.github.goldfish07.hydra.vpn.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;


@CoordinatorLayout.DefaultBehavior(MoveUpBehavior.class)
public class MoveUpBehavior extends CoordinatorLayout.Behavior<View> {
    private static final boolean SNACKBAR_BEHAVIOR_ENABLED;

    static {
        SNACKBAR_BEHAVIOR_ENABLED = true;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }


}
