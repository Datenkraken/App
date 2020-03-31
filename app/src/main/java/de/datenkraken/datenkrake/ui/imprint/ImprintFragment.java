package de.datenkraken.datenkrake.ui.imprint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;

/**
 * Fragment to Load the Imprint and display it as a WebView in the App.
 */
public class ImprintFragment extends Fragment {

    @BindView(R.id.imprint_webview)
    WebView webView;

    /**
     * Loads the imprint into a WebView and displays Fragment.
     *
     * @param inflater to inflate the layout.
     * @param container of the view.
     * @param savedInstanceState a bundle of a saved instance sent to the function.
     * @return View of Fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imprint, container, false);
        ButterKnife.bind(this, view);
        // Load the imprint into a WebView.
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(getString(R.string.imprint_webview_url));
        return view;
    }
}
