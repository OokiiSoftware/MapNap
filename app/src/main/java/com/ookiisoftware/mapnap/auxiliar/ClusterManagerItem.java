package com.ookiisoftware.mapnap.auxiliar;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.modelo.ClusterMarkerItem;
import com.ookiisoftware.mapnap.modelo.ClusterRendererItem;
import com.ookiisoftware.mapnap.modelo.Endereco;

import java.util.LinkedList;
import java.util.List;

public class ClusterManagerItem<T extends ClusterItem> extends ClusterManager<ClusterMarkerItem> {

    private static final String TAG = "ClusterManagerItem";

    public static boolean canSearchEndereco = false;

    private final GoogleMap map;
    private final List<ClusterMarkerItem> markers;
    private final Context context;
    private final TextView txt_msg;
    public LatLng cameraPosition;
    public Endereco endereco;

    public ClusterManagerItem(Context context, GoogleMap map, List<ClusterMarkerItem> markers, TextView txt_msg) {
        super(context, map);
        this.map = map;
        this.txt_msg = txt_msg;
        this.markers = markers;
        this.context = context;
    }

    @Override
    public void onCameraIdle() {
        super.onCameraIdle();
        cameraPosition = map.getCameraPosition().target;

        if (canSearchEndereco)
            endereco = GetEndereco(cameraPosition);
        String enderecoString;
        if(endereco == null){
            enderecoString = "Endereço não encontrado";
        } else {
            enderecoString =  endereco.getRua() + " " + endereco.getBairro();
        }
        txt_msg.setText(enderecoString);
    }

    public void Atualizar(){
        Log.e("ClusterManagerItem", "Atualizar");
        for (ClusterMarkerItem m : markers){
            ClusterRendererItem rendererItem = (ClusterRendererItem) getRenderer();
            rendererItem.setUpdateMarker(m);
        }
    }


    private Endereco GetEndereco(LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Import.get.locale);
        Endereco endereco = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address address = addresses.get(0);

            endereco = new Endereco();
            endereco.setBairro(address.getSubLocality());
            endereco.setEstado(address.getAdminArea());
            endereco.setCidade(address.getSubAdminArea());
            endereco.setRua(address.getThoroughfare());

            if(endereco.getBairro() != null)
                endereco.setBairro(endereco.getBairro().toUpperCase());
            if(endereco.getEstado() != null)
                endereco.setEstado(endereco.getEstado().toUpperCase());
            if(endereco.getCidade() != null)
                endereco.setCidade(endereco.getCidade().toUpperCase());
            if(endereco.getRua() != null)
                endereco.setRua(endereco.getRua().toUpperCase());

            /*getCountryName(Brasil), getFeatureName(446), getLocality(null), getLocale(pt-Br), getPostalCode(06570-000)*/

            if(endereco.getBairro() == null || endereco.getEstado() == null || endereco.getCidade() == null || endereco.getRua() == null)
                return null;
        }catch (Exception ex) {
            endereco = null;
            Log.e(TAG, "GetEndereco: " + ex.getMessage());
        }
        return endereco;
    }

}
