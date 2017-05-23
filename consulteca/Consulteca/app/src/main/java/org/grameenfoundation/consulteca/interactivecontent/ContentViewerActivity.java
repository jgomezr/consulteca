package org.grameenfoundation.consulteca.interactivecontent;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.grameenfoundation.consulteca.R;

/**
 * Activity for viewing the interactive content
 */
public class ContentViewerActivity extends Activity {
    public static final String EXTRA_CONTENT_IDENTIFIER = "content_item";
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            LayoutInflater inflator = getLayoutInflater();
            View mainView = inflator.inflate(R.layout.interactive_content_viewer, null);

            String contentItem = (String) getIntent().getStringExtra(EXTRA_CONTENT_IDENTIFIER);

            String contentFolder = ContentUtils.getContentFolder(contentItem);
            String url = "file://" + contentFolder + "/story.html";

            webView = (WebView) mainView.findViewById(R.id.webview);
            setContentView(mainView);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            }
            webView.getSettings().setAllowContentAccess(true);
            webView.getSettings().setBlockNetworkImage(false);
            webView.getSettings().setDomStorageEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    view.clearCache(true);
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    //super.onLoadResource(view, url);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }
            });

            webView.loadUrl(url);

            webView.setWebChromeClient(new WebChromeClient(){

            });
        } catch (Exception ex) {
            Log.e(ContentViewerActivity.class.getName(), "Application Error", ex);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            /*this.getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);*/

            webView.stopLoading();
            webView.loadUrl("about:blank");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
