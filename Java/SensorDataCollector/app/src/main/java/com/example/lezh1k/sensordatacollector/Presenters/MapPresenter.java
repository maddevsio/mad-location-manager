package com.example.lezh1k.sensordatacollector.Presenters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.example.lezh1k.sensordatacollector.Interfaces.MapInterface;
import com.example.lezh1k.sensordatacollector.Services.ServicesHelper;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lezh1k on 1/30/18.
 */

public class MapPresenter {
    private MapInterface mapInterface;
    private Context context;

    public MapPresenter(Context context, MapInterface mapInterface) {
        this.mapInterface = mapInterface;
        this.context = context;
    }

    public void onLocationChanged(Location location, CameraPosition currentCameraPosition) {
        CameraPosition.Builder position =
                new CameraPosition.Builder(currentCameraPosition).target(new LatLng(location));
        mapInterface.moveCamera(position.build());
        getRoute();
    }

    public void getRoute() {
        ServicesHelper.getLocationService(context, value -> {
            List<LatLng> route = new ArrayList<>();
            for (Location location : new ArrayList<>(value.getTrack())) {
                route.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            mapInterface.showRoute(route);
        });
    }

    public void share() {
        ServicesHelper.getLocationService(context, value -> {
            StringBuilder shareRoute = new StringBuilder();
            for (Location location : new ArrayList<>(value.getTrack())) {
                shareRoute.append(location.toString()).append("\n");
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareRoute.toString());
            sendIntent.setType("text/plain");
            context.startActivity(sendIntent);

            value.clearTrack();
        });
    }

    private List<Location> trackFromString(String track) {
        List<Location> route = new ArrayList<>();

        for (String point : track.split("\n")) {
            String[] data = point.split("Location\\[")[1].split(" ");
            String provider = data[0];
            String latitude = data[1].substring(0, data[1].indexOf(",", data[1].indexOf(",") + 1)).replace(",", ".");
            String longitude = data[1].substring(latitude.length() + 1).replace(",", ".");
            String accuracy = data[2];
            String elapsed = data[3];
            String altitude = data[4];
            String velocity = data[5];
            String bearing = data.length > 7 ? data[6] : "bear=0";

            Location location = new Location(provider);
            location.setLatitude(Double.valueOf(latitude));
            location.setLongitude(Double.valueOf(longitude));
            location.setAltitude(Double.valueOf(altitude.split("=")[1]));
            location.setAccuracy(Float.valueOf(accuracy.split("=")[1]));
            location.setBearing(Float.valueOf(bearing.split("=")[1]));

            Log.d("TrackFromString", provider + "|" + latitude + "|" + longitude + "|" + accuracy + "|" + elapsed + "|" + altitude + "|" + velocity + "|" + bearing);
            route.add(location);
        }


        return route;
    }

    public void test() {
        List<Location> track = trackFromString("Location[gps 42,879100,74,617678 acc=124 et=+4d3h30m31s542ms alt=700.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879099,74,617573 acc=13 et=+4d3h30m38s543ms alt=704.0 vel=3.06 bear=246.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879104,74,617654 acc=10 et=+4d3h30m40s540ms alt=701.0 vel=0.77 bear=246.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879085,74,617715 acc=8 et=+4d3h30m44s550ms alt=702.0 vel=2.01 bear=217.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879033,74,617733 acc=7 et=+4d3h30m50s550ms alt=706.0 vel=1.31 bear=186.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878987,74,617764 acc=5 et=+4d3h30m55s542ms alt=708.0 vel=0.38 bear=246.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878991,74,617847 acc=4 et=+4d3h31m1s545ms alt=707.0 vel=0.95 bear=97.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878994,74,617919 acc=4 et=+4d3h31m4s548ms alt=706.0 vel=1.1 bear=91.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878995,74,617999 acc=3 et=+4d3h31m8s547ms alt=707.0 vel=1.15 bear=92.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878994,74,618075 acc=4 et=+4d3h31m12s544ms alt=707.0 vel=1.42 bear=107.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878998,74,618140 acc=4 et=+4d3h31m16s544ms alt=706.0 vel=1.16 bear=88.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878990,74,618215 acc=4 et=+4d3h31m20s551ms alt=705.0 vel=1.48 bear=88.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878953,74,618264 acc=3 et=+4d3h31m25s544ms alt=705.0 vel=1.59 bear=127.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878945,74,618333 acc=3 et=+4d3h31m32s541ms alt=707.0 vel=1.32 bear=110.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878914,74,618380 acc=3 et=+4d3h32m13s545ms alt=709.0 vel=0.67 bear=195.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878924,74,618319 acc=5 et=+4d3h33m15s546ms alt=708.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878962,74,618248 acc=9 et=+4d3h33m31s545ms alt=726.0 vel=2.61 bear=270.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878971,74,618134 acc=3 et=+4d3h33m33s547ms alt=729.0 vel=2.54 bear=277.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878972,74,618024 acc=3 et=+4d3h33m35s552ms alt=730.0 vel=3.15 bear=268.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878965,74,617916 acc=3 et=+4d3h33m37s548ms alt=732.0 vel=3.32 bear=267.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878959,74,617823 acc=3 et=+4d3h33m39s555ms alt=734.0 vel=3.29 bear=274.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878972,74,617737 acc=3 et=+4d3h33m41s554ms alt=729.0 vel=2.77 bear=292.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878996,74,617671 acc=3 et=+4d3h33m43s551ms alt=728.0 vel=2.98 bear=313.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879029,74,617624 acc=4 et=+4d3h33m45s547ms alt=727.0 vel=1.96 bear=307.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879039,74,617532 acc=4 et=+4d3h33m49s578ms alt=726.0 vel=1.45 bear=259.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879060,74,617438 acc=7 et=+4d3h33m55s552ms alt=731.0 vel=3.49 bear=327.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879126,74,617419 acc=9 et=+4d3h33m57s539ms alt=732.0 vel=3.43 bear=337.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879356,74,617448 acc=7 et=+4d3h33m59s546ms alt=726.0 vel=9.26 bear=348.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879530,74,617415 acc=7 et=+4d3h34m1s544ms alt=725.0 vel=9.7 bear=353.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879721,74,617384 acc=9 et=+4d3h34m3s553ms alt=720.0 vel=10.05 bear=356.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879892,74,617397 acc=8 et=+4d3h34m5s551ms alt=718.0 vel=9.68 bear=357.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880063,74,617394 acc=7 et=+4d3h34m7s544ms alt=715.0 vel=9.58 bear=357.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880234,74,617384 acc=6 et=+4d3h34m9s548ms alt=716.0 vel=9.33 bear=357.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880327,74,617365 acc=5 et=+4d3h34m11s549ms alt=708.0 vel=6.2 bear=357.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880373,74,617358 acc=4 et=+4d3h34m13s553ms alt=708.0 vel=2.7 bear=357.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880481,74,617371 acc=10 et=+4d3h34m45s546ms alt=707.0 vel=4.72 bear=358.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880580,74,617306 acc=5 et=+4d3h34m47s548ms alt=707.0 vel=3.86 bear=325.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880610,74,617246 acc=5 et=+4d3h34m49s547ms alt=708.0 vel=2.93 bear=281.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880604,74,617170 acc=4 et=+4d3h34m53s547ms alt=708.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880568,74,617129 acc=5 et=+4d3h34m57s569ms alt=707.0 vel=2.41 bear=195.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880519,74,617119 acc=6 et=+4d3h34m59s556ms alt=707.0 vel=2.79 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880450,74,617108 acc=9 et=+4d3h35m1s556ms alt=707.0 vel=4.36 bear=185.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,880147,74,617080 acc=10 et=+4d3h35m3s545ms alt=712.0 vel=9.7 bear=185.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879959,74,617124 acc=7 et=+4d3h35m5s547ms alt=709.0 vel=9.44 bear=179.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879763,74,617150 acc=5 et=+4d3h35m7s544ms alt=707.0 vel=10.88 bear=179.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879555,74,617165 acc=4 et=+4d3h35m10s499ms alt=707.0 vel=11.57 bear=178.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879358,74,617175 acc=4 et=+4d3h35m12s504ms alt=707.0 vel=11.18 bear=178.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,879194,74,617206 acc=5 et=+4d3h35m14s503ms alt=705.0 vel=11.46 bear=178.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878971,74,617214 acc=8 et=+4d3h35m16s520ms alt=706.0 vel=11.94 bear=178.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878821,74,617226 acc=8 et=+4d3h35m18s506ms alt=705.0 vel=11.93 bear=179.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878592,74,617232 acc=8 et=+4d3h35m20s512ms alt=704.0 vel=12.03 bear=179.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878386,74,617245 acc=7 et=+4d3h35m22s511ms alt=703.0 vel=11.73 bear=180.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,878162,74,617244 acc=8 et=+4d3h35m24s512ms alt=703.0 vel=11.65 bear=180.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,877976,74,617248 acc=6 et=+4d3h35m26s412ms alt=708.0 vel=10.45 bear=181.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,877768,74,617245 acc=9 et=+4d3h35m28s506ms alt=709.0 vel=10.91 bear=181.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,877496,74,617257 acc=7 et=+4d3h35m30s510ms alt=709.0 vel=12.58 bear=181.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,877266,74,617251 acc=5 et=+4d3h35m32s504ms alt=706.0 vel=12.54 bear=181.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,877004,74,617232 acc=3 et=+4d3h35m34s500ms alt=711.0 vel=13.06 bear=182.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,876744,74,617217 acc=3 et=+4d3h35m36s411ms alt=710.0 vel=13.71 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875959,74,617136 acc=8 et=+4d3h35m54s526ms alt=714.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875881,74,617144 acc=6 et=+4d3h36m6s514ms alt=716.0 vel=2.74 bear=184.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875806,74,617144 acc=5 et=+4d3h36m8s507ms alt=717.0 vel=3.57 bear=184.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875731,74,617138 acc=3 et=+4d3h36m10s503ms alt=715.0 vel=4.22 bear=183.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875642,74,617133 acc=3 et=+4d3h36m12s416ms alt=715.0 vel=4.91 bear=181.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875555,74,617142 acc=3 et=+4d3h36m14s518ms alt=717.0 vel=4.1 bear=167.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875500,74,617184 acc=3 et=+4d3h36m16s515ms alt=716.0 vel=2.22 bear=126.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875490,74,617247 acc=3 et=+4d3h36m18s502ms alt=715.0 vel=3.03 bear=101.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875490,74,617319 acc=3 et=+4d3h36m20s538ms alt=715.0 vel=1.6 bear=94.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875499,74,617389 acc=8 et=+4d3h36m30s503ms alt=715.0 vel=0.77 bear=91.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875505,74,617650 acc=7 et=+4d3h36m32s515ms alt=716.0 vel=6.17 bear=90.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875502,74,617919 acc=7 et=+4d3h36m34s512ms alt=716.0 vel=8.86 bear=91.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875495,74,618212 acc=7 et=+4d3h36m36s515ms alt=716.0 vel=10.78 bear=92.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875480,74,618531 acc=8 et=+4d3h36m38s494ms alt=716.0 vel=11.87 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875451,74,618908 acc=6 et=+4d3h36m40s509ms alt=717.0 vel=12.91 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875445,74,619260 acc=4 et=+4d3h36m42s419ms alt=717.0 vel=13.54 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875429,74,619591 acc=3 et=+4d3h36m44s510ms alt=719.0 vel=13.67 bear=93.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875412,74,619915 acc=3 et=+4d3h36m46s507ms alt=718.0 vel=13.08 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875400,74,620218 acc=3 et=+4d3h36m48s500ms alt=715.0 vel=12.31 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875397,74,620481 acc=3 et=+4d3h36m50s505ms alt=716.0 vel=11.65 bear=92.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875393,74,620760 acc=3 et=+4d3h36m52s507ms alt=717.0 vel=12.15 bear=92.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875389,74,621080 acc=4 et=+4d3h36m54s520ms alt=717.0 vel=13.4 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875390,74,621401 acc=4 et=+4d3h36m56s507ms alt=716.0 vel=14.14 bear=94.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875364,74,621684 acc=5 et=+4d3h36m58s522ms alt=718.0 vel=12.49 bear=96.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875343,74,621988 acc=7 et=+4d3h37m0s504ms alt=718.0 vel=12.47 bear=94.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875349,74,622234 acc=8 et=+4d3h37m2s514ms alt=717.0 vel=9.99 bear=93.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875334,74,622478 acc=7 et=+4d3h37m4s583ms alt=717.0 vel=8.66 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875317,74,622610 acc=6 et=+4d3h37m6s526ms alt=717.0 vel=5.18 bear=92.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875322,74,622672 acc=8 et=+4d3h37m8s502ms alt=715.0 vel=3.43 bear=92.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875293,74,622747 acc=7 et=+4d3h37m20s518ms alt=713.0 vel=2.72 bear=97.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875274,74,622830 acc=7 et=+4d3h37m22s524ms alt=713.0 vel=3.95 bear=97.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875252,74,622944 acc=7 et=+4d3h37m24s508ms alt=712.0 vel=4.8 bear=93.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875210,74,623101 acc=4 et=+4d3h37m26s523ms alt=714.0 vel=6.34 bear=92.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875215,74,623274 acc=4 et=+4d3h37m28s516ms alt=716.0 vel=7.57 bear=90.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875225,74,623492 acc=3 et=+4d3h37m30s505ms alt=718.0 vel=9.21 bear=90.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875230,74,623696 acc=3 et=+4d3h37m32s517ms alt=719.0 vel=8.74 bear=90.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875218,74,623952 acc=3 et=+4d3h37m34s526ms alt=718.0 vel=10.8 bear=92.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875207,74,624235 acc=4 et=+4d3h37m36s511ms alt=718.0 vel=11.67 bear=92.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875203,74,624561 acc=5 et=+4d3h37m38s508ms alt=719.0 vel=12.85 bear=93.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875151,74,625138 acc=5 et=+4d3h37m42s530ms alt=720.0 vel=10.72 bear=95.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875118,74,625353 acc=6 et=+4d3h37m44s542ms alt=719.0 vel=9.07 bear=94.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875092,74,625602 acc=6 et=+4d3h37m46s549ms alt=718.0 vel=9.85 bear=99.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875090,74,625821 acc=6 et=+4d3h37m48s590ms alt=719.0 vel=10.79 bear=99.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875066,74,626081 acc=8 et=+4d3h37m50s545ms alt=719.0 vel=10.71 bear=94.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875086,74,626295 acc=5 et=+4d3h37m52s561ms alt=720.0 vel=8.18 bear=94.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,875067,74,626444 acc=5 et=+4d3h37m54s556ms alt=719.0 vel=7.01 bear=124.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,874964,74,626482 acc=5 et=+4d3h37m56s532ms alt=716.0 vel=6.67 bear=162.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,874807,74,626506 acc=6 et=+4d3h37m58s559ms alt=719.0 vel=9.03 bear=164.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,874633,74,626551 acc=7 et=+4d3h38m0s534ms alt=720.0 vel=8.52 bear=164.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,874491,74,626613 acc=8 et=+4d3h38m2s556ms alt=721.0 vel=9.94 bear=165.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,874292,74,626663 acc=7 et=+4d3h38m4s556ms alt=719.0 vel=11.35 bear=165.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,874075,74,626746 acc=6 et=+4d3h38m6s557ms alt=718.0 vel=11.5 bear=165.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,873855,74,626816 acc=5 et=+4d3h38m8s551ms alt=719.0 vel=12.16 bear=166.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,873598,74,626893 acc=4 et=+4d3h38m10s551ms alt=722.0 vel=13.36 bear=168.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,873345,74,626956 acc=4 et=+4d3h38m12s545ms alt=722.0 vel=13.95 bear=172.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,873094,74,626994 acc=3 et=+4d3h38m14s551ms alt=721.0 vel=14.33 bear=174.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,872838,74,627024 acc=3 et=+4d3h38m16s554ms alt=722.0 vel=13.45 bear=176.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,872606,74,627034 acc=3 et=+4d3h38m18s552ms alt=722.0 vel=12.77 bear=180.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,872388,74,627033 acc=3 et=+4d3h38m20s558ms alt=721.0 vel=12.22 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,872170,74,627010 acc=4 et=+4d3h38m22s545ms alt=721.0 vel=12.0 bear=184.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871988,74,626989 acc=4 et=+4d3h38m24s467ms alt=720.0 vel=10.81 bear=185.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871806,74,626966 acc=4 et=+4d3h38m26s553ms alt=719.0 vel=10.11 bear=184.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871721,74,627020 acc=4 et=+4d3h38m28s559ms alt=720.0 vel=5.66 bear=148.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871665,74,627134 acc=4 et=+4d3h38m30s572ms alt=718.0 vel=5.45 bear=113.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871637,74,627272 acc=3 et=+4d3h38m32s554ms alt=717.0 vel=6.45 bear=110.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871588,74,627440 acc=3 et=+4d3h38m34s571ms alt=716.0 vel=7.1 bear=112.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871520,74,627603 acc=3 et=+4d3h38m36s552ms alt=716.0 vel=7.24 bear=118.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871455,74,627772 acc=3 et=+4d3h38m38s568ms alt=714.0 vel=6.88 bear=127.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871363,74,627871 acc=5 et=+4d3h38m40s558ms alt=714.0 vel=6.58 bear=141.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871265,74,627975 acc=5 et=+4d3h38m42s567ms alt=716.0 vel=6.28 bear=145.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871182,74,628075 acc=6 et=+4d3h38m44s557ms alt=716.0 vel=6.33 bear=135.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871097,74,628176 acc=7 et=+4d3h38m46s547ms alt=716.0 vel=6.27 bear=143.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,871014,74,628258 acc=6 et=+4d3h38m48s545ms alt=717.0 vel=6.1 bear=143.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,870943,74,628374 acc=7 et=+4d3h38m50s563ms alt=718.0 vel=6.47 bear=133.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,870329,74,629132 acc=4 et=+4d3h39m7s629ms alt=716.0 vel=5.21 bear=137.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,870260,74,629218 acc=6 et=+4d3h39m9s618ms alt=716.0 vel=5.21 bear=137.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,870172,74,629353 acc=4 et=+4d3h39m11s542ms alt=716.0 vel=6.5 bear=135.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,870091,74,629461 acc=3 et=+4d3h39m13s660ms alt=718.0 vel=6.14 bear=136.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,870026,74,629554 acc=4 et=+4d3h39m16s285ms alt=720.0 vel=5.93 bear=138.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869959,74,629659 acc=3 et=+4d3h39m18s285ms alt=724.0 vel=6.63 bear=134.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869856,74,629796 acc=3 et=+4d3h39m20s283ms alt=721.0 vel=7.14 bear=133.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869759,74,629919 acc=4 et=+4d3h39m22s286ms alt=720.0 vel=7.34 bear=136.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869670,74,630053 acc=4 et=+4d3h39m24s304ms alt=722.0 vel=7.23 bear=135.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869589,74,630166 acc=5 et=+4d3h39m26s305ms alt=721.0 vel=6.63 bear=135.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869498,74,630262 acc=6 et=+4d3h39m28s294ms alt=722.0 vel=6.12 bear=137.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869420,74,630338 acc=6 et=+4d3h39m30s299ms alt=724.0 vel=4.86 bear=136.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869373,74,630403 acc=7 et=+4d3h39m32s299ms alt=724.0 vel=3.04 bear=122.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869342,74,630458 acc=7 et=+4d3h39m34s294ms alt=724.0 vel=2.04 bear=124.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869362,74,630465 acc=3 et=+4d3h40m17s385ms alt=731.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869371,74,630476 acc=4 et=+4d4h20m37s561ms alt=730.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n");

        track.addAll(trackFromString("Location[gps 42,869265,74,630593 acc=3 et=+4d4h20m55s554ms alt=730.0 vel=5.19 bear=138.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869165,74,630731 acc=3 et=+4d4h20m57s605ms alt=730.0 vel=7.7 bear=134.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869042,74,630922 acc=3 et=+4d4h20m59s652ms alt=730.0 vel=10.05 bear=133.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868910,74,631090 acc=4 et=+4d4h21m1s567ms alt=731.0 vel=9.8 bear=133.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868787,74,631266 acc=4 et=+4d4h21m3s668ms alt=729.0 vel=9.29 bear=133.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868694,74,631425 acc=3 et=+4d4h21m5s669ms alt=732.0 vel=8.43 bear=133.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868600,74,631546 acc=3 et=+4d4h21m7s648ms alt=732.0 vel=8.32 bear=131.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868499,74,631692 acc=3 et=+4d4h21m9s654ms alt=731.0 vel=8.05 bear=132.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868413,74,631841 acc=3 et=+4d4h21m11s659ms alt=732.0 vel=7.34 bear=133.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868328,74,631976 acc=3 et=+4d4h21m13s666ms alt=734.0 vel=7.11 bear=132.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868246,74,632094 acc=3 et=+4d4h21m15s667ms alt=737.0 vel=6.92 bear=132.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868163,74,632228 acc=3 et=+4d4h21m17s666ms alt=737.0 vel=6.69 bear=131.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868076,74,632320 acc=3 et=+4d4h21m19s655ms alt=736.0 vel=5.86 bear=134.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868016,74,632401 acc=4 et=+4d4h21m21s651ms alt=737.0 vel=3.69 bear=132.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867976,74,632484 acc=4 et=+4d4h21m25s658ms alt=737.0 vel=1.33 bear=138.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867919,74,632565 acc=3 et=+4d4h21m29s663ms alt=738.0 vel=3.57 bear=131.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867861,74,632632 acc=3 et=+4d4h21m31s661ms alt=735.0 vel=5.15 bear=136.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867792,74,632751 acc=4 et=+4d4h21m33s652ms alt=731.0 vel=6.76 bear=134.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867724,74,632937 acc=4 et=+4d4h21m35s657ms alt=731.0 vel=9.0 bear=133.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867603,74,633070 acc=5 et=+4d4h21m37s657ms alt=730.0 vel=9.68 bear=133.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867478,74,633242 acc=4 et=+4d4h21m39s693ms alt=732.0 vel=9.48 bear=132.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867354,74,633417 acc=4 et=+4d4h21m41s668ms alt=733.0 vel=9.21 bear=131.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867246,74,633598 acc=3 et=+4d4h21m43s665ms alt=736.0 vel=8.27 bear=117.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867159,74,633828 acc=3 et=+4d4h21m45s650ms alt=738.0 vel=9.12 bear=101.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867146,74,634061 acc=3 et=+4d4h21m47s674ms alt=737.0 vel=10.65 bear=88.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867174,74,634320 acc=3 et=+4d4h21m49s660ms alt=735.0 vel=10.88 bear=83.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867230,74,634575 acc=4 et=+4d4h21m51s599ms alt=734.0 vel=10.37 bear=81.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867257,74,634826 acc=6 et=+4d4h21m53s646ms alt=734.0 vel=10.37 bear=81.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867259,74,635057 acc=4 et=+4d4h21m55s666ms alt=736.0 vel=9.73 bear=83.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867286,74,635245 acc=5 et=+4d4h21m57s663ms alt=735.0 vel=9.11 bear=82.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867299,74,635450 acc=5 et=+4d4h21m59s661ms alt=736.0 vel=7.22 bear=82.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867319,74,635597 acc=5 et=+4d4h22m1s665ms alt=738.0 vel=3.74 bear=76.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867363,74,635687 acc=3 et=+4d4h22m5s663ms alt=739.0 vel=2.59 bear=71.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867368,74,635760 acc=4 et=+4d4h22m7s684ms alt=739.0 vel=2.95 bear=76.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867377,74,635831 acc=6 et=+4d4h22m9s654ms alt=740.0 vel=2.94 bear=77.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867395,74,635961 acc=3 et=+4d4h22m11s664ms alt=736.0 vel=4.66 bear=68.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867450,74,636053 acc=3 et=+4d4h22m13s667ms alt=736.0 vel=5.65 bear=43.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867564,74,636106 acc=3 et=+4d4h22m15s675ms alt=734.0 vel=6.8 bear=3.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867689,74,636099 acc=3 et=+4d4h22m17s583ms alt=734.0 vel=8.04 bear=2.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867837,74,636097 acc=3 et=+4d4h22m19s563ms alt=734.0 vel=8.48 bear=1.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867981,74,636125 acc=3 et=+4d4h22m21s659ms alt=733.0 vel=9.73 bear=3.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868165,74,636147 acc=3 et=+4d4h22m23s661ms alt=732.0 vel=10.48 bear=4.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868346,74,636153 acc=3 et=+4d4h22m25s564ms alt=734.0 vel=10.92 bear=3.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868544,74,636174 acc=5 et=+4d4h22m27s600ms alt=734.0 vel=10.97 bear=2.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868741,74,636188 acc=10 et=+4d4h22m29s659ms alt=735.0 vel=10.97 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868906,74,636213 acc=8 et=+4d4h22m31s657ms alt=735.0 vel=9.57 bear=7.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868960,74,636278 acc=4 et=+4d4h22m33s592ms alt=735.0 vel=5.08 bear=44.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868989,74,636384 acc=3 et=+4d4h22m35s560ms alt=734.0 vel=4.06 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868953,74,636481 acc=3 et=+4d4h22m37s691ms alt=734.0 vel=4.22 bear=131.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868868,74,636509 acc=4 et=+4d4h22m39s662ms alt=733.0 vel=6.3 bear=181.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868545,74,636482 acc=4 et=+4d4h22m43s592ms alt=733.0 vel=10.89 bear=182.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868349,74,636470 acc=6 et=+4d4h22m45s565ms alt=733.0 vel=10.88 bear=182.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868133,74,636454 acc=6 et=+4d4h22m47s642ms alt=732.0 vel=11.38 bear=181.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867843,74,636440 acc=4 et=+4d4h22m49s649ms alt=731.0 vel=15.11 bear=179.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867571,74,636448 acc=6 et=+4d4h22m51s649ms alt=732.0 vel=15.1 bear=178.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867306,74,636449 acc=6 et=+4d4h22m53s654ms alt=731.0 vel=14.66 bear=185.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867059,74,636436 acc=10 et=+4d4h22m55s666ms alt=731.0 vel=14.11 bear=179.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,866672,74,636441 acc=7 et=+4d4h22m57s665ms alt=730.0 vel=17.03 bear=180.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,866336,74,636425 acc=5 et=+4d4h22m59s655ms alt=733.0 vel=18.04 bear=182.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865999,74,636399 acc=5 et=+4d4h23m1s650ms alt=735.0 vel=17.9 bear=180.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865680,74,636373 acc=5 et=+4d4h23m3s667ms alt=736.0 vel=17.44 bear=181.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865379,74,636405 acc=4 et=+4d4h23m5s693ms alt=738.0 vel=17.04 bear=179.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865072,74,636406 acc=6 et=+4d4h23m7s651ms alt=739.0 vel=17.03 bear=179.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864807,74,636392 acc=4 et=+4d4h23m9s655ms alt=739.0 vel=15.68 bear=180.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864569,74,636478 acc=4 et=+4d4h23m11s687ms alt=734.0 vel=14.63 bear=178.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864305,74,636485 acc=6 et=+4d4h23m13s656ms alt=734.0 vel=14.62 bear=179.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864082,74,636461 acc=4 et=+4d4h23m15s696ms alt=738.0 vel=12.16 bear=179.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863863,74,636464 acc=7 et=+4d4h23m17s656ms alt=738.0 vel=12.15 bear=179.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863698,74,636423 acc=7 et=+4d4h23m19s661ms alt=738.0 vel=10.02 bear=179.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863509,74,636439 acc=5 et=+4d4h23m21s657ms alt=738.0 vel=9.18 bear=179.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863346,74,636431 acc=4 et=+4d4h23m23s661ms alt=741.0 vel=8.51 bear=180.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863175,74,636433 acc=4 et=+4d4h23m25s659ms alt=742.0 vel=8.74 bear=178.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862980,74,636421 acc=4 et=+4d4h23m27s656ms alt=742.0 vel=10.01 bear=176.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862811,74,636413 acc=4 et=+4d4h23m29s690ms alt=745.0 vel=9.46 bear=177.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862641,74,636424 acc=7 et=+4d4h23m31s650ms alt=744.0 vel=9.46 bear=177.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862508,74,636407 acc=5 et=+4d4h23m33s694ms alt=746.0 vel=9.84 bear=180.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862137,74,636410 acc=7 et=+4d4h23m37s630ms alt=748.0 vel=10.42 bear=178.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861963,74,636429 acc=5 et=+4d4h23m39s689ms alt=749.0 vel=10.99 bear=179.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861767,74,636404 acc=4 et=+4d4h23m41s682ms alt=751.0 vel=10.99 bear=181.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861592,74,636415 acc=4 et=+4d4h23m43s670ms alt=752.0 vel=11.61 bear=180.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861386,74,636404 acc=5 et=+4d4h23m45s661ms alt=744.0 vel=11.91 bear=179.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861165,74,636413 acc=5 et=+4d4h23m47s661ms alt=749.0 vel=12.41 bear=179.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860905,74,636419 acc=5 et=+4d4h23m49s662ms alt=754.0 vel=12.98 bear=179.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860671,74,636419 acc=8 et=+4d4h23m51s651ms alt=755.0 vel=12.97 bear=180.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860467,74,636459 acc=5 et=+4d4h23m53s662ms alt=764.0 vel=11.3 bear=178.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860247,74,636452 acc=4 et=+4d4h23m55s678ms alt=761.0 vel=11.79 bear=180.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860034,74,636457 acc=4 et=+4d4h23m57s667ms alt=758.0 vel=12.23 bear=179.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859833,74,636499 acc=5 et=+4d4h23m59s656ms alt=756.0 vel=12.49 bear=180.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859597,74,636497 acc=5 et=+4d4h24m1s663ms alt=755.0 vel=12.62 bear=180.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859371,74,636496 acc=5 et=+4d4h24m3s662ms alt=754.0 vel=12.57 bear=180.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859155,74,636492 acc=4 et=+4d4h24m5s660ms alt=754.0 vel=12.12 bear=179.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858955,74,636495 acc=4 et=+4d4h24m7s668ms alt=755.0 vel=10.78 bear=180.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858737,74,636493 acc=3 et=+4d4h24m9s651ms alt=755.0 vel=11.59 bear=179.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858543,74,636490 acc=3 et=+4d4h24m11s664ms alt=755.0 vel=11.35 bear=179.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858347,74,636492 acc=3 et=+4d4h24m13s692ms alt=754.0 vel=11.09 bear=180.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858147,74,636490 acc=5 et=+4d4h24m15s653ms alt=754.0 vel=11.08 bear=180.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857961,74,636477 acc=3 et=+4d4h24m17s677ms alt=756.0 vel=10.85 bear=184.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857760,74,636447 acc=3 et=+4d4h24m19s661ms alt=756.0 vel=11.2 bear=186.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857549,74,636406 acc=4 et=+4d4h24m21s665ms alt=759.0 vel=11.55 bear=191.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857347,74,636338 acc=3 et=+4d4h24m23s667ms alt=760.0 vel=11.2 bear=194.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857161,74,636275 acc=4 et=+4d4h24m25s664ms alt=762.0 vel=10.49 bear=197.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856982,74,636195 acc=6 et=+4d4h24m27s656ms alt=762.0 vel=10.46 bear=201.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856849,74,636104 acc=3 et=+4d4h24m29s661ms alt=761.0 vel=8.77 bear=203.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856727,74,636010 acc=4 et=+4d4h24m31s678ms alt=762.0 vel=5.79 bear=208.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856651,74,635954 acc=4 et=+4d4h24m33s660ms alt=765.0 vel=2.95 bear=211.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856631,74,635891 acc=6 et=+4d4h24m43s655ms alt=770.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856580,74,635885 acc=5 et=+4d4h24m53s664ms alt=767.0 vel=0.39 bear=212.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856519,74,635843 acc=4 et=+4d4h25m22s854ms alt=765.0 vel=3.84 bear=216.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856435,74,635772 acc=4 et=+4d4h25m24s857ms alt=765.0 vel=5.62 bear=216.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856299,74,635670 acc=4 et=+4d4h25m26s871ms alt=769.0 vel=9.04 bear=213.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856157,74,635529 acc=5 et=+4d4h25m28s870ms alt=769.0 vel=9.75 bear=212.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855982,74,635384 acc=4 et=+4d4h25m30s844ms alt=770.0 vel=10.66 bear=210.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855811,74,635254 acc=5 et=+4d4h25m32s862ms alt=768.0 vel=12.06 bear=209.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855620,74,635093 acc=5 et=+4d4h25m34s859ms alt=768.0 vel=12.9 bear=210.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855405,74,634904 acc=4 et=+4d4h25m36s861ms alt=770.0 vel=13.91 bear=213.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855194,74,634735 acc=4 et=+4d4h25m38s875ms alt=769.0 vel=14.04 bear=211.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854859,74,634463 acc=4 et=+4d4h25m41s854ms alt=768.0 vel=14.7 bear=209.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854636,74,634291 acc=4 et=+4d4h25m43s868ms alt=769.0 vel=14.4 bear=209.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854435,74,634138 acc=5 et=+4d4h25m45s885ms alt=770.0 vel=14.13 bear=204.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854198,74,634017 acc=5 et=+4d4h25m47s878ms alt=771.0 vel=14.2 bear=198.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853957,74,633902 acc=5 et=+4d4h25m49s874ms alt=772.0 vel=13.69 bear=195.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853726,74,633840 acc=6 et=+4d4h25m51s866ms alt=771.0 vel=12.94 bear=192.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853525,74,633762 acc=5 et=+4d4h25m53s867ms alt=774.0 vel=11.38 bear=192.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853372,74,633721 acc=5 et=+4d4h25m55s872ms alt=779.0 vel=9.46 bear=191.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853247,74,633693 acc=6 et=+4d4h25m57s880ms alt=778.0 vel=7.56 bear=193.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853179,74,633651 acc=6 et=+4d4h25m59s874ms alt=773.0 vel=5.04 bear=214.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853071,74,633409 acc=5 et=+4d4h26m2s868ms alt=776.0 vel=8.94 bear=254.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853035,74,633199 acc=5 et=+4d4h26m4s880ms alt=778.0 vel=9.21 bear=256.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852979,74,632934 acc=6 et=+4d4h26m6s866ms alt=771.0 vel=10.47 bear=257.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852900,74,632663 acc=5 et=+4d4h26m8s878ms alt=774.0 vel=12.36 bear=257.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852856,74,632333 acc=6 et=+4d4h26m10s876ms alt=778.0 vel=13.98 bear=258.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852814,74,631991 acc=5 et=+4d4h26m12s877ms alt=780.0 vel=14.57 bear=257.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852742,74,631622 acc=5 et=+4d4h26m14s878ms alt=781.0 vel=15.74 bear=257.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852683,74,631234 acc=4 et=+4d4h26m16s881ms alt=776.0 vel=16.56 bear=257.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852616,74,630856 acc=4 et=+4d4h26m18s870ms alt=778.0 vel=16.31 bear=255.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852543,74,630469 acc=6 et=+4d4h26m20s872ms alt=779.0 vel=16.3 bear=255.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852474,74,630123 acc=3 et=+4d4h26m22s905ms alt=776.0 vel=15.37 bear=254.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852397,74,629768 acc=3 et=+4d4h26m24s885ms alt=779.0 vel=14.74 bear=253.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852323,74,629421 acc=4 et=+4d4h26m26s876ms alt=781.0 vel=14.44 bear=252.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852270,74,629074 acc=4 et=+4d4h26m28s889ms alt=779.0 vel=14.23 bear=253.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852196,74,628737 acc=4 et=+4d4h26m30s884ms alt=780.0 vel=14.08 bear=253.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852139,74,628423 acc=4 et=+4d4h26m32s885ms alt=779.0 vel=13.87 bear=254.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852099,74,628067 acc=4 et=+4d4h26m34s876ms alt=776.0 vel=13.58 bear=255.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852020,74,627745 acc=3 et=+4d4h26m36s904ms alt=775.0 vel=13.42 bear=255.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851960,74,627427 acc=5 et=+4d4h26m38s868ms alt=775.0 vel=13.42 bear=255.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851916,74,627156 acc=5 et=+4d4h26m40s875ms alt=775.0 vel=12.19 bear=255.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851870,74,626882 acc=5 et=+4d4h26m42s877ms alt=771.0 vel=11.28 bear=255.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851809,74,626621 acc=4 et=+4d4h26m44s948ms alt=772.0 vel=11.07 bear=255.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851750,74,626364 acc=3 et=+4d4h26m47s282ms alt=773.0 vel=11.03 bear=255.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851702,74,626111 acc=3 et=+4d4h26m49s286ms alt=773.0 vel=10.64 bear=254.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851650,74,625867 acc=3 et=+4d4h26m51s289ms alt=773.0 vel=10.23 bear=255.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851612,74,625647 acc=3 et=+4d4h26m53s294ms alt=773.0 vel=8.88 bear=258.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851593,74,625433 acc=3 et=+4d4h26m55s293ms alt=774.0 vel=8.62 bear=266.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851592,74,625192 acc=3 et=+4d4h26m57s295ms alt=774.0 vel=9.71 bear=272.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851592,74,624952 acc=3 et=+4d4h26m59s286ms alt=775.0 vel=9.59 bear=272.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851601,74,624705 acc=3 et=+4d4h27m1s288ms alt=774.0 vel=10.57 bear=273.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851610,74,624448 acc=3 et=+4d4h27m3s291ms alt=774.0 vel=10.56 bear=272.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851616,74,624197 acc=3 et=+4d4h27m5s331ms alt=773.0 vel=8.57 bear=271.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851621,74,623987 acc=4 et=+4d4h27m7s287ms alt=773.0 vel=8.57 bear=272.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851642,74,623735 acc=5 et=+4d4h27m17s4ms alt=775.0 vel=2.42 bear=272.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851644,74,623665 acc=4 et=+4d4h27m19s0ms alt=775.0 vel=3.5 bear=273.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851639,74,623573 acc=4 et=+4d4h27m21s6ms alt=773.0 vel=3.41 bear=273.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851636,74,623498 acc=4 et=+4d4h27m23s11ms alt=773.0 vel=2.19 bear=271.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851614,74,623392 acc=5 et=+4d4h27m27s19ms alt=775.0 vel=2.0 bear=240.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851569,74,623372 acc=7 et=+4d4h27m31s9ms alt=774.0 vel=1.14 bear=214.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851509,74,623352 acc=4 et=+4d4h27m33s22ms alt=773.0 vel=4.29 bear=185.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851380,74,623327 acc=5 et=+4d4h27m35s57ms alt=773.0 vel=6.85 bear=182.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851266,74,623346 acc=6 et=+4d4h27m37s28ms alt=771.0 vel=8.21 bear=180.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851102,74,623330 acc=5 et=+4d4h27m39s26ms alt=772.0 vel=9.09 bear=182.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850915,74,623286 acc=6 et=+4d4h27m40s930ms alt=771.0 vel=10.05 bear=182.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850746,74,623298 acc=6 et=+4d4h27m42s925ms alt=770.0 vel=11.15 bear=182.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850542,74,623283 acc=6 et=+4d4h27m45s22ms alt=770.0 vel=12.56 bear=183.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850322,74,623271 acc=6 et=+4d4h27m47s19ms alt=770.0 vel=12.88 bear=183.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850072,74,623268 acc=5 et=+4d4h27m49s15ms alt=770.0 vel=12.9 bear=183.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849829,74,623243 acc=5 et=+4d4h27m51s26ms alt=769.0 vel=13.31 bear=183.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849570,74,623237 acc=6 et=+4d4h27m53s21ms alt=769.0 vel=13.23 bear=183.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849341,74,623201 acc=6 et=+4d4h27m55s23ms alt=771.0 vel=13.05 bear=185.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849100,74,623193 acc=6 et=+4d4h27m57s19ms alt=772.0 vel=12.71 bear=185.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848873,74,623183 acc=5 et=+4d4h27m59s25ms alt=774.0 vel=12.26 bear=184.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848681,74,623157 acc=5 et=+4d4h28m1s29ms alt=776.0 vel=9.5 bear=184.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848542,74,623139 acc=5 et=+4d4h28m3s28ms alt=776.0 vel=5.47 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848491,74,623132 acc=5 et=+4d4h28m5s28ms alt=773.0 vel=2.87 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848435,74,623131 acc=6 et=+4d4h28m23s113ms alt=779.0 vel=0.93 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848381,74,623123 acc=6 et=+4d4h28m26s464ms alt=779.0 vel=4.12 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848308,74,623116 acc=5 et=+4d4h28m28s438ms alt=779.0 vel=5.14 bear=181.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848157,74,623072 acc=5 et=+4d4h28m32s482ms alt=778.0 vel=6.29 bear=186.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847950,74,623031 acc=3 et=+4d4h28m36s478ms alt=782.0 vel=8.53 bear=181.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847796,74,623027 acc=6 et=+4d4h28m38s478ms alt=782.0 vel=8.53 bear=181.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847622,74,623004 acc=4 et=+4d4h28m41s350ms alt=783.0 vel=8.94 bear=183.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847444,74,622973 acc=5 et=+4d4h28m43s347ms alt=786.0 vel=10.28 bear=183.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847231,74,622953 acc=5 et=+4d4h28m45s415ms alt=786.0 vel=11.78 bear=183.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847000,74,622944 acc=4 et=+4d4h28m47s360ms alt=777.0 vel=12.88 bear=183.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845248,74,622786 acc=3 et=+4d4h29m3s341ms alt=791.0 vel=12.78 bear=183.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845004,74,622779 acc=3 et=+4d4h29m5s379ms alt=790.0 vel=12.53 bear=182.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844777,74,622740 acc=3 et=+4d4h29m7s355ms alt=790.0 vel=12.87 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844539,74,622713 acc=4 et=+4d4h29m9s368ms alt=792.0 vel=13.26 bear=182.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844301,74,622694 acc=6 et=+4d4h29m11s356ms alt=793.0 vel=13.26 bear=183.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844039,74,622695 acc=3 et=+4d4h29m13s365ms alt=794.0 vel=13.44 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843799,74,622680 acc=3 et=+4d4h29m15s345ms alt=796.0 vel=13.23 bear=183.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843574,74,622660 acc=3 et=+4d4h29m17s360ms alt=796.0 vel=12.81 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843345,74,622646 acc=3 et=+4d4h29m19s360ms alt=797.0 vel=12.24 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843142,74,622627 acc=3 et=+4d4h29m21s351ms alt=796.0 vel=11.19 bear=183.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842968,74,622612 acc=3 et=+4d4h29m23s369ms alt=796.0 vel=9.34 bear=183.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842806,74,622584 acc=3 et=+4d4h29m25s480ms alt=796.0 vel=7.56 bear=183.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842679,74,622571 acc=3 et=+4d4h29m28s856ms alt=797.0 vel=6.31 bear=183.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842596,74,622573 acc=3 et=+4d4h29m30s857ms alt=798.0 vel=4.13 bear=184.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842549,74,622580 acc=3 et=+4d4h29m32s879ms alt=797.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842479,74,622521 acc=4 et=+4d4h30m18s879ms alt=799.0 vel=4.59 bear=183.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842372,74,622519 acc=3 et=+4d4h30m20s873ms alt=799.0 vel=6.69 bear=183.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842222,74,622516 acc=4 et=+4d4h30m22s868ms alt=799.0 vel=9.25 bear=182.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842047,74,622501 acc=4 et=+4d4h30m24s862ms alt=800.0 vel=9.89 bear=183.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841896,74,622506 acc=4 et=+4d4h30m26s863ms alt=799.0 vel=9.84 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841674,74,622497 acc=5 et=+4d4h30m28s875ms alt=798.0 vel=12.07 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841418,74,622485 acc=5 et=+4d4h30m30s873ms alt=803.0 vel=13.51 bear=183.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841186,74,622456 acc=5 et=+4d4h30m32s870ms alt=803.0 vel=13.07 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840952,74,622441 acc=4 et=+4d4h30m34s872ms alt=802.0 vel=13.28 bear=183.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840719,74,622432 acc=5 et=+4d4h30m36s865ms alt=803.0 vel=12.31 bear=182.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840503,74,622425 acc=3 et=+4d4h30m38s881ms alt=802.0 vel=10.76 bear=182.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840332,74,622427 acc=4 et=+4d4h30m40s875ms alt=804.0 vel=9.64 bear=182.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840145,74,622387 acc=4 et=+4d4h30m42s878ms alt=806.0 vel=10.3 bear=182.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839928,74,622400 acc=4 et=+4d4h30m44s867ms alt=805.0 vel=10.17 bear=182.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839764,74,622333 acc=5 et=+4d4h30m46s866ms alt=807.0 vel=11.16 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839562,74,622302 acc=5 et=+4d4h30m48s862ms alt=808.0 vel=11.22 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839368,74,622272 acc=5 et=+4d4h30m50s875ms alt=807.0 vel=10.91 bear=183.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839160,74,622257 acc=4 et=+4d4h30m52s873ms alt=807.0 vel=11.56 bear=182.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838963,74,622244 acc=4 et=+4d4h30m54s879ms alt=808.0 vel=11.98 bear=182.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838735,74,622232 acc=4 et=+4d4h30m56s860ms alt=809.0 vel=11.95 bear=184.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838514,74,622227 acc=5 et=+4d4h30m58s878ms alt=800.0 vel=12.02 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838290,74,622218 acc=5 et=+4d4h31m0s888ms alt=803.0 vel=12.1 bear=183.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838082,74,622214 acc=5 et=+4d4h31m2s866ms alt=806.0 vel=11.8 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837914,74,622192 acc=6 et=+4d4h31m4s867ms alt=804.0 vel=9.69 bear=183.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837764,74,622174 acc=5 et=+4d4h31m6s873ms alt=807.0 vel=7.58 bear=183.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837620,74,622170 acc=5 et=+4d4h31m8s875ms alt=810.0 vel=7.35 bear=183.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837494,74,622177 acc=4 et=+4d4h31m10s877ms alt=810.0 vel=7.15 bear=182.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837368,74,622170 acc=5 et=+4d4h31m12s875ms alt=811.0 vel=6.69 bear=182.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837221,74,622173 acc=4 et=+4d4h31m14s882ms alt=812.0 vel=7.85 bear=178.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837057,74,622176 acc=3 et=+4d4h31m16s906ms alt=813.0 vel=9.53 bear=180.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836887,74,622162 acc=5 et=+4d4h31m18s872ms alt=813.0 vel=9.45 bear=187.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836615,74,622149 acc=4 et=+4d4h31m21s879ms alt=814.0 vel=9.36 bear=183.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836338,74,622119 acc=4 et=+4d4h31m24s888ms alt=813.0 vel=7.48 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836243,74,622087 acc=4 et=+4d4h31m26s876ms alt=813.0 vel=6.15 bear=183.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836129,74,622076 acc=4 et=+4d4h31m28s881ms alt=816.0 vel=5.98 bear=183.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836017,74,622079 acc=5 et=+4d4h31m30s867ms alt=814.0 vel=6.12 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835889,74,622049 acc=5 et=+4d4h31m32s877ms alt=816.0 vel=7.36 bear=183.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835759,74,622050 acc=5 et=+4d4h31m34s872ms alt=818.0 vel=8.21 bear=182.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835590,74,622021 acc=6 et=+4d4h31m36s873ms alt=817.0 vel=8.25 bear=182.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835452,74,622017 acc=5 et=+4d4h31m38s908ms alt=818.0 vel=8.12 bear=180.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835366,74,622055 acc=4 et=+4d4h31m40s874ms alt=815.0 vel=3.74 bear=179.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835310,74,622065 acc=4 et=+4d4h31m42s871ms alt=816.0 vel=1.64 bear=180.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835220,74,622083 acc=5 et=+4d4h31m48s868ms alt=818.0 vel=3.5 bear=182.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835154,74,622068 acc=5 et=+4d4h31m50s870ms alt=818.0 vel=4.02 bear=182.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835073,74,622047 acc=4 et=+4d4h31m52s876ms alt=815.0 vel=4.32 bear=183.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834988,74,622028 acc=5 et=+4d4h31m54s906ms alt=816.0 vel=3.89 bear=182.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834929,74,622018 acc=4 et=+4d4h31m56s877ms alt=815.0 vel=1.13 bear=183.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834847,74,622001 acc=6 et=+4d4h32m2s867ms alt=822.0 vel=4.59 bear=185.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834761,74,621976 acc=5 et=+4d4h32m4s864ms alt=822.0 vel=4.52 bear=188.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834681,74,621957 acc=5 et=+4d4h32m6s877ms alt=821.0 vel=3.33 bear=187.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834630,74,621951 acc=4 et=+4d4h32m8s870ms alt=823.0 vel=1.39 bear=186.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834568,74,621937 acc=5 et=+4d4h32m14s874ms alt=827.0 vel=1.09 bear=205.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834529,74,621880 acc=5 et=+4d4h32m18s865ms alt=826.0 vel=1.39 bear=244.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834501,74,621770 acc=5 et=+4d4h32m22s879ms alt=830.0 vel=3.01 bear=263.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834497,74,621686 acc=7 et=+4d4h32m24s859ms alt=831.0 vel=2.99 bear=268.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834503,74,621592 acc=4 et=+4d4h32m26s879ms alt=823.0 vel=3.7 bear=272.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834513,74,621505 acc=3 et=+4d4h32m28s887ms alt=826.0 vel=2.99 bear=275.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834523,74,621425 acc=4 et=+4d4h32m32s883ms alt=828.0 vel=0.46 bear=275.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834565,74,621493 acc=4 et=+4d4h32m44s899ms alt=823.0 vel=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834568,74,621354 acc=5 et=+4d4h33m24s784ms alt=828.0 vel=4.11 bear=273.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834557,74,621171 acc=4 et=+4d4h33m26s778ms alt=827.0 vel=6.85 bear=272.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834563,74,621004 acc=6 et=+4d4h33m28s873ms alt=827.0 vel=6.85 bear=272.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834538,74,620863 acc=5 et=+4d4h33m30s877ms alt=824.0 vel=4.77 bear=250.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834501,74,620797 acc=5 et=+4d4h33m32s783ms alt=823.0 vel=2.42 bear=211.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834430,74,620755 acc=6 et=+4d4h33m36s859ms alt=823.0 vel=2.45 bear=190.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834374,74,620734 acc=21 et=+4d4h33m46s236ms alt=821.0 vel=1.11 bear=181.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,834311,74,620735 acc=51 et=+4d4h33m52s251ms alt=821.0 vel=1.73 bear=176.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835049,74,614837 acc=5 et=+4d7h15m22s320ms alt=814.0 vel=4.57 bear=347.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835251,74,614839 acc=3 et=+4d7h15m25s308ms alt=813.0 vel=8.42 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835410,74,614849 acc=3 et=+4d7h15m27s314ms alt=812.0 vel=8.35 bear=4.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835567,74,614864 acc=3 et=+4d7h15m29s305ms alt=811.0 vel=8.82 bear=3.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835732,74,614897 acc=3 et=+4d7h15m31s350ms alt=812.0 vel=9.58 bear=5.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,835921,74,614923 acc=3 et=+4d7h15m33s318ms alt=811.0 vel=10.7 bear=3.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836122,74,614946 acc=3 et=+4d7h15m35s315ms alt=811.0 vel=11.29 bear=4.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836316,74,614980 acc=3 et=+4d7h15m37s338ms alt=810.0 vel=11.28 bear=5.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836520,74,614995 acc=4 et=+4d7h15m39s347ms alt=809.0 vel=11.61 bear=2.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836704,74,615030 acc=3 et=+4d7h15m41s317ms alt=812.0 vel=10.39 bear=5.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,836879,74,615061 acc=3 et=+4d7h15m43s322ms alt=810.0 vel=10.03 bear=5.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837064,74,615083 acc=3 et=+4d7h15m45s315ms alt=809.0 vel=10.22 bear=4.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837253,74,615098 acc=3 et=+4d7h15m47s314ms alt=808.0 vel=10.5 bear=3.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837459,74,615122 acc=3 et=+4d7h15m49s231ms alt=807.0 vel=11.88 bear=4.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837681,74,615143 acc=3 et=+4d7h15m51s322ms alt=806.0 vel=12.69 bear=4.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,837920,74,615172 acc=3 et=+4d7h15m53s331ms alt=806.0 vel=13.56 bear=4.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838179,74,615200 acc=3 et=+4d7h15m55s324ms alt=805.0 vel=14.53 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838445,74,615217 acc=3 et=+4d7h15m57s329ms alt=804.0 vel=14.5 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838702,74,615245 acc=3 et=+4d7h15m59s334ms alt=804.0 vel=14.59 bear=4.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,838964,74,615271 acc=3 et=+4d7h16m1s322ms alt=803.0 vel=14.44 bear=4.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839218,74,615299 acc=3 et=+4d7h16m3s332ms alt=804.0 vel=14.1 bear=4.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839474,74,615316 acc=3 et=+4d7h16m5s333ms alt=803.0 vel=14.33 bear=1.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839736,74,615329 acc=3 et=+4d7h16m7s329ms alt=802.0 vel=14.13 bear=2.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,839998,74,615347 acc=3 et=+4d7h16m9s324ms alt=801.0 vel=14.17 bear=3.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840249,74,615366 acc=3 et=+4d7h16m11s319ms alt=801.0 vel=14.21 bear=3.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840491,74,615380 acc=3 et=+4d7h16m13s366ms alt=801.0 vel=13.35 bear=3.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840730,74,615403 acc=3 et=+4d7h16m16s291ms alt=801.0 vel=13.12 bear=3.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,840974,74,615414 acc=3 et=+4d7h16m18s290ms alt=801.0 vel=13.34 bear=3.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841206,74,615433 acc=3 et=+4d7h16m20s300ms alt=800.0 vel=12.82 bear=3.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841427,74,615453 acc=3 et=+4d7h16m22s303ms alt=799.0 vel=12.63 bear=3.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841660,74,615472 acc=3 et=+4d7h16m24s308ms alt=798.0 vel=12.44 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,841875,74,615484 acc=3 et=+4d7h16m26s306ms alt=797.0 vel=11.39 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842038,74,615490 acc=3 et=+4d7h16m28s304ms alt=796.0 vel=8.3 bear=3.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842137,74,615501 acc=3 et=+4d7h16m30s307ms alt=796.0 vel=4.96 bear=3.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842188,74,615502 acc=3 et=+4d7h16m32s305ms alt=795.0 vel=2.52 bear=3.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842254,74,615527 acc=3 et=+4d7h16m50s345ms alt=797.0 vel=2.4 bear=5.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842331,74,615538 acc=5 et=+4d7h17m4s308ms alt=800.0 vel=1.42 bear=3.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842417,74,615548 acc=5 et=+4d7h17m8s321ms alt=798.0 vel=1.5 bear=7.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842530,74,615556 acc=4 et=+4d7h17m14s318ms alt=792.0 vel=4.85 bear=11.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842672,74,615534 acc=4 et=+4d7h17m16s316ms alt=792.0 vel=6.65 bear=6.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842781,74,615538 acc=4 et=+4d7h17m18s312ms alt=792.0 vel=7.07 bear=5.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,842937,74,615552 acc=4 et=+4d7h17m20s315ms alt=790.0 vel=7.45 bear=2.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843094,74,615553 acc=5 et=+4d7h17m22s319ms alt=790.0 vel=8.07 bear=2.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843244,74,615578 acc=6 et=+4d7h17m24s309ms alt=789.0 vel=8.42 bear=4.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843397,74,615589 acc=7 et=+4d7h17m26s313ms alt=789.0 vel=8.45 bear=4.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843558,74,615593 acc=5 et=+4d7h17m28s308ms alt=788.0 vel=8.39 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843719,74,615616 acc=5 et=+4d7h17m30s316ms alt=789.0 vel=8.82 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,843867,74,615621 acc=5 et=+4d7h17m32s298ms alt=786.0 vel=8.7 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844036,74,615638 acc=5 et=+4d7h17m34s312ms alt=787.0 vel=9.01 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844208,74,615663 acc=4 et=+4d7h17m36s316ms alt=792.0 vel=9.44 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844389,74,615684 acc=4 et=+4d7h17m38s311ms alt=789.0 vel=9.78 bear=3.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844573,74,615690 acc=3 et=+4d7h17m40s309ms alt=786.0 vel=9.83 bear=4.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844763,74,615694 acc=4 et=+4d7h17m42s326ms alt=785.0 vel=10.1 bear=2.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,844937,74,615678 acc=4 et=+4d7h17m44s318ms alt=784.0 vel=10.25 bear=2.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845121,74,615698 acc=4 et=+4d7h17m46s309ms alt=782.0 vel=10.66 bear=2.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845297,74,615706 acc=3 et=+4d7h17m48s309ms alt=781.0 vel=10.74 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845506,74,615713 acc=4 et=+4d7h17m50s305ms alt=781.0 vel=11.22 bear=3.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845706,74,615746 acc=5 et=+4d7h17m52s325ms alt=780.0 vel=11.52 bear=3.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,845921,74,615781 acc=5 et=+4d7h17m54s306ms alt=780.0 vel=11.65 bear=3.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,846138,74,615809 acc=6 et=+4d7h17m56s314ms alt=779.0 vel=11.39 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,846348,74,615828 acc=5 et=+4d7h17m58s314ms alt=777.0 vel=11.06 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,846542,74,615843 acc=4 et=+4d7h18m0s310ms alt=778.0 vel=10.48 bear=3.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,846698,74,615854 acc=4 et=+4d7h18m2s307ms alt=776.0 vel=9.81 bear=3.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,846850,74,615867 acc=4 et=+4d7h18m4s317ms alt=777.0 vel=9.22 bear=3.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847014,74,615897 acc=4 et=+4d7h18m6s311ms alt=777.0 vel=8.63 bear=2.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847154,74,615917 acc=4 et=+4d7h18m8s310ms alt=776.0 vel=8.02 bear=3.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847302,74,615932 acc=4 et=+4d7h18m10s309ms alt=777.0 vel=8.15 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847466,74,615929 acc=4 et=+4d7h18m12s305ms alt=774.0 vel=8.28 bear=4.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847604,74,615936 acc=5 et=+4d7h18m14s318ms alt=775.0 vel=8.47 bear=3.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847741,74,615928 acc=5 et=+4d7h18m16s314ms alt=771.0 vel=8.1 bear=2.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,847882,74,615919 acc=4 et=+4d7h18m18s308ms alt=769.0 vel=7.66 bear=5.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848009,74,615937 acc=4 et=+4d7h18m20s317ms alt=770.0 vel=6.85 bear=3.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848149,74,615957 acc=6 et=+4d7h18m22s311ms alt=767.0 vel=6.7 bear=1.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848282,74,615957 acc=5 et=+4d7h18m24s312ms alt=765.0 vel=6.74 bear=1.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848401,74,615944 acc=5 et=+4d7h18m26s308ms alt=765.0 vel=7.07 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848522,74,615960 acc=5 et=+4d7h18m28s309ms alt=765.0 vel=7.42 bear=3.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848669,74,615983 acc=5 et=+4d7h18m30s305ms alt=765.0 vel=7.77 bear=1.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848808,74,615984 acc=5 et=+4d7h18m32s314ms alt=766.0 vel=8.28 bear=4.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,848946,74,616009 acc=7 et=+4d7h18m34s319ms alt=766.0 vel=8.02 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849313,74,616044 acc=7 et=+4d7h18m45s22ms alt=765.0 vel=5.31 bear=3.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849426,74,616057 acc=8 et=+4d7h18m47s15ms alt=764.0 vel=6.0 bear=2.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849567,74,616075 acc=8 et=+4d7h18m49s26ms alt=764.0 vel=7.31 bear=1.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849703,74,616062 acc=6 et=+4d7h18m51s32ms alt=780.0 vel=8.24 bear=0.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,849866,74,616100 acc=6 et=+4d7h18m53s33ms alt=773.0 vel=9.45 bear=5.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850027,74,616132 acc=5 et=+4d7h18m55s21ms alt=770.0 vel=8.83 bear=2.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850224,74,616144 acc=4 et=+4d7h18m57s33ms alt=767.0 vel=10.08 bear=2.6 {Bundle[mParcelledData.dataSize=40]}]\n"));

        track.addAll(trackFromString("Location[gps 42,850410,74,616156 acc=4 et=+4d7h18m59s23ms alt=768.0 vel=10.3 bear=2.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850604,74,616184 acc=3 et=+4d7h19m1s31ms alt=768.0 vel=10.31 bear=2.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850789,74,616193 acc=4 et=+4d7h19m3s33ms alt=767.0 vel=10.08 bear=3.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,850955,74,616221 acc=3 et=+4d7h19m5s46ms alt=768.0 vel=9.62 bear=4.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851132,74,616222 acc=4 et=+4d7h19m7s47ms alt=768.0 vel=9.51 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851313,74,616256 acc=4 et=+4d7h19m9s40ms alt=772.0 vel=9.79 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851652,74,616275 acc=4 et=+4d7h19m13s58ms alt=774.0 vel=8.13 bear=4.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851768,74,616277 acc=3 et=+4d7h19m15s24ms alt=775.0 vel=5.7 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851836,74,616281 acc=3 et=+4d7h19m17s28ms alt=775.0 vel=4.42 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851897,74,616310 acc=3 et=+4d7h19m18s935ms alt=774.0 vel=4.22 bear=26.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851912,74,616439 acc=3 et=+4d7h19m20s935ms alt=773.0 vel=5.9 bear=84.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851908,74,616629 acc=3 et=+4d7h19m23s30ms alt=772.0 vel=8.25 bear=89.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851902,74,616876 acc=3 et=+4d7h19m24s933ms alt=772.0 vel=10.22 bear=91.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851889,74,617130 acc=3 et=+4d7h19m26s931ms alt=771.0 vel=9.97 bear=93.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851877,74,617403 acc=3 et=+4d7h19m28s936ms alt=771.0 vel=10.87 bear=93.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851863,74,617700 acc=3 et=+4d7h19m31s28ms alt=771.0 vel=11.89 bear=93.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851848,74,618003 acc=3 et=+4d7h19m33s31ms alt=771.0 vel=12.31 bear=92.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851837,74,618303 acc=3 et=+4d7h19m34s935ms alt=772.0 vel=12.02 bear=92.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851824,74,618599 acc=3 et=+4d7h19m36s941ms alt=772.0 vel=12.31 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851806,74,618885 acc=3 et=+4d7h19m39s29ms alt=771.0 vel=12.1 bear=93.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851795,74,619190 acc=3 et=+4d7h19m41s36ms alt=772.0 vel=12.32 bear=93.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851777,74,619491 acc=3 et=+4d7h19m43s25ms alt=771.0 vel=12.59 bear=93.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851759,74,619805 acc=3 et=+4d7h19m45s26ms alt=770.0 vel=13.08 bear=93.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851742,74,620140 acc=3 et=+4d7h19m47s28ms alt=770.0 vel=13.57 bear=93.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851727,74,620479 acc=3 et=+4d7h19m49s29ms alt=770.0 vel=13.95 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851711,74,620827 acc=3 et=+4d7h19m51s25ms alt=770.0 vel=14.31 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851697,74,621179 acc=3 et=+4d7h19m53s31ms alt=771.0 vel=14.44 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851679,74,621515 acc=3 et=+4d7h19m55s27ms alt=771.0 vel=14.21 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851665,74,621849 acc=3 et=+4d7h19m57s33ms alt=771.0 vel=13.65 bear=93.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851652,74,622329 acc=4 et=+4d7h20m0s18ms alt=772.0 vel=13.17 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851643,74,622625 acc=4 et=+4d7h20m2s38ms alt=772.0 vel=12.54 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851629,74,622846 acc=3 et=+4d7h20m4s49ms alt=771.0 vel=7.89 bear=93.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851619,74,622986 acc=3 et=+4d7h20m6s38ms alt=772.0 vel=4.47 bear=94.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851612,74,623057 acc=3 et=+4d7h20m7s967ms alt=772.0 vel=0.62 bear=93.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851639,74,623109 acc=3 et=+4d7h20m36s27ms alt=772.0 vel=0.79 bear=91.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851610,74,623188 acc=3 et=+4d7h20m38s45ms alt=772.0 vel=2.53 bear=87.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851606,74,623250 acc=3 et=+4d7h20m40s69ms alt=772.0 vel=2.19 bear=90.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851602,74,623341 acc=3 et=+4d7h20m44s32ms alt=769.0 vel=0.96 bear=89.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851656,74,623452 acc=3 et=+4d7h20m48s37ms alt=770.0 vel=4.82 bear=29.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851792,74,623460 acc=4 et=+4d7h20m50s39ms alt=769.0 vel=8.12 bear=3.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,851965,74,623485 acc=4 et=+4d7h20m52s48ms alt=771.0 vel=10.38 bear=0.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852166,74,623459 acc=5 et=+4d7h20m54s39ms alt=770.0 vel=10.74 bear=0.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852380,74,623459 acc=7 et=+4d7h20m56s43ms alt=770.0 vel=11.87 bear=1.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852618,74,623462 acc=9 et=+4d7h20m58s47ms alt=771.0 vel=13.04 bear=1.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,852924,74,623441 acc=8 et=+4d7h21m0s46ms alt=768.0 vel=13.73 bear=1.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853169,74,623473 acc=6 et=+4d7h21m2s31ms alt=769.0 vel=13.94 bear=1.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853398,74,623519 acc=6 et=+4d7h21m4s48ms alt=767.0 vel=14.72 bear=2.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853660,74,623504 acc=6 et=+4d7h21m6s40ms alt=767.0 vel=14.82 bear=2.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,853932,74,623455 acc=6 et=+4d7h21m8s35ms alt=764.0 vel=14.94 bear=2.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854200,74,623439 acc=6 et=+4d7h21m10s36ms alt=760.0 vel=14.97 bear=2.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854484,74,623462 acc=5 et=+4d7h21m12s34ms alt=757.0 vel=15.05 bear=2.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,854759,74,623487 acc=6 et=+4d7h21m14s41ms alt=754.0 vel=14.86 bear=4.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855023,74,623537 acc=6 et=+4d7h21m16s41ms alt=751.0 vel=16.02 bear=4.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855312,74,623533 acc=6 et=+4d7h21m18s34ms alt=748.0 vel=15.97 bear=2.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855599,74,623575 acc=5 et=+4d7h21m20s42ms alt=746.0 vel=15.82 bear=2.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,855886,74,623587 acc=5 et=+4d7h21m22s40ms alt=746.0 vel=15.15 bear=2.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856129,74,623611 acc=5 et=+4d7h21m24s75ms alt=747.0 vel=11.85 bear=4.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856308,74,623660 acc=5 et=+4d7h21m26s71ms alt=747.0 vel=7.37 bear=10.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856437,74,623698 acc=8 et=+4d7h21m28s41ms alt=747.0 vel=7.34 bear=11.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856410,74,623929 acc=4 et=+4d7h21m30s45ms alt=746.0 vel=8.51 bear=88.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856405,74,624176 acc=4 et=+4d7h21m32s53ms alt=747.0 vel=10.36 bear=91.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856427,74,624435 acc=4 et=+4d7h21m34s59ms alt=749.0 vel=11.92 bear=91.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856430,74,624753 acc=4 et=+4d7h21m36s33ms alt=749.0 vel=13.27 bear=92.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856415,74,625111 acc=4 et=+4d7h21m38s47ms alt=749.0 vel=13.94 bear=92.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856397,74,625474 acc=5 et=+4d7h21m40s41ms alt=749.0 vel=14.79 bear=93.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856370,74,625867 acc=6 et=+4d7h21m42s51ms alt=750.0 vel=15.6 bear=93.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856356,74,626235 acc=5 et=+4d7h21m44s37ms alt=750.0 vel=15.77 bear=93.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856343,74,626640 acc=5 et=+4d7h21m46s42ms alt=750.0 vel=16.08 bear=93.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856319,74,627047 acc=4 et=+4d7h21m48s36ms alt=751.0 vel=16.27 bear=92.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856294,74,627421 acc=5 et=+4d7h21m50s35ms alt=752.0 vel=16.13 bear=93.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856266,74,627801 acc=5 et=+4d7h21m52s51ms alt=753.0 vel=15.43 bear=93.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856228,74,628190 acc=4 et=+4d7h21m54s27ms alt=757.0 vel=14.89 bear=93.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856213,74,628554 acc=6 et=+4d7h21m56s13ms alt=757.0 vel=14.88 bear=92.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856242,74,628919 acc=4 et=+4d7h21m58s36ms alt=757.0 vel=14.87 bear=91.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856234,74,629290 acc=4 et=+4d7h22m0s36ms alt=757.0 vel=14.86 bear=92.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856230,74,629631 acc=4 et=+4d7h22m2s41ms alt=756.0 vel=15.01 bear=91.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856217,74,630005 acc=4 et=+4d7h22m4s44ms alt=757.0 vel=15.27 bear=92.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856195,74,630362 acc=4 et=+4d7h22m6s35ms alt=755.0 vel=15.31 bear=91.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856189,74,630736 acc=4 et=+4d7h22m8s39ms alt=756.0 vel=15.41 bear=91.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856169,74,631109 acc=3 et=+4d7h22m9s970ms alt=756.0 vel=15.41 bear=91.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856160,74,631486 acc=5 et=+4d7h22m11s934ms alt=756.0 vel=15.4 bear=91.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856187,74,631850 acc=3 et=+4d7h22m13s939ms alt=758.0 vel=15.24 bear=91.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856168,74,632233 acc=3 et=+4d7h22m16s32ms alt=758.0 vel=15.05 bear=91.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856165,74,632611 acc=3 et=+4d7h22m18s46ms alt=758.0 vel=14.81 bear=91.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856157,74,632945 acc=3 et=+4d7h22m20s58ms alt=757.0 vel=14.25 bear=91.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856148,74,633286 acc=3 et=+4d7h22m22s33ms alt=758.0 vel=13.74 bear=91.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856145,74,633585 acc=3 et=+4d7h22m24s29ms alt=758.0 vel=12.5 bear=90.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856131,74,633875 acc=4 et=+4d7h22m26s52ms alt=758.0 vel=11.61 bear=91.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856131,74,634146 acc=4 et=+4d7h22m28s42ms alt=759.0 vel=10.41 bear=91.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856123,74,634368 acc=4 et=+4d7h22m30s37ms alt=759.0 vel=7.99 bear=91.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856121,74,634553 acc=4 et=+4d7h22m32s41ms alt=760.0 vel=7.11 bear=91.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856117,74,634763 acc=4 et=+4d7h22m34s46ms alt=763.0 vel=7.95 bear=91.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856119,74,634976 acc=4 et=+4d7h22m36s37ms alt=763.0 vel=8.82 bear=90.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856132,74,635201 acc=5 et=+4d7h22m38s46ms alt=761.0 vel=8.63 bear=88.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856147,74,635420 acc=4 et=+4d7h22m40s36ms alt=761.0 vel=9.23 bear=81.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856195,74,635655 acc=4 et=+4d7h22m42s37ms alt=763.0 vel=9.74 bear=67.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856322,74,635848 acc=3 et=+4d7h22m44s42ms alt=762.0 vel=10.13 bear=48.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856473,74,636005 acc=4 et=+4d7h22m46s38ms alt=760.0 vel=10.98 bear=28.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856669,74,636158 acc=4 et=+4d7h22m48s34ms alt=759.0 vel=11.71 bear=26.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,856854,74,636291 acc=4 et=+4d7h22m50s68ms alt=760.0 vel=11.81 bear=24.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857050,74,636400 acc=7 et=+4d7h22m52s75ms alt=759.0 vel=11.73 bear=19.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,857982,74,636597 acc=3 et=+4d7h23m1s516ms alt=755.0 vel=13.98 bear=3.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858242,74,636604 acc=3 et=+4d7h23m3s522ms alt=755.0 vel=14.36 bear=0.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858498,74,636612 acc=3 et=+4d7h23m5s439ms alt=754.0 vel=14.25 bear=0.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,858754,74,636598 acc=3 et=+4d7h23m7s547ms alt=753.0 vel=14.38 bear=359.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859034,74,636602 acc=4 et=+4d7h23m9s537ms alt=752.0 vel=14.82 bear=359.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859301,74,636607 acc=4 et=+4d7h23m11s545ms alt=752.0 vel=15.11 bear=359.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859579,74,636629 acc=4 et=+4d7h23m13s534ms alt=751.0 vel=15.22 bear=359.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,859852,74,636622 acc=4 et=+4d7h23m15s548ms alt=751.0 vel=15.53 bear=359.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860140,74,636623 acc=4 et=+4d7h23m17s539ms alt=751.0 vel=15.74 bear=0.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860413,74,636618 acc=4 et=+4d7h23m19s532ms alt=748.0 vel=15.85 bear=359.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,860694,74,636642 acc=4 et=+4d7h23m21s541ms alt=745.0 vel=15.99 bear=0.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861009,74,636626 acc=4 et=+4d7h23m23s540ms alt=745.0 vel=16.29 bear=359.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861313,74,636638 acc=4 et=+4d7h23m25s541ms alt=743.0 vel=16.66 bear=359.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861604,74,636628 acc=4 et=+4d7h23m27s528ms alt=740.0 vel=16.72 bear=359.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,861899,74,636631 acc=4 et=+4d7h23m29s534ms alt=740.0 vel=16.49 bear=0.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862197,74,636635 acc=4 et=+4d7h23m31s540ms alt=746.0 vel=16.31 bear=0.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862505,74,636623 acc=5 et=+4d7h23m33s538ms alt=742.0 vel=16.5 bear=0.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,862802,74,636648 acc=5 et=+4d7h23m35s531ms alt=740.0 vel=16.52 bear=0.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863099,74,636653 acc=6 et=+4d7h23m37s530ms alt=740.0 vel=16.85 bear=0.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863380,74,636653 acc=5 et=+4d7h23m39s539ms alt=740.0 vel=17.05 bear=0.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,863683,74,636646 acc=5 et=+4d7h23m41s531ms alt=739.0 vel=17.38 bear=0.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864010,74,636654 acc=4 et=+4d7h23m43s527ms alt=739.0 vel=17.81 bear=359.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864347,74,636662 acc=4 et=+4d7h23m45s535ms alt=737.0 vel=18.31 bear=0.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,864705,74,636646 acc=5 et=+4d7h23m47s535ms alt=736.0 vel=18.62 bear=2.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865040,74,636671 acc=4 et=+4d7h23m49s535ms alt=735.0 vel=19.15 bear=2.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865388,74,636660 acc=5 et=+4d7h23m51s536ms alt=734.0 vel=19.42 bear=0.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,865747,74,636655 acc=5 et=+4d7h23m53s534ms alt=734.0 vel=19.6 bear=359.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,866099,74,636658 acc=5 et=+4d7h23m55s543ms alt=732.0 vel=19.88 bear=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,866438,74,636674 acc=5 et=+4d7h23m57s540ms alt=731.0 vel=19.74 bear=0.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,866802,74,636666 acc=5 et=+4d7h23m59s546ms alt=729.0 vel=19.57 bear=0.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867153,74,636651 acc=7 et=+4d7h24m1s533ms alt=729.0 vel=19.14 bear=0.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867494,74,636641 acc=5 et=+4d7h24m3s537ms alt=728.0 vel=18.81 bear=0.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867816,74,636655 acc=7 et=+4d7h24m5s525ms alt=729.0 vel=18.51 bear=3.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868104,74,636663 acc=5 et=+4d7h24m7s572ms alt=725.0 vel=16.06 bear=4.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868388,74,636687 acc=5 et=+4d7h24m9s545ms alt=723.0 vel=14.19 bear=4.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868627,74,636711 acc=4 et=+4d7h24m11s542ms alt=724.0 vel=11.47 bear=4.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868801,74,636740 acc=5 et=+4d7h24m13s544ms alt=724.0 vel=8.74 bear=4.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868918,74,636822 acc=5 et=+4d7h24m15s567ms alt=724.0 vel=6.41 bear=36.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868990,74,636916 acc=7 et=+4d7h24m17s526ms alt=724.0 vel=4.87 bear=93.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868868,74,637047 acc=4 et=+4d7h24m19s542ms alt=726.0 vel=7.01 bear=157.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868740,74,637056 acc=4 et=+4d7h24m21s542ms alt=730.0 vel=8.54 bear=177.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868576,74,637065 acc=4 et=+4d7h24m23s546ms alt=730.0 vel=8.79 bear=177.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868426,74,637074 acc=6 et=+4d7h24m25s540ms alt=731.0 vel=8.62 bear=178.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868254,74,637077 acc=5 et=+4d7h24m27s540ms alt=730.0 vel=9.51 bear=178.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868091,74,637088 acc=4 et=+4d7h24m29s566ms alt=731.0 vel=9.46 bear=178.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867920,74,637092 acc=5 et=+4d7h24m31s537ms alt=731.0 vel=9.45 bear=179.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867766,74,637113 acc=4 et=+4d7h24m33s535ms alt=733.0 vel=7.58 bear=182.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867641,74,637064 acc=3 et=+4d7h24m35s549ms alt=734.0 vel=6.85 bear=206.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867568,74,636947 acc=4 et=+4d7h24m37s547ms alt=735.0 vel=5.62 bear=236.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867558,74,636821 acc=4 et=+4d7h24m39s535ms alt=731.0 vel=4.84 bear=253.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867535,74,636681 acc=4 et=+4d7h24m41s539ms alt=730.0 vel=4.89 bear=271.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867535,74,636557 acc=4 et=+4d7h24m43s539ms alt=731.0 vel=4.53 bear=264.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867543,74,636426 acc=4 et=+4d7h24m45s548ms alt=729.0 vel=4.34 bear=260.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867521,74,636336 acc=4 et=+4d7h24m47s544ms alt=729.0 vel=3.87 bear=259.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867504,74,636250 acc=4 et=+4d7h24m49s535ms alt=730.0 vel=4.53 bear=261.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867491,74,636145 acc=3 et=+4d7h24m51s542ms alt=730.0 vel=4.55 bear=261.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867478,74,636042 acc=4 et=+4d7h24m53s531ms alt=732.0 vel=4.11 bear=260.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867401,74,635702 acc=5 et=+4d7h25m1s537ms alt=731.0 vel=3.23 bear=253.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867387,74,635624 acc=4 et=+4d7h25m3s531ms alt=729.0 vel=3.3 bear=253.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867375,74,635537 acc=4 et=+4d7h25m5s571ms alt=728.0 vel=3.98 bear=257.5 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867335,74,635418 acc=4 et=+4d7h25m7s564ms alt=731.0 vel=5.52 bear=261.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867335,74,635142 acc=4 et=+4d7h25m11s537ms alt=730.0 vel=6.33 bear=262.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867324,74,634998 acc=4 et=+4d7h25m13s541ms alt=733.0 vel=6.24 bear=262.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867307,74,634821 acc=4 et=+4d7h25m15s540ms alt=734.0 vel=8.22 bear=262.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867257,74,634609 acc=4 et=+4d7h25m17s547ms alt=732.0 vel=7.83 bear=261.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867229,74,634410 acc=4 et=+4d7h25m19s572ms alt=732.0 vel=8.03 bear=261.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867209,74,634215 acc=6 et=+4d7h25m21s531ms alt=732.0 vel=8.03 bear=262.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867174,74,633921 acc=4 et=+4d7h25m24s444ms alt=734.0 vel=8.44 bear=272.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867243,74,633599 acc=6 et=+4d7h25m27s517ms alt=735.0 vel=8.13 bear=289.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867332,74,633402 acc=3 et=+4d7h25m29s539ms alt=735.0 vel=8.52 bear=307.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867413,74,633279 acc=4 et=+4d7h25m31s550ms alt=735.0 vel=8.46 bear=311.6 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867523,74,633116 acc=4 et=+4d7h25m33s551ms alt=734.0 vel=8.72 bear=313.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867616,74,632947 acc=4 et=+4d7h25m35s543ms alt=732.0 vel=8.76 bear=313.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867729,74,632798 acc=3 et=+4d7h25m37s531ms alt=733.0 vel=8.9 bear=313.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867835,74,632643 acc=4 et=+4d7h25m39s568ms alt=732.0 vel=8.11 bear=312.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867934,74,632498 acc=5 et=+4d7h25m41s532ms alt=732.0 vel=8.1 bear=313.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,867986,74,632468 acc=4 et=+4d7h25m43s532ms alt=730.0 vel=4.2 bear=312.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868021,74,632388 acc=5 et=+4d7h25m45s607ms alt=730.0 vel=3.69 bear=309.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868063,74,632319 acc=10 et=+4d7h25m47s541ms alt=734.0 vel=3.67 bear=310.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868200,74,632113 acc=6 et=+4d7h25m49s550ms alt=733.0 vel=7.81 bear=312.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868293,74,631995 acc=5 et=+4d7h25m51s543ms alt=729.0 vel=7.97 bear=314.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868415,74,631847 acc=4 et=+4d7h25m53s541ms alt=730.0 vel=8.04 bear=316.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868530,74,631698 acc=3 et=+4d7h25m55s539ms alt=729.0 vel=8.2 bear=315.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868639,74,631553 acc=4 et=+4d7h25m57s545ms alt=728.0 vel=8.38 bear=314.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868759,74,631398 acc=3 et=+4d7h25m59s578ms alt=726.0 vel=8.51 bear=313.7 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868864,74,631247 acc=4 et=+4d7h26m1s532ms alt=726.0 vel=8.5 bear=313.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,868972,74,631072 acc=4 et=+4d7h26m3s544ms alt=727.0 vel=8.59 bear=312.3 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869068,74,630929 acc=3 et=+4d7h26m5s548ms alt=727.0 vel=8.5 bear=312.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869172,74,630793 acc=4 et=+4d7h26m7s565ms alt=726.0 vel=7.56 bear=313.9 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869268,74,630662 acc=5 et=+4d7h26m9s527ms alt=726.0 vel=7.57 bear=316.8 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869312,74,630595 acc=5 et=+4d7h26m11s533ms alt=728.0 vel=5.26 bear=314.0 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869340,74,630544 acc=3 et=+4d7h27m17s552ms alt=727.0 vel=1.71 bear=300.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869321,74,630478 acc=3 et=+4d7h27m21s542ms alt=728.0 vel=0.85 bear=277.4 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869310,74,630400 acc=3 et=+4d7h27m29s541ms alt=728.0 vel=1.32 bear=232.1 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869266,74,630375 acc=6 et=+4d7h29m13s601ms alt=727.0 vel=1.38 bear=219.2 {Bundle[mParcelledData.dataSize=40]}]\n" +
                "Location[gps 42,869252,74,630368 acc=7 et=+4d7h29m14s600ms alt=726.0 vel=1.21 bear=222.2 {Bundle[mParcelledData.dataSize=40]}]"));

        List<LatLng> route = new ArrayList<>();
        for (Location location : track) {
            route.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        mapInterface.showRoute(route);
    }

}
