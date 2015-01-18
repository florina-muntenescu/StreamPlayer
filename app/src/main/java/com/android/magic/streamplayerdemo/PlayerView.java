package com.android.magic.streamplayerdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;

/**
 * View that contains the player
 */
public class PlayerView extends LinearLayout implements PlayerController.PlayerListener {

    private PlayerController mPlayerController;
    private Context mContext;

    private TextView mRadioUrl;
    private TextView mTrack;
    private ImageView mPlayPauseButton;

    private String mRadio;

    public PlayerView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }


    public PlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(final Context context) {
        mContext = context;

        mPlayerController = PlayerController.getInstance(context);
        mPlayerController.registerListener(this);

        View rootView = LayoutInflater.from(context).inflate(R.layout.player_view, this, true);

        mRadioUrl = ButterKnife.findById(rootView, R.id.radio_title);
        mPlayPauseButton = ButterKnife.findById(rootView, R.id.play_pause_button);
        mTrack = ButterKnife.findById(rootView, R.id.radio_track);

        mPlayPauseButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRadio != null) {
                            if (mPlayerController.getPlayingUrl() != null) {
                                mPlayerController.stop();
                            } else {
                                mPlayerController.play(mRadio);
                            }
                        }
                    }
                });
        if(mPlayerController.getPlayingUrl() != null){
            mRadio = mPlayerController.getPlayingUrl();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerController.unregisterListener(this);
    }

    public void setPlayingURL(String radioURL){
        mRadioUrl.setText(radioURL);
        mRadio = radioURL;
        mPlayPauseButton.setImageResource(R.drawable.pause_circle_fill);
    }

    @Override
    public void onPlay(String radioURL) {
        setPlayingURL(radioURL);
        mTrack.setText("");
    }

    @Override
    public void onPlayerStop() {
        mPlayPauseButton.setImageResource(R.drawable.play_circle);
    }

    @Override
    public void onTrackChanged(final String track) {
        mTrack.setText(track);
    }
}
