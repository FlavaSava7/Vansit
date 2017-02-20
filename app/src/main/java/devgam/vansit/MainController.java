package devgam.vansit;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class MainController extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{


    FragmentManager fragmentManager;// this is used for the ChangeFrag method



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);// we should show this when he is logged
        if(Util.isLogged())
        {
            fab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    AddOffer addOfferPage = new AddOffer();
                    Util.ChangeFrag(addOfferPage,fragmentManager);
                }
            });
        }else
        {
            Log.v("Main","onCreate "+Util.isLogged());
            fab.setVisibility(View.GONE);
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager  = getSupportFragmentManager();


        // to check for the first time if we have internet , then internet listener will keep checking
        if(Util.CheckConnection(this)) //INTERNET IS ON
            Util.IS_USER_CONNECTED =true;
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Util.IS_USER_CONNECTED =Util.isOnlineApi18(this);
            //Log.v("Fragment:", "MainActivity : NETWORK(18 Api) IS " + ApplicationLinking.JuBooks_isUserConnected);
        }

        Main mainPage = new Main();
        Util.ChangeFrag(mainPage,fragmentManager);
        //Log.v("Main:","Util.IS_USER_CONNECTED: "+Util.IS_USER_CONNECTED);



    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    public void onBackPressed()
    {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 1)
        {
            finish();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings)
        {
            FirebaseAuth.getInstance().signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        /**
        *
        * Check what ID user clicked (change IDs in activity_main_drawer.xml)
        * Then navigate to the selected Page
        * each time u need to create new instance and call ChangeFrag method
        */
        if (id == R.id.nav_login)
        {
            if(Util.isLogged())//user is logged in
            {
                Profile profilePage = new Profile();
                Util.ChangeFrag(profilePage,fragmentManager);
            }
            else//user is logged out
            {
                Login loginPage = new Login();
                Util.ChangeFrag(loginPage,fragmentManager);
            }


        } else if (id == R.id.nav_register)
        {
            Registration registerPage = new Registration();
            Util.ChangeFrag(registerPage,fragmentManager);
        } else if (id == R.id.nav_main)
        {
            Main mainPage = new Main();
            Util.ChangeFrag(mainPage,fragmentManager);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
