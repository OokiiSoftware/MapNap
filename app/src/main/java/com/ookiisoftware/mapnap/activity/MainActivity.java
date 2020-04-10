package com.ookiisoftware.mapnap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ookiisoftware.mapnap.R;
import com.ookiisoftware.mapnap.auxiliar.Constantes;
import com.ookiisoftware.mapnap.auxiliar.Import;
import com.ookiisoftware.mapnap.auxiliar.SectionsPagerAdapter;
import com.ookiisoftware.mapnap.auxiliar.SegundoPlanoService;
import com.ookiisoftware.mapnap.fragment.ConversasFragment;
import com.ookiisoftware.mapnap.modelo.Usuario;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(getApplicationContext(), SegundoPlanoService.class));
        Init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Import.cancelNotification(getApplication(), Constantes.NOTIFICACAO_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_pesquisa:
                break;
            case R.id.action_logout:
                LogOut();
                break;
            case R.id.action_sair:
                finishAffinity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() != 1){
            viewPager.setCurrentItem(1);
        }else {
            super.onBackPressed();
        }
    }


    private void Init(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);

        setSupportActionBar(toolbar);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getApplicationContext(), getSupportFragmentManager());

        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position != 2)
                    getSupportActionBar().show();
                else
                    getSupportActionBar().hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorAccent));
        tabs.setTabTextColors(ContextCompat.getColor(this, R.color.colorGray), ContextCompat.getColor(this, R.color.corClaro));
        tabs.setupWithViewPager(viewPager);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ConversasFragment conversasFragment = new ConversasFragment();
        transaction.add(R.id.view_pager, conversasFragment);
    }

    private void LogOut() {
        Import.usuario.setUsuarioLogado(getApplicationContext(), new Usuario());
        Import.getFirebase.getFirebaseAuth().signOut();
        IrProLogin();
    }
    private void IrProLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
