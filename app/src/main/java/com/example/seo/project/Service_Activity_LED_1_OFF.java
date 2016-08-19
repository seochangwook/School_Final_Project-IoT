package com.example.seo.project;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by apple on 2016. 4. 26..
 */
public class Service_Activity_LED_1_OFF extends Service
{
    int service_number;
    String service_number_str;

    public Service_NetworkTask_LED_1 sensor_task_1;

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

        Log.i("Service Log", "service 1 onCreate()");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i("Service Log", "LED service LED 1 off onDestroy()");

        //sensor_task_1.cancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStart(intent, startId);

        Log.i("Service Log", "service 1 LED off onStartCommand()");

        ip_address=(String) intent.getExtras().get("KEY_IP_ADDRESS");
        port_number_str = (String)intent.getExtras().get("KEY_PORT_NUMBER");
        service_number_str = (String)intent.getExtras().get("KEY_SERVICE_NUMBER");

        port_number = Integer.parseInt(port_number_str.toString());
        service_number = Integer.parseInt(service_number_str.toString());

        if(service_number == 1)
        {
            //Toast.makeText(getApplicationContext(), "" + service_number, Toast.LENGTH_SHORT).show();

            Log.i("Service Log", "service LED 1 OFF");

            sensor_task_1 = new Service_NetworkTask_LED_1(ip_address, port_number);

            sensor_task_1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public class Service_NetworkTask_LED_1 extends AsyncTask<Void, String, Void>
    {
        String dstAddress;
        int dstPort;
        String response;

        Service_NetworkTask_LED_1(String addr, int port)
        {
            dstAddress = addr;
            dstPort = port;

            Log.i("IP / PORT :", ""+dstAddress+"/"+dstPort);
        }

        /*protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "Start motor 1 information", Toast.LENGTH_SHORT).show();
        }*/

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                Socket socket = new Socket(dstAddress, dstPort);

                InputStream inputStream = socket.getInputStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                //5번 서비스 이용, 1번 아두이노의 6번 센서(LED 1)를 OFF(1)한다.//
                trans_data_format += "5" +"/" + "1" + "/" + "" + "9" + "/" +"0" +".";

                out.println(trans_data_format);

                trans_data_format = "";


                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1)
                {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                socket.close();

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

        protected void onProgressUpdate(String... values)
        {
            //LED는 따로 데이터를 받을 필요가 없다.//
        }

        @Override
        protected void onPostExecute(Void result)
        {
            //Toast.makeText(getApplicationContext(), response.trim(), Toast.LENGTH_SHORT).show();
        }

        /*protected void onCancelled(Void result)
        {
            Toast.makeText(getApplicationContext(), "motor 1 task shutdown...", Toast.LENGTH_SHORT).show();
        }*/
    }
}

