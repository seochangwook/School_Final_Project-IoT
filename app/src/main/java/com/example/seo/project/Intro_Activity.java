package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Seo on 2016-03-29.
 */
public class Intro_Activity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity_layout);

        Handler handler=new Handler(); //핸들러 객체 생성.//

        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                //화면이동.//
                Intent activity = new Intent(Intro_Activity.this, Login_Activity.class); //인텐트로 정의.//
                startActivity(activity); //인텐트로 지정한 액티비티 실행.//

                //단말기 back버튼을 누를시 인트로 화면으로 돌아오지 않도록 인트로 화면 종료.//
                finish();
            }
        }, 3000);
    }
}
