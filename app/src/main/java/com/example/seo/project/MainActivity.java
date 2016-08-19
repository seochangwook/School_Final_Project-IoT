package com.example.seo.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private SectionsPagerAdapter mSectionsPagerAdapter; //탭뷰를 위한 어댑터.//
    private ViewPager mViewPager; //하나의 액티비티에서 가상을 띄우는 것이기에 ViewPager를 정의.//

    String server_ip;
    String server_port_number;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        //기본적으로 ip, 포트번호, 이메일 주소를 가져온다. 이메일 비밀번호는 보안 상 필요시에 물러온다.//
        server_ip = intent.getStringExtra("KEY_IP_ADDRESS");
        server_port_number = intent.getStringExtra("KEY_PORT_NUMBER");
        user_id = intent.getStringExtra("KEY_ID");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setBackgroundColor(Color.DKGRAY);
        tabLayout.setAlpha(0.8f);
        tabLayout.setTabTextColors(Color.WHITE, Color.YELLOW);

        tabLayout.setupWithViewPager(mViewPager); //탭을 장착.//

       /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        //네비게이션 모드에서 애니메이션 적으로 나오게 하기 위해서 설정.//
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); //네비게이션의 이벤트 리스너 장착.//
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId(); //액션바에 선택된 아이템의 id를 가져온다.//

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        else if(id == R.id.gps_bar)
        {
            Intent intent = new Intent(MainActivity.this, GoogleMap_Activity.class);

            //유저 id를 넘겨서 위치를 찾을 때 가입한 지역으로 검색되게 한다.//
            intent.putExtra("KEY_USER_ID", user_id);

            startActivity(intent);

            return true;
        }

        else if(id == R.id.voice_bar)
        {
            Intent intent = new Intent(MainActivity.this, Voice_Activity.class);

            //유저 id, server ip, server port is transfer Voice_Activity.//
            intent.putExtra("KEY_USER_ID", user_id);
            intent.putExtra("KEY_SERVER_IP", server_ip);
            intent.putExtra("KEY_PORT_NUMBER", server_port_number);

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment = null;

            switch(position)
            {
                case 0:
                    service_tab_activity fragment_1 =  new service_tab_activity();

                    Bundle bundle = new Bundle(); //Fragment에게 값을 전달하기 위해서 Bundle사용.//

                    //Intent일 때는 액티비티간에 데이터 전달이였다. 마찬가지로 프래그먼트도 (key,value)로 구성 후 bundle을 이용한다.//
                    bundle.putString("SERVER_IP_ADDRESS_KEY", server_ip);
                    bundle.putInt("PORT_NUMBER_KEY", Integer.parseInt(server_port_number));

                    fragment_1.setArguments(bundle); //프래그먼트에게 인자들(아규먼트)을 전송할 준비를 한다.//

                    return fragment_1; //호출.(뷰 반환 -> 사용자에게 보여짐)//

                case 1:
                    Motor_tab_activity fragment_2 =  new Motor_tab_activity();

                    Bundle bundle_2 = new Bundle(); //Fragment에게 값을 전달하기 위해서 Bundle사용.//

                    //Intent일 때는 액티비티간에 데이터 전달이였다. 마찬가지로 프래그먼트도 (key,value)로 구성 후 bundle을 이용한다.//
                    bundle_2.putString("SERVER_IP_ADDRESS_KEY", server_ip);
                    bundle_2.putInt("PORT_NUMBER_KEY", Integer.parseInt(server_port_number));
                    bundle_2.putString("USER_ID", user_id);

                    fragment_2.setArguments(bundle_2); //프래그먼트에게 인자들(아규먼트)을 전송할 준비를 한다.//

                    return fragment_2; //호출.(뷰 반환 -> 사용자에게 보여짐)//

                case 2:
                    Life_index_tab_activity fragment_3 =  new Life_index_tab_activity();

                    Bundle bundle_3 = new Bundle(); //Fragment에게 값을 전달하기 위해서 Bundle사용.//

                    //Intent일 때는 액티비티간에 데이터 전달이였다. 마찬가지로 프래그먼트도 (key,value)로 구성 후 bundle을 이용한다.//
                    bundle_3.putString("SERVER_IP_ADDRESS_KEY", server_ip);
                    bundle_3.putInt("PORT_NUMBER_KEY", Integer.parseInt(server_port_number));

                    fragment_3.setArguments(bundle_3); //프래그먼트에게 인자들(아규먼트)을 전송할 준비를 한다.//

                    return fragment_3; //호출.(뷰 반환 -> 사용자에게 보여짐)//

                case 3:
                    return new statistics_tab_activity();
            }

            return null;
        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 4; //나타낼 페이지의 수. 탭뷰의 개수만큼 반환.//
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)  //각 페이지의 정보에 따라 설정.//
            {
                case 0:
                    return "Service";
                case 1:
                    return "Door";
                case 2:
                    return "Life index";
                case 3:
                    return "statistics";
            }

            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        public PlaceholderFragment()
        {

        }
    }

    //네비게이션 모드.//
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId(); //네비게이션에서 선택된 아이텀의 id값을 가져온다.//'

        if (id == R.id.nav_light) //조도센서.//
        {
            // Handle the light action
            Intent intent = new Intent(MainActivity.this, Light_Info_Activity.class);

            intent.putExtra("KEY_SERVER_IP",server_ip);
            intent.putExtra("KEY_PORT_NUMBER",server_port_number);
            intent.putExtra("KEY_USER_ID", user_id);

            startActivity(intent);
        }

        else if (id == R.id.nav_temperature) //온도센서.//
        {
            Intent intent = new Intent(MainActivity.this, Temperature_Info_Activity.class);

            intent.putExtra("KEY_SERVER_IP",server_ip);
            intent.putExtra("KEY_PORT_NUMBER",server_port_number);

            startActivity(intent);
        }

        else if (id == R.id.nav_gas) //가스센서.//
        {
            Intent intent = new Intent(MainActivity.this, Gas_Info_Activity.class);

            intent.putExtra("KEY_SERVER_IP",server_ip);
            intent.putExtra("KEY_PORT_NUMBER",server_port_number);

            startActivity(intent);
        }

        else if (id == R.id.nav_humidity) //습도센서.//
        {
            Intent intent = new Intent(MainActivity.this, Humidity_Info_Activity.class);

            intent.putExtra("KEY_SERVER_IP",server_ip);
            intent.putExtra("KEY_PORT_NUMBER",server_port_number);

            startActivity(intent);
        }

        else if (id == R.id.nav_dust) //먼지센서.//
        {
            Intent intent = new Intent(MainActivity.this, Dust_Info_Activity.class);

            intent.putExtra("KEY_SERVER_IP",server_ip);
            intent.putExtra("KEY_PORT_NUMBER",server_port_number);

            startActivity(intent);
        }

        else if (id == R.id.nav_water) //물 센서.//
        {
            Intent intent = new Intent(MainActivity.this, Water_Info_Activity.class);

            intent.putExtra("KEY_SERVER_IP",server_ip);
            intent.putExtra("KEY_PORT_NUMBER",server_port_number);

            startActivity(intent);
        }

        else if (id == R.id.nav_share) //공유하기.(메시지말고 다른 기타 메신저 앱 이용.)//
        {
            Intent send_intent = new Intent(Intent.ACTION_SEND);

            send_intent.putExtra(Intent.EXTRA_TEXT, "" );

            send_intent.setType("text/plain");

            startActivity(send_intent);
        }

        else if (id == R.id.nav_send) //메일 보내기.//
        {
            Intent intent = new Intent(MainActivity.this, Email_send_Activity.class);

            intent.putExtra("KEY_ID", user_id);

            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        
        return true;
    }
}
