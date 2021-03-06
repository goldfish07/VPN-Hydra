package com.github.goldfish07.hydra.vpn.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import com.github.goldfish07.hydra.vpn.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CountriesNames {

    public static Map<String, String> getCountries() {
        Map<String, String> countries = new HashMap<>();
        String[] isoCountries = Locale.getISOCountries();
        for (String country : isoCountries) {
            Locale locale = new Locale("", country);
            String iso = locale.getISO3Country();
            String code = locale.getCountry();
            String name = locale.getDisplayCountry();
            if (!"".equals(iso) && !"".equals(code)
                    && !"".equals(name)) {
                countries.put(code, name);
            }
        }
        return countries;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawableFromAssets(Context context, String str) {
        if (str != null) {
            try {
                AssetManager assets = context.getAssets();
                return Drawable.createFromStream(assets.open(str.toLowerCase() + ".png"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return context.getResources().getDrawable(R.drawable.unknown);
    }

    public static String getCountryName(String str) {
        return new Locale("", str.toUpperCase()).getDisplayCountry();
    }
}
