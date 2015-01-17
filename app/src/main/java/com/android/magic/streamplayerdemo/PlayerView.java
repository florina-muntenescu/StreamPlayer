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
    private ImageView mPlayPauseButton;

    private String mRadio;

    public PlayerView(Context context) {
        super(context);
        init(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public PlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        mContext = context;

        mPlayerController = PlayerController.getInstance(context);
        mPlayerController.registerListener(this);

        View rootView = LayoutInflater.from(context).inflate(R.layout.player_view, this, true);

        mRadioUrl = ButterKnife.findById(rootView, R.id.radio_title);
        mPlayPauseButton = ButterKnife.findById(rootView, R.id.play_pause_button);

        mPlayPauseButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRadio != null) {
                            if (mPlayerController.getPlayingUrl() != null) {
                                mPlayerController.pause();
                            } else {
                                mPlayerController.play(mRadio);
                            }
                        }
                    }
                });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerController.unregisterListener(this);
    }


    @Override
    public void onPlay(String radioURL) {
        mRadioUrl.setText(radioURL);
        mPlayPauseButton.setImageResource(R.drawable.pause_circle_fill);
    }

    @Override
    public void onPlayerStop() {

    }

    @Override
    public void onTrackChanged(String track) {
    }
}
