package com.example.seo.project;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Seo on 2016-04-07.
 */
public class Service_Activity_Fire extends Service
{
    int service_number;
    String service_number_str;

    public Service_NetworkTask_1 sensor_task_1;

    public String ip_address;
    public int port_number;
    public String port_number_str;
    public String trans_data_format = "";

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        Log.i("Service Log", "service onBind()");

        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i("Service Log", "service onCreate()");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i("Service Log", "service 2 onDestroy()");

        sensor_task_1.cancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStart(intent, startId);

        Log.i("Service Log", "service 2 onStartCommand()");

        ip_address=(String) intent.getExtras().get("KEY_IP_ADDRESS");
        port_number_str = (String)intent.getExtras().get("KEY_PORT_NUMBER");
        service_number_str = (String)intent.getExtras().get("KEY_SERVICE_NUMBER");

        port_number = Integer.parseInt(port_number_str.toString());
        service_number = Integer.parseInt(service_number_str.toString());

        if(service_number == 1)
        {
            //Toast.makeText(getApplicationContext(), "" + service_number, Toast.LENGTH_SHORT).show();

            Log.i("Service Log", "service 2 start!!");

            sensor_task_1 = new Service_NetworkTask_1(ip_address, port_number);

            sensor_task_1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public class Service_NetworkTask_1 extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String response;

        Service_NetworkTask_1(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "화재감지 센서 작동", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                while(true)
                {
                    if(isCancelled())
                    {
                        break;
                    }

                    Socket socket = new Socket(dstAddress, dstPort);

                    InputStream inputStream = socket.getInputStream();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];

                    //5번으로 서비스 이용, 1번아두이노의 5번센서(불꽃센서)로 데이터를 센싱한다.//
                    trans_data_format += "5" +"/" + "1" + "/" + "" + "5" + "/" +"G" +".";

                    out.println(trans_data_format);

                    trans_data_format = "";

                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    socket.close();

                    response = byteArrayOutputStream.toString("UTF-8");

                    publishProgress(response);

                    Thread.sleep(8000);
                }
            }

            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... values)
        {
            Log.i("service log :", "service 1 value : "+values[0]);

            //불꽃센서로 부터 데이터를 받아온다. 잘못 전송되는 쓰레기값을 고려하여서 try~catch문으로 예외처리 구현//
            try
            {
                int sensor_value = Integer.parseInt(values[0].toString().trim());

                if(sensor_value ==1)
                {
                    SmsManager mSmsManager = SmsManager.getDefault();

                    mSmsManager.sendTextMessage("01042084757", null, "불이 났습니다. 119에 신고하세요!!", null, null);
                }
            }

            catch (NumberFormatException e)
            {
                Log.i("ERROR Message : ", values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        protected void onCancelled(Void result)
        {
            Toast.makeText(getApplicationContext(), "화재감지 센서 해제", Toast.LENGTH_SHORT).show();
        }
    }
}
