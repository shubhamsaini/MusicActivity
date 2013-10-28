/*
 * The input strings to be sent from the ring are as follows:
 * 
 * profile - toggles between silent,vibrate and loud modes
 * left - plays previous song
 * right - plays next song
 * upup - increase volume
 * down - decrease volume
 * 
 * The follows strings can be implemented in future enhancements
 * start - play/pause current song
 * shuffle - turn shuffle on/off
 * 
 */

package com.example.musicactivity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicActivity extends Activity implements OnCompletionListener {

	/* Declaring variables for the activity */
	
	TextView text;

	private ImageButton btnPlay;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnGal;
	private ImageView btnMute;

	// private ImageView btnLevel;
	private TextView songTitleLabel;
	ListView mListView;
	ImageView imgAlbum;

	/* Declaring media player variables */

	private MediaPlayer mp;
	private SongsManager songManager;
	private int currentSongIndex = 0;
	private boolean isShuffle = false;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	private int mute = 0;
	private SeekBar volumeSeekbar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_activity);

		

		/* Initialising variables to the activity views */

		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnNext = (ImageButton) findViewById(R.id.btnNextSong);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevSong);
		btnGal = (ImageButton) findViewById(R.id.btnGal);
		btnMute = (ImageView) findViewById(R.id.btnMute);
		// btnLevel = (ImageView) findViewById(R.id.btnLevel);
		songTitleLabel = (TextView) findViewById(R.id.songTitle2);
		imgAlbum = (ImageView) findViewById(R.id.albumCover);
		volumeSeekbar = (SeekBar) findViewById(R.id.songProgressBar);

		/*
		 * Initialising new media player and creating a new songs list
		 */

		mp = new MediaPlayer();
		songManager = new SongsManager();
		mp.setOnCompletionListener(this);
		songsList = songManager.getPlayList();

		// Populating the playlist ListView
		ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < songsList.size(); i++) {
			HashMap<String, String> song = songsList.get(i);
			songsListData.add(song);
		}
		ListAdapter adapter = new SimpleAdapter(this, songsListData,
				R.layout.playlist_item, new String[] { "songTitle" },
				new int[] { R.id.songTitle });
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			/* Function called when a playlist item is clicked */
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting listitem index
				int songIndex = position;
				currentSongIndex = songIndex;
				playSong(songIndex);
			}
		});

		//
		

		// set the progress of volume seekbar as per the current volume level
		volumeSeekbar.setMax(100);
		float vol2 = (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
				.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
				* (float) 100;
		volumeSeekbar.setProgress((int) vol2);
		if (vol2 > 0)
			btnMute.setImageResource(R.drawable.n_unmute);
		else {
			btnMute.setImageResource(R.drawable.n_mute);
			mute = 1;
		}

		if (songsList.size() > 0) {
			playSong(0);

			// Play button action
			btnPlay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {

					if (mp.isPlaying()) {
						if (mp != null) {
							mp.pause();
							btnPlay.setImageResource(R.drawable.n_play);
						}
					} else {

						if (mp != null) {
							mp.start();
							btnPlay.setImageResource(R.drawable.n_pause);
						}
					}
				}
			});

			// Next button action
			btnNext.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// if shuffle is on, play any random song
					if (isShuffle) {
						Random rand = new Random();
						currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
						playSong(currentSongIndex);
					} else {
						// check if next song is there or not
						if (currentSongIndex < (songsList.size() - 1)) {
							playSong(currentSongIndex + 1);
							currentSongIndex = currentSongIndex + 1;
						} else {
							// play first song
							playSong(0);
							currentSongIndex = 0;
						}
					}
				}
			});

			// Previous button action
			btnPrevious.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// if shuffle is on, play any random song
					if (isShuffle) {
						Random rand = new Random();
						currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
						playSong(currentSongIndex);
					} else {
						if (currentSongIndex > 0) {
							playSong(currentSongIndex - 1);
							currentSongIndex = currentSongIndex - 1;
						} else {
							// play last song
							playSong(songsList.size() - 1);
							currentSongIndex = songsList.size() - 1;
						}
					}
				}
			});

		}

		// invoked when volume seekbar is changed manually
		volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress,
					boolean arg2) {
				int volIndex = progress
						* ((AudioManager) getSystemService(AUDIO_SERVICE))
								.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
						/ 100;
				((AudioManager) getSystemService(AUDIO_SERVICE))
						.setStreamVolume(AudioManager.STREAM_MUSIC, volIndex,
								AudioManager.FLAG_SHOW_UI);
				// setVolIcon();
				if (progress > 0)
					btnMute.setImageResource(R.drawable.n_unmute);
				else
					btnMute.setImageResource(R.drawable.n_mute);

			}
		});

	

		/* Mute button action */
		btnMute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (mute == 0) {
					btnMute.setImageResource(R.drawable.n_mute);
					mute = 1;
					((AudioManager) getSystemService(AUDIO_SERVICE))
							.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
									AudioManager.FLAG_SHOW_UI);
					volumeSeekbar.setProgress(0);
					// setVolIcon();

				} else {
					mute = 0;
					((AudioManager) getSystemService(AUDIO_SERVICE))
							.setStreamVolume(AudioManager.STREAM_MUSIC, 5,
									AudioManager.FLAG_SHOW_UI);
					float vol2 = (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
							.getStreamVolume(AudioManager.STREAM_MUSIC)
							/ (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
									.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
							* (float) 100;
					volumeSeekbar.setProgress((int) vol2);
					btnMute.setImageResource(R.drawable.n_unmute);
					// setVolIcon();
				}
			}
		});

	}

	// when silent-vibrate toggle button is clicked

	

	// Playing a song at index songIndex
	public void playSong(int songIndex) {
		// Play song
		try {
			mp.reset();
			mp.setDataSource(songsList.get(songIndex).get("songPath"));
			mp.prepare();
			mp.start();
			// Displaying Song title
			String songTitle = songsList.get(songIndex).get("songTitle");
			songTitleLabel.setText("Now Playing: " + songTitle);
			btnPlay.setImageResource(R.drawable.n_pause);
			MediaMetadataRetriever mediaInfo = new MediaMetadataRetriever();
			mediaInfo.setDataSource(songsList.get(songIndex).get("songPath"));
			byte[] img = mediaInfo.getEmbeddedPicture();
			if (img != null)
				imgAlbum.setImageBitmap(BitmapFactory.decodeByteArray(img, 0,
						img.length));
			else
				imgAlbum.setImageResource(R.drawable.selena);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// what to do when a song ends
	@Override
	public void onCompletion(MediaPlayer arg0) {
		if (isShuffle) {
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex);
		}
	}


	/* Function called before exitting the music activity */

	@Override
	public void onDestroy() {
		mp.release();
		finish();
		super.onDestroy();

	}



	/* Function called when hardware Volume buttons are pressed */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (keyCode) {

			case KeyEvent.KEYCODE_VOLUME_UP:
				float vol = (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
						.getStreamVolume(AudioManager.STREAM_MUSIC)
						/ (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
								.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
						* (float) 100;
				volumeSeekbar.setProgress((int) vol);
				// setVolIcon();
				if (vol > 0)
					btnMute.setImageResource(R.drawable.n_unmute);
				else
					btnMute.setImageResource(R.drawable.n_mute);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				float vol2 = (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
						.getStreamVolume(AudioManager.STREAM_MUSIC)
						/ (float) ((AudioManager) getSystemService(AUDIO_SERVICE))
								.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
						* (float) 100;
				volumeSeekbar.setProgress((int) vol2);
				// setVolIcon();
				if (vol2 > 0)
					btnMute.setImageResource(R.drawable.n_unmute);
				else
					btnMute.setImageResource(R.drawable.n_mute);
				return true;

			default:
				return super.dispatchKeyEvent(event);
			}
		}
		return super.dispatchKeyEvent(event);
	}

}