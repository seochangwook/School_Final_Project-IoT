package com.example.seo.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Seo on 2016-04-19.
 */
public class CustomUIActivity extends Activity implements TextToSpeech.OnInitListener
{
    private ProgressBar mProgress;	//프로그레스바
    private ImageView mLeftVolume[], mRightVolume[];//볼륨 이미지
    private SpeechRecognizer mRecognizer;	//음성인식 객체
    private TextToSpeech mTTS;	//TextToSpeech 객체
    private ArrayList<Locale> mLanguages;	//사용할 언어(국가)
    private boolean isInit, isSupport;	//TextToSpeech가 초기화 되었는지, 지원하는 언어인지 판단하는 플래그

    private final int READY = 0, END=1, FINISH=2; //핸들러 메시지. 음성인식 준비, 끝, 앱 종료
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case READY:
                    mProgress.setVisibility(View.INVISIBLE);	//준비되었으면 프로그레스바 감춤
                    findViewById(R.id.stt_ui).setVisibility(View.VISIBLE);//마이크 이미지 보임.

                    break;

                case END:
                    mProgress.setVisibility(View.VISIBLE);	//말이 끝났으면 프로그레스바 출력(음성인식 중)
                    findViewById(R.id.stt_ui).setVisibility(View.INVISIBLE);//마이크 이미지 감춤
                    sendEmptyMessageDelayed(FINISH, 5000);
                    //인식 시간 5초로 설정. 8초 지나면 신경안씀.8000
                    break;

                case FINISH:
                    finish();
                    //앱 종료
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_ui);

        mProgress = (ProgressBar)findViewById(R.id.progress);	//프로그레스바

        mLeftVolume = new ImageView[3];	//왼쪽 볼륨
        mRightVolume = new ImageView[3]; //오른쪽 볼륨

        for(int i=0; i<3; i++)
        {
            mLeftVolume[i] = (ImageView)findViewById(R.id.volume_left_1+i);
            mRightVolume[i] = (ImageView)findViewById(R.id.volume_right_1+i);
        }

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);//음성인식 intent생성

        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());//데이터 설정
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");	//음성인식 언어 설정

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);	//음성인식 객체
        mRecognizer.setRecognitionListener(listener);										//음성인식 리스너 등록
        mRecognizer.startListening(i);

        mTTS = new TextToSpeech(this, this);		//tts 객체 생성
        mLanguages = new ArrayList<Locale>();	//사용할 언어 리스트 생성
        mLanguages.add(Locale.KOREA);				//한국어//음성인식 시작
    }

    //사용자의 입력 소리 크기에 따라 볼륨 이미지 설정. 3단계
    private void setVolumeImg(int step)
    {
        for(int i=0; i<3; i++)
        {
            if(i<step)
            {
                mLeftVolume[0].setVisibility(View.VISIBLE);
                mRightVolume[0].setVisibility(View.VISIBLE);
            }

            else
            {
                mLeftVolume[0].setVisibility(View.INVISIBLE);
                mRightVolume[0].setVisibility(View.INVISIBLE);
            }
        }
    }

    //음성인식 리스너
    private RecognitionListener listener = new RecognitionListener()
    {
        //입력 소리 변경 시
        @Override public void onRmsChanged(float rmsdB)
        {
            int step = (int)(rmsdB/7);		//소리 크기에 따라 step을 구함.

            setVolumeImg(step);			//총 4단계 이미지 설정. 없음. 1단계, 2단계, 3단계
        }

        //음성 인식 결과 받음
        @Override public void onResults(Bundle results)
        {
            mHandler.removeMessages(END);	//핸들러에 종료 메시지 삭제

            Intent i = new Intent();//결과 반환할 intent

            i.putExtras(results);	//결과 등록
            setResult(RESULT_OK, i);//결과 설정

            finish();	//앱 종료
        }

        //음성 인식 준비가 되었으면
        @Override public void onReadyForSpeech(Bundle params)
        {
            mHandler.sendEmptyMessage(READY);		//핸들러에 메시지 보냄
        }

        //음성 입력이 끝났으면
        @Override public void onEndOfSpeech()
        {
            mHandler.sendEmptyMessage(END);		//핸들러에 메시지 보냄
        }

        //에러가 발생하면
        @Override public void onError(int error)
        {
            setResult(error);		//전 activity로 에러코드 전송
        }

        @Override public void onBeginningOfSpeech()
        {

        }	//입력이 시작되면

        @Override public void onPartialResults(Bundle partialResults) {}		//인식 결과의 일부가 유효할 때
        @Override public void onEvent(int eventType, Bundle params) {}		//미래의 이벤트를 추가하기 위해 미리 예약되어진 함수
        @Override public void onBufferReceived(byte[] buffer) {}				//더 많은 소리를 받을 때
    };

    @Override
    public void finish()
    {
        if(mRecognizer!= null)
        {
            mRecognizer.stopListening();	//음성인식 중지
        }

        mHandler.removeMessages(READY);			//메시지 삭제
        mHandler.removeMessages(END);			//메시지 삭제
        mHandler.removeMessages(FINISH);			//메시지 삭제

        super.finish();
    }

    @Override
    public void onInit(int status)
    {
        // TODO Auto-generated method stub
        speak();
    }

    public void speak()
    {
        Locale lang = mLanguages.get(0);	//선택한 언어

        mTTS.setLanguage(lang);									//언어 설정.
        mTTS.setPitch(1.0f);				//pitch 설정.
        mTTS.setSpeechRate(1.2f);		//rate 설정.

        String text="서비스를 말하세요";

        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);	//해당 언어로 텍스트 음성 출력

        text = null;
    }
}
