package com.companyname.chatapp.chatapp.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;;

import com.companyname.chatapp.chatapp.Database.UserProvider;
import com.companyname.chatapp.chatapp.R;
import com.companyname.chatapp.chatapp.Adapters.SectionsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.main_tab_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        } else {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue((long) 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_item) {
            FirebaseAuth.getInstance().signOut();
            //TODO delete the current user from db
            getContentResolver().delete(
                    UserProvider.CONTENT_URI, null, null);

            sendToStart();
        }
        if (item.getItemId() == R.id.account_settings_item) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        if (item.getItemId() == R.id.all_users_item) {
            startActivity(new Intent(MainActivity.this, UsersActivity.class));
        }
        return true;
    }
}
