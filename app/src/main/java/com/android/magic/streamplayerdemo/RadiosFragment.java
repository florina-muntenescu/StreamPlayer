package com.android.magic.streamplayerdemo;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Displays a list of radios and allows playing radios
 */
public class RadiosFragment extends android.support.v4.app.Fragment {

    private ListView mRadiosList;
    private PlayerView mPlayerView;

    private PlayerController mPlayerController;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPlayerController = PlayerController.getInstance(getActivity());

        mRadiosList = (ListView) rootView.findViewById(R.id.radios);
        mPlayerView = (PlayerView) rootView.findViewById(R.id.player);

        final List<String> radios = new ArrayList<String>();
        radios.add("http://streams.greenhost.nl:8080/concertzenderlive");
        radios.add("http://xstream1.somafm.com:8600");
        radios.add("http://tsfjazz.ice.infomaniak.ch:80/tsfjazz-high.mp3");

        ListAdapter listAdapter = new RadiosListAdapter(getActivity(),
                R.layout.radio_list_item_view, radios);

        mRadiosList.setAdapter(listAdapter);

        mRadiosList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        final String url = radios.get(position);
                        mPlayerController.play(url);
                    }
                });

        if(mPlayerController.getPlayingUrl() != null){
            mPlayerView.setPlayingURL(mPlayerController.getPlayingUrl());
        }
        return rootView;
    }

    static class ViewHolder {
        @InjectView(R.id.radio_title)
        TextView title;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    private class RadiosListAdapter extends ArrayAdapter<String> {
        private LayoutInflater mLayoutInflater;

        public RadiosListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            init(context);
        }

        private void init(Context context) {
            mLayoutInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = mLayoutInflater.inflate(R.layout.radio_list_item_view, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            final String radio = getItem(position);
            holder.title.setText(radio);

            return view;
        }


    }
}
