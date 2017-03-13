package devgam.vansit;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import devgam.vansit.JSON_Classes.Users;


public class MainController extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{


    FragmentManager fragmentManager;// this is used for the ChangeFrag method
    DrawerLayout drawer;
    FirebaseAuth firebaseAuth;
    DatabaseReference mRef;
    String tempUID;
    TextView name;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        fragmentManager  = getSupportFragmentManager();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        name = (TextView)header.findViewById(R.id.nav_header_main_name_text);
        TextView email = (TextView)header.findViewById(R.id.nav_header_main_email_text);
        img = (ImageView)header.findViewById(R.id.nav_header_main_img);

        if(Util.isLogged()) {
            firebaseAuth = FirebaseAuth.getInstance();
            tempUID = firebaseAuth.getCurrentUser().getUid();
            mRef = FirebaseDatabase.getInstance().
                    getReference(Util.RDB_USERS + "/" +
                            tempUID);
            email.setText(firebaseAuth.getCurrentUser().getEmail());

            setDataToViews();
        } else {
            name.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
            img.setVisibility(View.INVISIBLE);
        }



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

        hideItem();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
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
        if (id == R.id.nav_login) {
            Login loginPage = new Login();
            Util.ChangeFrag(loginPage,fragmentManager);
        } else if (id == R.id.nav_main) {
            Main mainPage = new Main();
            Util.ChangeFrag(mainPage,fragmentManager);
        } else if(id == R.id.nav_my_account){
            myAccount myAccount = new myAccount();
            Util.ChangeFrag(myAccount, fragmentManager);
        }  else if(id == R.id.nav_my_offers) {
            myOffers myOffers = new myOffers();
            Util.ChangeFrag(myOffers, fragmentManager);
            drawer.closeDrawer(GravityCompat.START);
        } else  if(id == R.id.nav_fav) {
            favorite favorite = new favorite();
            Util.ChangeFrag(favorite, fragmentManager);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else  if(id == R.id.nav_rec) {
            addRequest rec = new addRequest();
            Util.ChangeFrag(rec, fragmentManager);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else  if(id == R.id.nav_logout) {
            FloatingActionButton fabMain = (FloatingActionButton) findViewById(R.id.add_fab);
            if(fabMain!=null)
                fabMain.setVisibility(View.GONE);
            FirebaseAuth.getInstance().signOut();
            Main mainPage = new Main();
            Util.ChangeFrag(mainPage,fragmentManager);
            hideItem();
            return true;
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
            nav_Menu.findItem(R.id.nav_rec).setVisible(true);
            nav_Menu.findItem(R.id.nav_fav).setVisible(true);
            nav_Menu.findItem(R.id.nav_my_account).setVisible(true);
            nav_Menu.findItem(R.id.nav_logout).setVisible(true);
            nav_Menu.findItem(R.id.nav_my_offers).setVisible(true);
        } else {
            nav_Menu.findItem(R.id.nav_login).setVisible(true);
            nav_Menu.findItem(R.id.nav_rec).setVisible(false);
            nav_Menu.findItem(R.id.nav_fav).setVisible(false);
            nav_Menu.findItem(R.id.nav_my_account).setVisible(false);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
            nav_Menu.findItem(R.id.nav_my_offers).setVisible(false);

        }
    }

    //to set data to views after data set it one time
    private void setDataToViews() {

        //temp code:
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Users tempUser = dataSnapshot.getValue(Users.class);

                if(! tempUser.getFirstName().isEmpty()) {
                    //For Check method
                    name.setText(tempUser.getFirstName() + " " + tempUser.getLastName());
                    if(tempUser.getGender().equals("male"))
                        img.setImageResource(R.mipmap.ic_action_male);
                    else
                        img.setImageResource(R.mipmap.ic_action_female);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



}
