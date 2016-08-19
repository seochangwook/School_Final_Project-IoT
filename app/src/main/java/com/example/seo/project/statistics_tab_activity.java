package com.example.seo.project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Seo on 2016-03-29.
 */
public class statistics_tab_activity  extends Fragment
{
    WebView webview;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.statistics_tab_activity_layout, container, false);
        webview = (WebView)rootView.findViewById(R.id.webView);

        webview.setWebViewClient(new WebViewClient());

        WebSettings webSettings = webview.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webview.loadUrl("http://54.64.44.201/src/graphWeb.php");

        return rootView;
    }
}
