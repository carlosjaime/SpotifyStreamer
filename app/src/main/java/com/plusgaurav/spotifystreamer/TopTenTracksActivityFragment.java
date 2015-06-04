package com.plusgaurav.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTenTracksActivityFragment extends Fragment {

    private static myTopTenTrackAdapter topTenTrackAdapter;
    private static List<HashMap<String, String>> topTenTrackList;

    public TopTenTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        // top 10 track listview part
        topTenTrackList = new ArrayList<>();

        // Keys used in Hashmap
        // TODO change keys
        String[] from = {"trackName", "trackAlbum", "trackImage"};

        // Ids of views in listview_layout
        int[] to = {R.id.trackName, R.id.trackAlbum, R.id.trackImage};

        // initialize adapter
        topTenTrackAdapter = new myTopTenTrackAdapter(getActivity(), topTenTrackList, R.layout.toptentracklistview_layout, from, to);

        // bind listview
        ListView artistView = (ListView) rootView.findViewById(R.id.artistListView);
        artistView.setAdapter(topTenTrackAdapter);

        // get top ten tracks of the artist (async task)
        FetchTopTenTrack task = new FetchTopTenTrack();
        String[] artistInfo = getActivity().getIntent().getExtras().getStringArray(Intent.EXTRA_TEXT);
        // pass artistId
        task.execute(artistInfo[0]);


        return rootView;
    }

    public class FetchTopTenTrack extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... artistId) {

            // do spotify transaction
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(SearchArtistActivity.getAccessToken());
            SpotifyService spotify = api.getService();

            // set options
            Map<String, Object> options = new HashMap<>();
            options.put("country", "US");

            // search artist
            Tracks topTracks = spotify.getArtistTopTrack(artistId[0], options);

            // update data source
            topTenTrackList.clear();
            for (Track track : topTracks.tracks) {
                HashMap<String, String> trackMap = new HashMap<>();
                trackMap.put("trackName", track.name);
                trackMap.put("trackAlbum", track.album.name);
                // TODO put logic to decide image size
                for (Image image : track.album.images) {
                    //if((image.height)*(image.width)==200)
                    trackMap.put("trackImage", image.url);
                }
                topTenTrackList.add(trackMap);
            }

            // return true if data source refreshed
            return !topTenTrackList.isEmpty();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean isDataSourceRefereshed) {
            if (isDataSourceRefereshed) {
                topTenTrackAdapter.notifyDataSetChanged();
            } else {
                // TODO handle if no tracks returned
            }
        }
    }


    // create custom adapter
    // got help from "http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view"
    public class myTopTenTrackAdapter extends SimpleAdapter {

        public myTopTenTrackAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View view = super.getView(position, convertView, parent);

            // get reference to imageview
            ImageView artistImageView = (ImageView) view.findViewById(R.id.trackImage);

            // get the url from the data source
            String url = (String) ((Map) getItem(position)).get("trackImage");

            // load it to the imageview
            if (url != null) {
                Picasso.with(view.getContext()).load(url).placeholder(R.drawable.ic_play_circle_filled_black_36dp).error(R.drawable.ic_play_circle_filled_black_36dp).into(artistImageView);
            }

            return view;
        }
    }
}