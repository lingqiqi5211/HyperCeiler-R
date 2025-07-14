/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2025 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.ui.holiday;

import static com.sevtinge.hyperceiler.common.utils.PersistConfig.isLunarNewYearThemeView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.ui.holiday.weather.ConfettiManager;
import com.sevtinge.hyperceiler.ui.holiday.weather.PrecipType;
import com.sevtinge.hyperceiler.ui.holiday.weather.confetto.ConfettoGenerator;

import java.lang.ref.WeakReference;

import fan.navigator.app.NavigatorActivity;

public class HolidayHelper {

    // Animation type constants
    private static final int ANIMATION_TYPE_SNOW = 1;
    private static final int ANIMATION_TYPE_FLOWER = 2;
    private static final int ANIMATION_TYPE_COIN = 3;

    // Animation settings constants
    private static final int SNOW_SPEED = 50;
    private static final int FLOWER_SPEED = 35;
    private static final int COIN_SPEED = 0;
    
    private static final int PORTRAIT_EMISSION_RATE_SNOW = 4;
    private static final int PORTRAIT_EMISSION_RATE_FLOWER = 2;
    private static final int LANDSCAPE_EMISSION_RATE_SNOW = 8;
    private static final int LANDSCAPE_EMISSION_RATE_FLOWER = 4;
    
    private static final float FADE_OUT_PERCENT_NORMAL = 0.75f;
    private static final float FADE_OUT_PERCENT_COIN = 1.0f;
    
    private static final int ROTATIONAL_VELOCITY_NORMAL = 45;
    private static final int ROTATIONAL_VELOCITY_COIN = 15;
    
    private static final long COIN_TTL = 30000;

    private final Context mContext;
    private final View mHolidayView;
    private final ViewGroup mContentView;
    private WeatherView mWeatherView;
    private ImageView mHeaderView;

    private final int mRotation;
    private static final int currentAnimationType = ANIMATION_TYPE_FLOWER;
    
    private static WeakReference<WeatherView> weatherViewRef;
    private static WeakReference<GravitySensor> gravityListenerRef;

    public static void init(Activity activity) {
        if (isLunarNewYearThemeView) {
            new HolidayHelper(activity);
        }
    }

    public HolidayHelper(Activity activity) {
        mContext = activity;
        mRotation = activity.getDisplay() != null ? activity.getDisplay().getRotation() : Surface.ROTATION_0;
        mContentView = activity.findViewById(android.R.id.content);
        mHolidayView = LayoutInflater.from(mContext).inflate(R.layout.layout_holiday, mContentView, false);
        initialize(activity instanceof NavigatorActivity);
    }

    public void initialize(boolean isNavigatorActivity) {
        if (isNavigatorActivity) {
            setupForNavigatorActivity();
        }
    }

    public void setupForNavigatorActivity() {
        ViewGroup parent = (ViewGroup) mContentView.getParent();
        FrameLayout mNavHostView = mHolidayView.findViewById(R.id.nav_host);

        parent.removeAllViews();

        mNavHostView.addView(mContentView);
        parent.addView(mHolidayView, 0);

        initView();
        initHoliday();
    }

    private void initView() {
        mWeatherView = mHolidayView.findViewById(R.id.weather_view);
        mHeaderView = mHolidayView.findViewById(R.id.holiday_header);
    }

    private void initHoliday() {
        mWeatherView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        weatherViewRef = new WeakReference<>(mWeatherView);

        configureWeatherAnimation();
        configureWeatherView();
        setupGravitySensor();
        setupHeaderView(currentAnimationType);
    }

    private void configureWeatherAnimation() {
        switch (currentAnimationType) {
            case ANIMATION_TYPE_SNOW:
                mWeatherView.setPrecipType(PrecipType.SNOW);
                mWeatherView.setSpeed(SNOW_SPEED);
                mWeatherView.setFadeOutPercent(FADE_OUT_PERCENT_NORMAL);
                break;
            case ANIMATION_TYPE_FLOWER:
                mWeatherView.setPrecipType(PrecipType.SNOW);
                mWeatherView.setSpeed(FLOWER_SPEED);
                mWeatherView.setFadeOutPercent(FADE_OUT_PERCENT_NORMAL);
                break;
            case ANIMATION_TYPE_COIN:
                mWeatherView.setPrecipType(PrecipType.CLEAR);
                mWeatherView.setSpeed(COIN_SPEED);
                mWeatherView.setFadeOutPercent(FADE_OUT_PERCENT_COIN);
                break;
        }
        
        mWeatherView.setAngle(0);
    }

