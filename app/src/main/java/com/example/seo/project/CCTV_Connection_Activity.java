package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Seo on 2016-04-11.
 */
public class CCTV_Connection_Activity extends Activity
{
    TextView port_number_text;
    Button ok_button;

    //포트번호를 난수로 제공.//

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cctv_conection_activity_layout);

        port_number_text = (TextView)findViewById(R.id.port_number_text);
        ok_button = (Button)findViewById(R.id.ok_button);

        port_number_text.setText("8090");

        ok_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = getPackageManager().getLaunchIntentForPackage("org.videolan.vlc");

                startActivity(intent);

                finish();
            }
        });
    }
}
