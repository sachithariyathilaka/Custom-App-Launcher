package io.github.sachithariyathilaka.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import com.sachith.popupsnackbar.PopupSnackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.sachithariyathilaka.R;
import io.github.sachithariyathilaka.adapter.GridViewAdapter;
import io.github.sachithariyathilaka.database.DbConnection;
import io.github.sachithariyathilaka.model.AppPackage;
import io.github.sachithariyathilaka.model.Apps;
import io.github.sachithariyathilaka.service.ClickListenerService;

public class AppListActivity extends AppCompatActivity implements ClickListenerService {

    private PackageManager packageManager;
    private List<Apps> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        //Setup UI
        init();
    }

    @Override
    public void onAppClick(Apps app) {
        if(app.getDisable()){
            ConstraintLayout view = findViewById(R.id.activity_app_list);
            new PopupSnackbar().snackBar(this,view,"This App is Disabled", 1);
        } else{
            startActivity(new Intent((packageManager.getLaunchIntentForPackage(app.getLabel().toString()))));
        }
    }

    //<editor-fold desc="Private Methods">

    //Setup UI
    private void init() {
        //Load apps from system
        loadApps();

        //Sort apps
        sortApps();

        //Go to Settings
        goToSettings();
    }

    //Go to Settings
    private void goToSettings() {
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppListActivity.this,SettingsActivity.class));
            }
        });
    }

    //Sort apps
    private void sortApps() {
        TextView sortText = findViewById(R.id.sort);
        sortText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(appList, new Comparator<Apps>() {
                    @Override
                    public int compare(Apps app1, Apps app2) {
                        return String.valueOf(app1.getName()).compareTo(String.valueOf(app2.getName()));
                    }
                });
                displayApps();
            }
        });
    }

    //Load apps from system
    private void loadApps() {
        packageManager = getPackageManager();
        appList = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(i,0);
        DbConnection database = Room.databaseBuilder(getApplicationContext(), DbConnection.class, "Apps").allowMainThreadQueries().build();
        List<AppPackage> disabledApps = database.getAppDao().getData();
        for(ResolveInfo resolveInfo: availableActivities){
            Apps newApp = new Apps();
            newApp.setLabel(resolveInfo.activityInfo.packageName);
            newApp.setName(resolveInfo.loadLabel(packageManager));
            newApp.setIcon(resolveInfo.loadIcon(packageManager));
            newApp.setDisable(false);
            appList.add(newApp);
        }

        for(int j = 0; j<disabledApps.size(); j++){
            for(int k=0; k<appList.size(); k++){
                if(appList.get(k).getLabel().equals(disabledApps.get(j).getName())){
                    appList.get(k).setDisable(true);
                }
            }
        }

        //Display apps in grid view
        displayApps();
    }

    //Display apps in grid view
    private void displayApps() {
        GridViewAdapter gridViewAdapter = new GridViewAdapter(appList,this,this);
        GridView appsView = findViewById(R.id.appGrid);
        appsView.setAdapter(gridViewAdapter);
    }

    //</editor-fold>
}