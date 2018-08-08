package com.example.scott.newapp1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.widget.TextView;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<NewsItem>> {

    /**
     * URL for News information from the from The Guardian API
     */
    private static final String GUARDIAN_URL = "https://content.guardianapis.com/search?q=ufc%20AND%20fashion&api-key=fa889bfa-3afd-443b-bf4a-46e363835da5";

    //Only really needed if using more than one loader
    private static final int NEWSITEM_LOADER_ID = 1;

    @BindView(R.id.list)
    ListView newsListView;

    @BindView(R.id.emptyView)
    TextView emptyTextView;

    @BindView(R.id.progress_bar)
    View progressBar;

    private static final String LOG_TAG = NewsActivity.class.getName();

    //Adapter for the list of articles
    private NewsItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        // bind the view using butterknife
        ButterKnife.bind(this);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWSITEM_LOADER_ID, null, this);
        } else{
            //If there is no connectivity set the empty view to the correct text
            progressBar.setVisibility(View.GONE);
            emptyTextView.setText(R.string.no_connection);
        }

        //Create a new adapter that takes the list of news stories as input
        mAdapter = new NewsItemAdapter(this, new ArrayList<NewsItem>());

        //If the there is no data to pull an empty view is displayed
        newsListView.setEmptyView(emptyTextView);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Find the current news item that was clicked
                NewsItem currentNewsItem = mAdapter.getItem(position);

                //Take the Url String and turn it into a Uri Object
                Uri newsItemUri = Uri.parse(currentNewsItem.getUrl());

                //Create a new Intent to view the web page
                Intent openWebPage = new Intent(Intent.ACTION_VIEW, newsItemUri);

                //launch the intent to open the web page
                startActivity(openWebPage);
            }
        });

    }

    @Override
    public Loader<List<NewsItem>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new NewsItemLoader(this, GUARDIAN_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> newstories) {
        //Hide the progress bar after the data load is complete
        progressBar.setVisibility(View.GONE);

        //Display text if no data is pulled from the Guardian API
        emptyTextView.setText(R.string.whoops);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of articles, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (newstories != null && !newstories.isEmpty()) {
           mAdapter.addAll(newstories);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();

    }
}

