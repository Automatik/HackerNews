package emilsoft.hackernews;

import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import emilsoft.hackernews.connectivity.ConnectivityProvider;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.ActivityMainBinding;
import emilsoft.hackernews.fragment.HomeFragment;

public class MainActivity extends AppCompatActivity implements CustomTabActivityHelper.LaunchUrlCallback,
        ConnectivityProvider.ConnectivityStateListener {

    private AppBarConfiguration mAppBarConfiguration;
    private CustomTabActivityHelper customTabActivityHelper;
    private ConnectivityProvider connectivityProvider;
    public static final String TAG = "Emil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_topstories, R.id.nav_newstories, R.id.nav_beststories,
                R.id.nav_showstories, R.id.nav_askstories, R.id.nav_jobstories)
                .setOpenableLayout(drawer)
//                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        customTabActivityHelper = new CustomTabActivityHelper();
//        customTabActivityHelper.setConnectionCallback(this);

        connectivityProvider = ConnectivityProvider.createProvider(this);
        // check could be made also here
        //isStateConnected(connectivityProvider.getNetworkState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
        connectivityProvider.addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
        connectivityProvider.removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customTabActivityHelper.setConnectionCallback(null);
    }


    @Override
    public void onMayLaunchUrl(Uri priorUri, List<Bundle> otherLikelyBundles) {
        if(customTabActivityHelper != null) {
            customTabActivityHelper.mayLaunchUrl(priorUri, null, otherLikelyBundles);
        }
    }

    @Override
    public void onStateChange(ConnectivityProvider.NetworkState state) {
        Log.v(TAG, "MainActivity/isConnected " + ConnectivityProvider.isStateConnected(state));
    }


}
