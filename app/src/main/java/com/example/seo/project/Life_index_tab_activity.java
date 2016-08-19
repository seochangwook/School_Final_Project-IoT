package com.example.seo.project;

import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;

/**
 * Created by Seo on 2016-03-29.
 */
public class Life_index_tab_activity  extends Fragment
{
    private String server_ip_address;
    private int port_number;

    public String area_code;
    String do_value;
    String Gu_si_value;

    TextView server_info_text;

    EditText area_name_do_text;
    EditText area_name_si_gu_text;
    Button search_button;

    ImageButton cold_index_button;
    ImageButton sun_index_button;
    ImageButton feel_temp_index_button;
    ImageButton flower_index_button;
    ImageButton cold_effect_index_button;
    ImageButton food_poisoning_index_button;
    ImageButton discomfort_index_button;

    FloatingActionButton info_1_button;
    FloatingActionButton info_2_button;

    //지역코드를 가져오는 서비스.//
    Service_NetworkTask service_1_task; //서비스 작업 1(백그라운드로 수행)//

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.life_index_tab_activity_layout, container, false);

        server_info_text = (TextView) rootView.findViewById(R.id.server_address_info_text);
        info_1_button = (FloatingActionButton)rootView.findViewById(R.id.info_button_1);
        info_2_button = (FloatingActionButton)rootView.findViewById(R.id.info_button_2);
        area_name_do_text = (EditText)rootView.findViewById(R.id.area_name_do);
        area_name_si_gu_text = (EditText)rootView.findViewById(R.id.area_name_si_gu);
        search_button = (Button)rootView.findViewById(R.id.search_button);
        cold_index_button = (ImageButton)rootView.findViewById(R.id.cold_index);
        sun_index_button = (ImageButton)rootView.findViewById(R.id.sun_index);
        feel_temp_index_button = (ImageButton)rootView.findViewById(R.id.feel_temp_index);
        flower_index_button = (ImageButton)rootView.findViewById(R.id.flower_index);
        cold_effect_index_button = (ImageButton)rootView.findViewById(R.id.cold_effect_index);
        food_poisoning_index_button = (ImageButton)rootView.findViewById(R.id.food_poisoning_index);
        discomfort_index_button = (ImageButton)rootView.findViewById(R.id.discomfort_index);

        Bundle receive_data = getArguments(); //액티비티에서 넘어온 값을 받기 위해서 Bundle설정.//

        server_ip_address = receive_data.getString("SERVER_IP_ADDRESS_KEY");
        port_number = receive_data.getInt("PORT_NUMBER_KEY");

        server_info_text.setText("Server IP : " + server_ip_address + " / Server PORT : " + port_number);

        search_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    do_value = area_name_do_text.getText().toString();
                    Gu_si_value = area_name_si_gu_text.getText().toString();

                    String sql = "";

                    //서버에 존재하는 MYSQL을 접근하기 위해서 질의문을 작성. 외부DB를 사용.//
                    //서버에서 while()문으로 하나씩 검사해가면서 찾는거보다 sql문에서 where절로 해주는 것이 쿼리에 효율적이다.//
                    sql = "SELECT area_code FROM haengjeongdong WHERE " +
                            "area_name_1 LIKE '%" + do_value + "%' AND " +
                            "area_name_2 LIKE '%" + Gu_si_value + "%';";

                    //서비스 작업 실행.//
                    service_1_task = new Service_NetworkTask(server_ip_address, port_number, sql);

                    service_1_task.execute(); //네트워크 작업을 수행.//
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } //UTF-8로 변환.//
            }
        });

        food_poisoning_index_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), Food_Poisoning_Info.class);

                intent.putExtra("KEY_IP_ADDRESS", server_ip_address);
                intent.putExtra("KEY_PORT_NUMBER", ""+port_number);
                intent.putExtra("KEY_AREA_CODE", area_code);

                startActivity(intent);

                area_code = "";
            }
        });

        info_1_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        "1. 센서의 특징설명 입니다." + "\n",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        info_2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        "1. 센서의 주의사항입니다." + "\n",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        return rootView;
    }

    public class Service_NetworkTask extends AsyncTask<Void, Void, Void>
    {

        String dstAddress;
        int dstPort;
        String response;
        String sql;

        //IP와 PORT번호를 설정.//
        Service_NetworkTask(String addr, int port, String sql)
        {
            dstAddress = addr;
            dstPort = port;
            this.sql=sql;

            Log.i("IP / PORT :", "" + dstAddress + "/" + dstPort);
        }

        @Override
        protected Void doInBackground(Void... arg0) //백그라운드로 데이터 송수신 작업 진행.//
        {
            try
            {
                Socket socket = new Socket(dstAddress, dstPort); //서버소켓에 연결.//

                //데이터를 송수신할 스트림들을 정의.//
                InputStream inputStream = socket.getInputStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                //데이터를 전송.서버로 부터 에코기능을 수행. SQL서비스 이므로 서비스 분기는 SQL로 한다//
                out.println("SQL_S/"+sql);

                //데이터를 받을때까지 기다림.//
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1)
                {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                socket.close();

                //받아온 데이터 타입의 인코딩 방식을 결정.//
                response = byteArrayOutputStream.toString("UTF-8");
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            int str_size = response.length();

            area_code = response.trim();

            Log.i("Code : ", area_code);

            Toast.makeText(getActivity(), "Search Finish", Toast.LENGTH_SHORT).show();

            super.onPostExecute(result);
        }
    }
}