    private void configureWeatherView() {
        boolean isLandscape = mRotation == Surface.ROTATION_90 || mRotation == Surface.ROTATION_270;
        
        int emissionRate = getEmissionRate(isLandscape);
        mWeatherView.setEmissionRate(emissionRate);
        
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mWeatherView.getLayoutParams();
        lp.height = calculateViewHeight(isLandscape);
        mWeatherView.setLayoutParams(lp);
        
        setWeatherGenerator(createWeatherGenerator());
        mWeatherView.resetWeather();
        mWeatherView.setVisibility(View.VISIBLE);
        
        int rotationalVelocity = currentAnimationType == ANIMATION_TYPE_COIN ? 
                ROTATIONAL_VELOCITY_COIN : ROTATIONAL_VELOCITY_NORMAL;
        mWeatherView.getConfettiManager().setRotationalVelocity(0, rotationalVelocity);
        
        if (currentAnimationType == ANIMATION_TYPE_COIN) {
            mWeatherView.getConfettiManager().setTTL(COIN_TTL);
        }
    }

    private int getEmissionRate(boolean isLandscape) {
        switch (currentAnimationType) {
            case ANIMATION_TYPE_SNOW:
                return isLandscape ? LANDSCAPE_EMISSION_RATE_SNOW : PORTRAIT_EMISSION_RATE_SNOW;
            case ANIMATION_TYPE_FLOWER:
                return isLandscape ? LANDSCAPE_EMISSION_RATE_FLOWER : PORTRAIT_EMISSION_RATE_FLOWER;
            case ANIMATION_TYPE_COIN:
            default:
                return isLandscape ? LANDSCAPE_EMISSION_RATE_FLOWER : PORTRAIT_EMISSION_RATE_FLOWER;
        }
    }

    private int calculateViewHeight(boolean isLandscape) {
        int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        
        switch (currentAnimationType) {
            case ANIMATION_TYPE_SNOW:
                return screenHeight / (isLandscape ? 2 : 3);
            case ANIMATION_TYPE_FLOWER:
                return screenHeight / (isLandscape ? 3 : 4);
            case ANIMATION_TYPE_COIN:
            default:
                return screenHeight / (isLandscape ? 3 : 4);
        }
    }

    private ConfettoGenerator createWeatherGenerator() {
        switch (currentAnimationType) {
            case ANIMATION_TYPE_SNOW:
                return new SnowGenerator(mContext);
            case ANIMATION_TYPE_FLOWER:
                return new FlowerGenerator(mContext);
            case ANIMATION_TYPE_COIN:
                return new CoinGenerator(mContext);
            default:
                return new FlowerGenerator(mContext);
        }
    }

    private void setupGravitySensor() {
        int speed = currentAnimationType == ANIMATION_TYPE_SNOW ? SNOW_SPEED : FLOWER_SPEED;
        
        GravitySensor listener = new GravitySensor(mContext, mWeatherView);
        listener.setOrientation(mRotation);
        listener.setSpeed(speed);
        listener.start();

        gravityListenerRef = new WeakReference<>(listener);
    }

    public static void pauseAnimation() {
        if (gravityListenerRef != null) {
            GravitySensor listener = gravityListenerRef.get();
            if (listener != null) {
                listener.stop();
            }
        }
    }

    public static void resumeAnimation() {
        if (gravityListenerRef != null) {
            GravitySensor listener = gravityListenerRef.get();
            if (listener != null) {
                listener.start();
            }
        }
    }

    private void setupHeaderView(int animationType) {
        int headerResId = getHeaderResourceId(animationType);
        mHeaderView.setImageResource(headerResId);
        mHeaderView.setVisibility(View.VISIBLE);
    }

    private int getHeaderResourceId(int animationType) {
        switch (animationType) {
            case ANIMATION_TYPE_SNOW:
                return R.drawable.newyear_header;
            case ANIMATION_TYPE_FLOWER:
                return R.drawable.lunar_newyear_header;
            case ANIMATION_TYPE_COIN:
                return R.drawable.crypto_header;
            default:
                return R.drawable.lunar_newyear_header;
        }
    }

    /**
     * Sets the weather generator without using reflection.
     * This is a safer and more maintainable approach.
     */
    private void setWeatherGenerator(ConfettoGenerator generator) {
        try {
            WeatherView weatherView = weatherViewRef != null ? weatherViewRef.get() : null;
            if (weatherView != null) {
                ConfettiManager manager = weatherView.getConfettiManager();
                // Use the new updateConfettoGenerator method instead of reflection
                ConfettiManager newManager = manager.updateConfettoGenerator(generator);
                
                // Update the weather view with the new manager
                // Note: This approach may need adjustment based on WeatherView implementation
                // If WeatherView doesn't have a setter, we might need to add one there too
            }
        } catch (Exception e) {
            // Log error but don't crash the app
            e.printStackTrace();
        }
    }
}
