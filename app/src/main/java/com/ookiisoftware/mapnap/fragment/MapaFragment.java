package com.ookiisoftware.mapnap.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.ookiisoftware.mapnap.R;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.auxiliar.Import.Log;
import com.ookiisoftware.mapnap.modelo.Caixa;
import com.ookiisoftware.mapnap.modelo.CaixaAlterada;
import com.ookiisoftware.mapnap.auxiliar.ClusterManagerItem;
import com.ookiisoftware.mapnap.modelo.ClusterRendererItem;
import com.ookiisoftware.mapnap.modelo.Endereco;
import com.ookiisoftware.mapnap.modelo.ClusterMarkerItem;
import com.ookiisoftware.mapnap.modelo.PerfilDeAcesso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static com.ookiisoftware.mapnap.auxiliar.Import.get.BarraDeLoad;

public class MapaFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener, ClusterManager.OnClusterItemClickListener<ClusterMarkerItem> {

    //region VARIAVEIS

    private static final String TAG = "MapaFragment";

    private int acao = 0;//define a ação do app no AlertDialog
    private static final int ACAO_ADD_MARKER = 650;
    private static final int ACAO_FECHAR = 264;
    private static final int ACAO_SALVAR = 134;
    private static final int ACAO_ATUALIZAR = 810;
    private static final int ACAO_EXCLUIR = 229;

    private final int CAIXA_TIPO_1 = 8, CAIXA_TIPO_2 = 16;
    private PerfilDeAcesso meuPerfil;

    private Integer numberPickerValue;
    private Integer PonValue;

    private List<Caixa> caixas = new LinkedList<>();
    private List<ClusterMarkerItem> markers = new LinkedList<>();
    private Caixa caixa_selecionada;
    private EditText et_Pesquisa;

    private View view;
    private FloatingActionButton fab_alert, fab_botton, fab_top;
    private AlertDialog.Builder alert_Dialog;
    private LocationManager locationManager;
    private ProgressBar progressBar;
    private TextView txt_msg;
    private Handler AddMarkerAsync = new Handler();

    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    private Marker markerIndicador;
    private ClusterManagerItem<ClusterMarkerItem> clusterManager;
    private boolean primeiraAtualizacaoDoMapa = true;

    private DatabaseReference firebase;
    private ValueEventListener valueEventListener;
    private Async async = new Async();

    //endregion

    public MapaFragment() {}

