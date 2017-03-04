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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import devgam.vansit.JSON_Classes.Users;


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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Header text must be changed
        View header=navigationView.getHeaderView(0);
        TextView name = (TextView)header.findViewById(R.id.nav_header_main_name_text);
        TextView email = (TextView)header.findViewById(R.id.nav_header_main_email_text);
        ImageView img = (ImageView)header.findViewById(R.id.nav_header_main_img);
        name.setText("nimer esam");
        email.setText("nimeresam95@gmail.com");
        img.setImageResource(R.mipmap.ic_action_male);


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
        hideItem();



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
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            if(fab!=null)
                fab.setVisibility(View.GONE);
            FirebaseAuth.getInstance().signOut();
            Main mainPage = new Main();
            Util.ChangeFrag(mainPage,fragmentManager);


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


        } /*else if (id == R.id.nav_register)
        {
            Registration registerPage = new Registration();
            Util.ChangeFrag(registerPage,fragmentManager);
        }*/ else if (id == R.id.nav_main)
        {
            Main mainPage = new Main();
            Util.ChangeFrag(mainPage,fragmentManager);
        } else if(id == R.id.nav_my_account){
            myAccount myAccount = new myAccount();
            Util.ChangeFrag(myAccount, fragmentManager);
        } else if(id == R.id.nav_user) {
            //Temp calling to test setter valid or not !
            Users tempUserForTest = new Users("Nimer","Esam","Amman","0796546549","male","6","6","1966");//just for testing user information
            userInformation user = new userInformation(this,tempUserForTest,fragmentManager);
            user.show();

            //Util.ChangeFrag(user, fragmentManager);

        }else if(id == R.id.nav_share) {
            myOffers myOffers = new myOffers();
            Util.ChangeFrag(myOffers, fragmentManager);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void hideItem() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();

        if (Util.isLogged()) {//user is logged in
            nav_Menu.findItem(R.id.nav_login).setVisible(false);
            //nav_Menu.findItem(R.id.nav_register).setVisible(false);
            nav_Menu.findItem(R.id.nav_my_account).setVisible(true);
        } else {
            nav_Menu.findItem(R.id.nav_login).setVisible(true);
            //nav_Menu.findItem(R.id.nav_register).setVisible(true);
            //Temp comment :nav_Menu.findItem(R.id.nav_my_account).setVisible(false);

        }
    }



}
