package com.example.pointmap;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import android.util.Log;

import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener;
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener;
import com.baidu.mapapi.walknavi.adapter.IWTTSPlayer;
import com.baidu.mapapi.walknavi.model.RouteGuideKind;
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener;
import com.simple.spiderman.SpiderMan;

public class WNaviGuideActivity extends Activity {

    private final static String TAG = WNaviGuideActivity.class.getSimpleName();
    private WalkNavigateHelper mNaviHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNaviHelper = WalkNavigateHelper.getInstance();
        try {
            View view = mNaviHelper.onCreate(WNaviGuideActivity.this);
            if (view != null) {
                setContentView(view);
            }
        }catch(Exception e){
            SpiderMan.show(e);
        }
        mNaviHelper.setWalkNaviStatusListener(new IWNaviStatusListener() {
            @Override
            public void onWalkNaviModeChange(int mode, WalkNaviModeSwitchListener listener) {
                Log.d(TAG, "onWalkNaviModeChange : " + mode);
                mNaviHelper.switchWalkNaviMode(WNaviGuideActivity.this, mode, listener);
            }
            @Override
            public void onNaviExit() {
                Log.d(TAG, "onNaviExit");
            }
        });
        // TODO 语音导航
        mNaviHelper.setTTsPlayer(new IWTTSPlayer() {
            @Override
            public int playTTSText(final String s, boolean b) {
                Log.d(TAG, "tts: " + s);
                return 0;
            }
        });
        boolean startResult = mNaviHelper.startWalkNavi(WNaviGuideActivity.this);
//        mNaviHelper.setRouteGuidanceListener(this, new IWRouteGuidanceListener() {
//            @Override
//            public void onRouteGuideIconUpdate(Drawable icon) {
//
//            }
//
//            @Override
//            public void onRouteGuideKind(RouteGuideKind routeGuideKind) {
//                Log.d(TAG, "onRouteGuideKind: " + routeGuideKind);
//            }
//
//            @Override
//            public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence charSequence1) {
//                Log.d(TAG, "onRoadGuideTextUpdate   charSequence=: " + charSequence + "   charSequence1 = : " +
//                        charSequence1);
//            }
//
//            @Override
//            public void onRemainDistanceUpdate(CharSequence charSequence) {
//                Log.d(TAG, "onRemainDistanceUpdate: charSequence = :" + charSequence);
//            }
//
//            @Override
//            public void onRemainTimeUpdate(CharSequence charSequence) {
//                Log.d(TAG, "onRemainTimeUpdate: charSequence = :" + charSequence);
//            }
//
//            @Override
//            public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {
//                Log.d(TAG, "onGpsStatusChange: charSequence = :" + charSequence);
//            }
//
//            @Override
//            public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {
//                Log.d(TAG, "onRouteFarAway: charSequence = :" + charSequence);
//            }
//
//            @Override
//            public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {
//                Log.d(TAG, "onRoutePlanYawing: charSequence = :" + charSequence);
//            }
//
//            @Override
//            public void onReRouteComplete() {
//
//            }
//
//            @Override
//            public void onArriveDest() {
//
//            }
//            @Override
//            public void onIndoorEnd(Message msg) {
//
//            }
//
//            @Override
//
//            public void onFinalEnd(Message msg) {
//
//            }
//
//            @Override
//            public void onVibrate() {
//
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviHelper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviHelper.quit();
    }
}