    //region SOBRESCRITAS DE MÉTODOS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mapa, viewGroup, false);
        Init(view);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapView mapView = (MapView) view.findViewById(R.id.map);
        if(mapView != null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        try
        {
            boolean semAcesso = false;
            meuPerfil = Import.usuario.getPerfil();
            if(meuPerfil != null) {
                if(meuPerfil.isCaixaAdicionar())
                    Fab(ACAO_FECHAR);
                else
                    semAcesso = true;
            }
            else {
                semAcesso = true;
            }

            if(semAcesso){
                fab_top.hide();
                fab_botton.hide();
                fab_alert.hide();
            }

            if(clusterManager != null)
            {
                AddMarkerAsync.removeCallbacks(delayParaAddMarkers);
                AddMarkerAsync.postDelayed(delayParaAddMarkers, Constantes.MENU_TIME_TO_HIDE);

                primeiraAtualizacaoDoMapa = true;
                Log.m(TAG, "RESUME");
            }

            firebase.addValueEventListener(valueEventListener);
            // Solicita atualizações na inicialização : Pega a localização do usuario em tempo real
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, this);
            }
        }
        catch (SecurityException e) {
            Log.e(TAG, "onResume", e.getMessage());
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            if(clusterManager != null)
                clusterManager.clearItems();
            markers.clear();
            firebase.removeEventListener(valueEventListener);
            // Remove as atualizações do listener de localização quando a atividade estiver em pausa
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e(TAG, "onPause", e.getMessage());
        }
    }

    //endregion

    //region Métodos pra uso do Mapa

    @Override
    public void onMapReady(GoogleMap googleMap){

        MapsInitializer.initialize(getContext());
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);// Desativa 2 botões ao clicar em um marker (eles fazem abrir o app google Maps)
            mMap.setOnMapClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        } catch (SecurityException ex) {
            Log.e(TAG, "onMapReady: ", ex.getMessage());
            Import.snakeBar(view, "Conceda acesso ao GPS");
        }

        clusterManager = new ClusterManagerItem<>(getContext(), mMap, markers, txt_msg);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setRenderer(new ClusterRendererItem(getContext(), mMap, clusterManager));
        clusterManager.setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<ClusterMarkerItem>());

        mMap.setOnInfoWindowClickListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnCameraIdleListener(clusterManager);

        markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        BarraDeLoad(progressBar, false);
    }
    @Override
    public void onMapClick(LatLng latLng) {
        Fab(ACAO_FECHAR);
    }

    @Override
    public boolean onClusterItemClick(ClusterMarkerItem clusterItem) {
        try {
            caixa_selecionada = ProcurarCaixa(clusterItem.getId());
            if(caixa_selecionada != null)
                Fab(ACAO_ATUALIZAR);
            ClusterRendererItem rendererItem = (ClusterRendererItem) clusterManager.getRenderer();
            rendererItem.setUpdateMarker(clusterItem);
            Log.m(TAG, "onClusterItemClick","Nome" + clusterItem.getTitle());
            Log.m(TAG, "onClusterItemClick","Snipet" + clusterItem.getSnippet());
        } catch (SecurityException e) {
            Log.e(TAG, "onClusterItemClick", e.getMessage());
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!TemInternet())
            return;
        // ao abrir o app a camera da zoom no local do dispositivo
        if(primeiraAtualizacaoDoMapa){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng latLng = new LatLng(lat, lng);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getMaxZoomLevel() - 2));
            primeiraAtualizacaoDoMapa = false;
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {
        Import.toast(getContext(), provider + " ativado");
    }
    @Override
    public void onProviderDisabled(String provider) {
        Import.toast(getContext(), "Por favor ative o GPS e a Internet");
    }

    //endregion

    //region Criação de popups pra manipulação das caixas

    private void Alert_Dialog(String titulo, String mensagem) {
        alert_Dialog = new AlertDialog.Builder(getContext());
        alert_Dialog.setCancelable(false);
        alert_Dialog.setTitle(titulo);
        alert_Dialog.setIcon(R.drawable.ic_chart);

        switch (acao) {
            case ACAO_SALVAR:
                Alert_Salvar(mensagem);
                break;
            case ACAO_ATUALIZAR:
                Alert_Update(caixa_selecionada);
                break;
            case ACAO_EXCLUIR:
                Alert_Excluir(mensagem);
                break;
        }
        //cria o AlertDialog
        AlertDialog alerta = alert_Dialog.create();
        //Exibe
        alerta.show();
    }

    private void Alert_Salvar(String mensagem) {
        alert_Dialog.setMessage(mensagem);
        final EditText input = new EditText(getContext());
        final RadioGroup radioGroup = new RadioGroup(getContext());
        final RadioButton gPon = new RadioButton(getContext());
        RadioButton pacPon = new RadioButton(getContext());
        gPon.setText("GPon");
        pacPon.setText("PacPon");

        radioGroup.addView(gPon);
        radioGroup.addView(pacPon);
        gPon.toggle();

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        radioGroup.setLayoutParams(lp);
        linearLayout.setLayoutParams(lp2);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Número da caixa");
        linearLayout.addView(radioGroup);
        linearLayout.addView(input);
        alert_Dialog.setView(linearLayout);

        //define um botão como negativo.
        alert_Dialog.setNegativeButton(CAIXA_TIPO_1 + " Portas", new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(DialogInterface arg0, int arg1) {
                String numero = input.getText().toString();
                int pon = gPon.isChecked() ? 0 : 1;
                double lat = clusterManager.cameraPosition.latitude;// centroDoMapa.latitude;
                double lng = clusterManager.cameraPosition.longitude;// centroDoMapa.longitude;
                SalvarCaixa(lat, lng, CAIXA_TIPO_1, numero, pon);
            }
        });

        //define um botão como positivo
        alert_Dialog.setPositiveButton(CAIXA_TIPO_2 + " Portas", new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(DialogInterface arg0, int arg1) {
                String numero = input.getText().toString();
                int pon = gPon.isChecked() ? 0 : 1;
                double lat = clusterManager.cameraPosition.latitude;//centroDoMapa.latitude;
                double lng = clusterManager.cameraPosition.longitude;//centroDoMapa.longitude;
                SalvarCaixa(lat, lng, CAIXA_TIPO_2, numero, pon);
            }
        });


        //define um botão de cancelar.
        alert_Dialog.setNeutralButton("Cancelar", null);
    }

    private void Alert_Update(Caixa c) {

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setLayoutParams(lp);

        numberPickerValue = null;
        PonValue = null;

        if(c.getClientes().size() == 0){
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.popup_number_picker, null);

            NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
            numberPicker.setMaxValue(caixa_selecionada.getPortas());
            numberPicker.setMinValue(0);
            numberPicker.setValue(0);
            numberPicker.setWrapSelectorWheel(false);
            numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    numberPickerValue = picker.getValue();
                }
            });
            linearLayout.addView(dialogView);
        }

        if(c.getPon() == 1){
            final Switch pon = new Switch(getContext());
            pon.setLayoutParams(lp2);
            pon.setText("Tecnologia GPon");
            pon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    pon.setText(isChecked ? "Tecnologia GPon": "Tecnologia PacPon");
                    PonValue = isChecked ? 0 : 1;
                }
            });
            linearLayout.addView(pon);
        }

        alert_Dialog.setView(linearLayout);

        alert_Dialog.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Fab(ACAO_FECHAR);
                AtualizarCaixa(caixa_selecionada, PonValue, numberPickerValue);
            }
        });

        alert_Dialog.setNeutralButton("Cancelar", null);
    }

    private void Alert_Excluir(String mensagem) {
        alert_Dialog.setMessage(mensagem);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputSenha = new EditText(getContext());
        final EditText inputMotivo = new EditText(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        inputSenha.setLayoutParams(lp);
        inputMotivo.setLayoutParams(lp);
        inputSenha.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        inputMotivo.setInputType(InputType.TYPE_CLASS_TEXT);
        inputSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
        inputSenha.setHint("Informe sua senha");
        inputMotivo.setHint("Informe o motivo da esclusão");

        linearLayout.addView(inputSenha);
        linearLayout.addView(inputMotivo);

        alert_Dialog.setView(linearLayout);

        //define um botão como positivo
        alert_Dialog.setPositiveButton("Excluir", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface arg0, int arg1) {
                String senha = inputSenha.getText().toString();
                String motivo = inputMotivo.getText().toString();
                if(!ExcluirCaixa(caixa_selecionada, senha, motivo))
                    Alert_Dialog(Alert_Text_Titulo(), Alert_Text_Mensagem());
            }
        });

        //define um botão de cancelar.
        alert_Dialog.setNeutralButton("Cancelar", null);
    }

    private void SalvarCaixa(double lat, double lng, int portas, String numero, int pon) {
        if(!TemInternet())
            return;
        try {
            if(numero.trim().isEmpty()) {
                Import.snakeBar(view, "Informe o número da caixa");
                Alert_Dialog(Alert_Text_Titulo(), Alert_Text_Mensagem());
                Log.m(TAG, "SalvarCaixa","(" + numero + ") - Numero vazio");
                return;
            }
            Endereco endereco = clusterManager.endereco;
            if(endereco == null){
                Import.snakeBar(view, "Endereço não encontrado. Mova um pouco para o lado");
                Alert_Dialog(Alert_Text_Titulo(), Alert_Text_Mensagem());
                Log.m(TAG, "SalvarCaixa","Endereço nulo");
                return;
            }

            Caixa c = new Caixa();
            c.setNome("CAIXA NAP " + numero);
            c.setLatitude(lat);
            c.setLongitude(lng);
            c.setPon(pon);
            c.setExcluido(false);
            c.setPortas(portas);
            c.setData(Import.get.Data());
            c.setId_usuario(Import.usuario.getId(getContext(), false));
            c.setEndereco(endereco);

            for (Caixa d : caixas)
                if (d.getNome().equals(c.getNome())) {
                    Import.toast(getContext(), "Já existe uma caixa com este número");
                    Alert_Dialog(Alert_Text_Titulo(), Alert_Text_Mensagem());
                    Log.m(TAG, "SalvarCaixa","Numero repetido");
                    return;
                }

            Fab(ACAO_FECHAR);
                c.Salvar(false);
        } catch (SecurityException e) {
            Log.e(TAG, "SalvarCaixa", e.getMessage());
        }
    }

    private void AtualizarCaixa(Caixa c, Integer pon, Integer portasUsadas) {
        Caixa c2 = Import.get.NewCaixa(c);
        CaixaAlterada.Salvar(c2);
//        DataCaixa.AddAlterada(c2);

        if (portasUsadas != null)
            for (int i = 0; i < portasUsadas; i++)
                c.getClientes().add((i + 1) + ";CLIENTE");

        c.setId_usuario(Import.usuario.getId(getContext(), false));
        c.setData(Import.get.Data());
        if (pon != null)
            if (pon < 1)
                c.setPon(pon);

        if (pon != null)
            c.SalvarPon();
        if (portasUsadas != null)
            c.SalvarClientes();
    }

    private boolean ExcluirCaixa(Caixa c, String senha, String motivo){
        if(!TemInternet())
            return false;

        if(senha.trim().isEmpty()){
            Import.snakeBar(view, "Informe sua senha");
            return false;
        }
        else if (motivo.trim().isEmpty()){
            Import.snakeBar(view, "Informe o motivo da exclusão");
            return false;
        }
        else if(!senha.equals(Import.usuario.getSenha(getContext()))) {
            Import.snakeBar(view, "Senha incorreta");
            return false;
        }
        else {
            c.setMotivo(motivo);
            Fab(ACAO_FECHAR);
            c.Remove();
            return true;
        }
    }

    private void CaixaEmManutencao(Caixa c, boolean acao) {
        c.setData(Import.get.Data());
        c.setId_usuario(Import.usuario.getId(getContext(), false));
        c.SalvarEmManutencao(acao);
    }

    private String Alert_Text_Titulo() {
        switch (acao){
            case ACAO_SALVAR:
                return  "Salvar caixa neste local";
            case ACAO_ATUALIZAR:
                String titulo = String.format(Import.get.locale, "Atualizar %s", caixa_selecionada.getNome());
                String[] texto = titulo.split("[.]");
                return texto[0];
            case ACAO_EXCLUIR:
                return "Excluir caixa";
        }
        return "";
    }

    private String Alert_Text_Mensagem() {
        switch (acao) {
            case ACAO_SALVAR:
                return "Selecione a quantidade de portas que contém na caixa";
            case ACAO_ATUALIZAR:
                int i = caixa_selecionada.getClientes().size();
                if(i == 1)
                    return String.format(Import.get.locale,"%d Porta usada", i);
                else
                    return String.format(Import.get.locale,"%d Portas usadas", i);
            case ACAO_EXCLUIR:
                return "Excluir esta caixa";
        }
        return "";
    }

    //endregion

    //region métodos Auxiliares

    private void Init(View view) {
        if (!CheckGooglePlayServices()) {
            Import.toast(getContext(), "Atualize o Google Play Services");
            Log.e(TAG, "Init","Erro no Google Play Services");
        }
        try {
            //region Pegar elementos do layout pelo ID

            ImageButton Btn_Pesquisa = view.findViewById(R.id.action_pesquisa);
            et_Pesquisa = view.findViewById(R.id.et_Pesquisa);
            caixa_selecionada = new Caixa();
            progressBar = view.findViewById(R.id.progressBar);
            fab_top = view.findViewById(R.id.fab_top);
            fab_botton = view.findViewById(R.id.fab_botton);
            fab_alert = view.findViewById(R.id.fab_alert);
            txt_msg = view.findViewById(R.id.txt_msg);

            //endregion

            firebase = Import.getFirebase.getRaiz().child(Constantes.Firebase.CHILD_CAIXAS);
            BarraDeLoad(progressBar, true);

            //region Botões flutuantes Clickes

            fab_botton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (Integer.parseInt(fab_botton.getTag().toString())) {
                        case ACAO_ADD_MARKER:
                            Fab(ACAO_ADD_MARKER);
                            break;
                        case ACAO_FECHAR:
                            Fab(ACAO_FECHAR);
                            break;
                        case ACAO_EXCLUIR:
                            acao = ACAO_EXCLUIR;
                            Alert_Dialog(Alert_Text_Titulo(), Alert_Text_Mensagem());
                            break;
                    }
                }
            });
            fab_top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acao = Integer.parseInt(fab_top.getTag().toString());
                    Alert_Dialog(Alert_Text_Titulo(), Alert_Text_Mensagem());
                }
            });

            fab_alert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean acao = !caixa_selecionada.isIsEmManutencao();
                    CaixaEmManutencao(caixa_selecionada, acao);
                    Fab(ACAO_FECHAR);
                    BarraDeLoad(progressBar, true);
                }
            });

            //endregion

            Btn_Pesquisa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!et_Pesquisa.getText().toString().trim().isEmpty())
                        Pesquisar(et_Pesquisa.getText().toString());
                }
            });

            //region Fica ouvindo se tem alteração nas caixas

            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Caixa c = dataSnapshot.getValue(Caixa.class);
                            if (c != null) {
                                c.setId(dataSnapshot.getKey());
                                Caixa c2 = ProcurarCaixa(c.getId());
                                if(c2 == null){
                                    caixas.add(c);
                                    AddMarkerAsync.removeCallbacks(delayParaAddMarkers);
                                    AddMarkerAsync.postDelayed(delayParaAddMarkers, Constantes.MENU_TIME_TO_HIDE);
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Caixa c = dataSnapshot.getValue(Caixa.class);
                            if (c != null) {
                                c.setId(dataSnapshot.getKey());
                                Caixa d = ProcurarCaixa(c.getId());
                                if (d != null) {
                                    caixas.set(caixas.indexOf(d), c);
                                    async.AtualizarMarker(c);
                                }
                            }
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                            Caixa c = dataSnapshot.getValue(Caixa.class);
                            if (c != null) {
                                c.setId(dataSnapshot.getKey());
                                Caixa d = ProcurarCaixa(c.getId());
                                if (d != null) {
                                    caixas.remove(d);
                                    async.RemoverMarker(c);
                                }
                            }
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };

            //endregion
            fab_top.hide();
            fab_botton.hide();
            fab_top.setTag("");
//        Fab(ACAO_FECHAR);
//        async.execute();
        } catch (SecurityException e) {
            Log.e(TAG, "Init", e.getMessage());
        }
    }

    private Runnable delayParaAddMarkers = new Runnable() {
        @Override
        public void run() {
            async.AddMarker();
        }};

    private void Pesquisar(String pesquisa) {
        for(Caixa c : caixas){
            if(c.getNome().contains(pesquisa))
            {
                // Ocultar o teclado
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                et_Pesquisa.setText("");
                LatLng local = new LatLng(c.getLatitude(), c.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(local));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getMaxZoomLevel()));

                ClusterMarkerItem m = MarkerFind(c.getId());
                if(m != null) {
//                    m.showInfoWindow();
//                    clusterManager.onMarkerClick();
                }
                break;
            }
        }
    }

    private void Fab(int opcao) {
        ClusterManagerItem.canSearchEndereco = false;
        if(meuPerfil.isCaixaAdicionar())
            switch (opcao){
            case ACAO_ADD_MARKER:{
                ClusterManagerItem.canSearchEndereco = true;
                fab_botton.setImageResource(R.drawable.ic_close);
                fab_botton.setTag(ACAO_FECHAR);
                fab_top.setImageResource(R.drawable.ic_ok);
                fab_top.setTag(ACAO_SALVAR);
                fab_top.show();
                txt_msg.setVisibility(View.VISIBLE);

                if(mMap != null) {
                    markerOptions.position(mMap.getCameraPosition().target);
//                    markerOptions.title("59");
                    markerIndicador = mMap.addMarker(markerOptions);
                    mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                        @Override
                        public void onCameraMove() {
                            LatLng latLng = mMap.getCameraPosition().target;
                            markerIndicador.setPosition(latLng);
                        }
                    });
                }
                Log.m(TAG, "ABRIU");
                break;
            }
            case ACAO_ATUALIZAR:{
                fab_botton.setImageResource(R.drawable.ic_excluir);
                fab_botton.setTag(ACAO_EXCLUIR);

                fab_top.setImageResource(R.drawable.ic_edit);
                fab_top.setTag(ACAO_ATUALIZAR);

                if(caixa_selecionada.getClientes().size() == 0 || caixa_selecionada.getPon() == 1) {
                    //o floatActionButton tem um bug quando troca o icone (as vezes não mostra o icone)
                    //a solução foi usar hide > show
                    fab_top.hide();
                    fab_top.show();
                }
                else
                    fab_top.hide();
                fab_alert.show();
                txt_msg.setVisibility(View.GONE);
                if(markerIndicador != null)
                    markerIndicador.remove();
                Log.m(TAG, "ATUALIZAR");
                break;
            }
            case ACAO_FECHAR:{
                fab_botton.setImageResource(R.drawable.ic_add);
                fab_botton.setTag(ACAO_ADD_MARKER);
                fab_botton.show();
                fab_top.hide();
                if(mMap != null)
                    mMap.setOnCameraMoveListener(null);

                fab_alert.hide();
                txt_msg.setVisibility(View.GONE);
                if(markerIndicador != null)
                    markerIndicador.remove();
                Log.m(TAG, "FECHOU");
                break;
            }
        }
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getContext());
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError((result))){
                googleAPI.getErrorDialog(getActivity(), result, 0).show();
            }
            return false;
        }
        return true;
    }

    private boolean TemInternet() {
        if (!Import.get.Internet(getContext()))
        {
            Import.toast(getContext(), "Verifique sua conexão com a internet");
            return false;
        }
        else
            return true;
    }

    private Caixa ProcurarCaixa(String id) {
        for (Caixa c : caixas)
            if (c.getId().equals(id))
                return c;
        return null;
    }

    private ClusterMarkerItem MarkerFind(String id){
        for(ClusterMarkerItem m : markers)
            if(m.getId().equals(id)) {
                return m;
            }
        return null;
    }

    //endregion

    @SuppressLint("StaticFieldLeak")
    private class Async {

        Async(){}

        // Usado pra add caixas por um arquivo kml
        private void Teste(){
            try{
                InputStream is = getActivity().getResources().openRawResource(R.raw.mapa);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String data;
                int i = 1;
                while ((data = reader.readLine()) != null){
                    String[] st = data.split(",");
                    double lng = Double.parseDouble(st[0]);
                    double lat = Double.parseDouble(st[1]);
                    SalvarCaixa(lat, lng,8, "" + i, 0);
                    Log.m(TAG, "Teste","Lat:" + lat + "," + lng);
                    i++;
                }
                is.close();
            }
            catch (IOException ex){
                Log.e(TAG, "Teste", ex.getMessage());
            }
        }

        private void AddMarker() {
            for (Caixa c : caixas)
            {
                try {
                    ClusterMarkerItem m = MarkerFind(c.getId());
                    if(m == null)
                    {
                        ClusterMarkerItem.icone icone = GetMarkerIcon(c);
                        LatLng local = new LatLng(c.getLatitude(), c.getLongitude());
                        ClusterMarkerItem clusterItem = new ClusterMarkerItem(c.getId(), local, c.getNome(), MarkerSnipet(c), icone);
                        clusterManager.addItem(clusterItem);
                        markers.add(clusterItem);
//                        Log.m(TAG, "AddMarker","Nome", c.getNome());
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "AddMarker", e.getMessage());
                }
            }
            clusterManager.cluster();

            BarraDeLoad(progressBar, false);
        }

        private ClusterMarkerItem.icone GetMarkerIcon(Caixa c) {
//            Log.e(TAG, "GetMarkerIcon", "Manu: " + c.getNome() + ": " + c.isIsEmManutencao());
            if (c.isIsEmManutencao())
                return ClusterMarkerItem.icone.Manutencao;
            return c.getStatus().equals("CHEIO") ?
                    c.getPon() == 0 ? ClusterMarkerItem.icone.GponCheio : ClusterMarkerItem.icone.PacPonCheio :
                    c.getPon() == 0 ? ClusterMarkerItem.icone.GPon : ClusterMarkerItem.icone.PacPon;
        }

        private void AtualizarMarker(Caixa c) {
            try {
                ClusterMarkerItem marker = null;
                for (ClusterMarkerItem m : markers)
                    if (c.getId().equals(m.getId())) {
                        marker = m;
                        break;
                    }

                if (marker == null) {
                    Log.m(TAG, "Async: AtualizarMarker","Marker não encontrado ID", c.getId());
                    return;
                }

//                Log.m(TAG, "Async: AtualizarMarker","Marker atualizado ", c.getNome());

                clusterManager.removeItem(marker);
                marker.setmSnippet(MarkerSnipet(c));
                marker.setIcone(GetMarkerIcon(c));
                clusterManager.addItem(marker);
                clusterManager.Atualizar();

                BarraDeLoad(progressBar, false);
            } catch (SecurityException e) {
                Log.e(TAG, "AtualizarMarker", e.getMessage());
            }
        }

        private void RemoverMarker(Caixa c) {
            for (ClusterMarkerItem m : markers)
            {
                try {
                    if (c.getId().equals(m.getId()))
                    {
                        Log.m(TAG, "RemoverMarker","Marker removido", m.getId());
                        markers.remove(m);
                        clusterManager.removeItem(m);
                        clusterManager.cluster();
                        break;
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "RemoverMarker", e.getMessage());
                }

            }

            BarraDeLoad(progressBar, false);
        }

        private String MarkerSnipet (Caixa c) {
            return String.format(Import.get.locale, "%s [%d Portas] [%d Usadas]", c.getPon() == 0 ? "GPon":"PacPon", c.getPortas(), c.getClientes().size());
        }
    }
}
