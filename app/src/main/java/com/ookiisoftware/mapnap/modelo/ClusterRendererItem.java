package com.ookiisoftware.mapnap.modelo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.ookiisoftware.mapnap.R;

public class ClusterRendererItem extends DefaultClusterRenderer<ClusterMarkerItem> {

    private final Context context;
    private final IconGenerator mClusterIconGenerator;

    public ClusterRendererItem(Context context, GoogleMap map, ClusterManager<ClusterMarkerItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        mClusterIconGenerator = new IconGenerator(context.getApplicationContext());

    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarkerItem item, MarkerOptions markerOptions) {
        markerOptions.icon(GetMarkerIcon(item.getIcone()));
    }

    @Override
    protected void onClusterItemRendered(ClusterMarkerItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterMarkerItem> cluster, MarkerOptions markerOptions) {

        final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.ic_lens_black_24dp);
        clusterIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);

        mClusterIconGenerator.setBackground(clusterIcon);

        //modificar preenchimento para números de um ou dois dígitos
        if (cluster.getSize() < 10) {
            int padding = 30;
            mClusterIconGenerator.setContentPadding(padding, padding - 10, padding, padding - 10);
        } else if (cluster.getSize() < 100){
            int padding = 40;
            mClusterIconGenerator.setContentPadding(padding, padding - 5, padding, padding - 5);
        } else {
            int padding = 50;
            mClusterIconGenerator.setContentPadding(padding - 5, padding, padding - 5, padding);
        }

        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    public void setUpdateMarker(ClusterMarkerItem clusterItem) {
        Marker marker = getMarker(clusterItem);
        if (marker != null) {
            marker.setIcon(GetMarkerIcon(clusterItem.getIcone()));
            marker.setSnippet(clusterItem.getSnippet());
        }
    }

    private BitmapDescriptor GetMarkerIcon(ClusterMarkerItem.icone icone)
    {
        if(icone == ClusterMarkerItem.icone.Manutencao)
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        if (icone == ClusterMarkerItem.icone.GPon)
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);//COM PONTO
        if (icone == ClusterMarkerItem.icone.GponCheio)
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);//COM PONTO
        if (icone == ClusterMarkerItem.icone.PacPon)
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        if (icone == ClusterMarkerItem.icone.PacPonCheio)
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);// SEM PONTO
        return null;
    }
}
