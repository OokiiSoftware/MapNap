package com.ookiisoftware.mapnap.modelo;

import android.os.IBinder;
import android.os.RemoteException;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.internal.maps.zzt;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarkerItem implements ClusterItem {

    private LatLng mPosition;
    private final String mId;
    private String mTitle;
    private String mSnippet;
    private icone mIcone;

    public ClusterMarkerItem(String id, LatLng position, final String title, final String snippet, icone icone) {
        mId = id;
        mPosition = position;
        mTitle = title;
        mIcone = icone;
        mSnippet = snippet;
    }

    public enum icone{
        Manutencao,
        GPon, PacPon,
        GponCheio, PacPonCheio
    }
    public icone Icone;

    public void setIcone(icone mIcone) {
        this.mIcone = mIcone;
    }

    public icone getIcone() {
        return mIcone;
    }

    public String getId(){
        return mId;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public void setmPosition(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmSnippet(String value){
        mSnippet = value;
    }
}
