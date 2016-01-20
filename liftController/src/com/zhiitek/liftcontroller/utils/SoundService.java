package com.zhiitek.liftcontroller.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.SparseArray;


/**
 * 声音服务类
 */
public class SoundService {

	// 声音资源缓存, 以声音资源的ID作为KEY。
	private SparseArray<MediaPlayer> soundCacheEmergencyMusic = new SparseArray<MediaPlayer>();

	private Context context;

	private static class SingletonHolder {
		public final static SoundService instance = new SoundService();
	}

	public static SoundService getInstance() {
		return SingletonHolder.instance;
	}
	
	public void init(Context context) {
		this.context = context;
	}

	/**
	 * 播放声音
	 *
	 * @param resId
	 */
	public void playVoice(int resId) {
		MediaPlayer mp = soundCacheEmergencyMusic.get(resId);
		try {
			if (mp == null) {
				mp = MediaPlayer.create(context, resId);
				soundCacheEmergencyMusic.put(resId, mp);
				mp.start();
			} else {
				mp.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 播放声音
	 *
	 * @param resId
	 */
	public void playVoice(int resId, OnCompletionListener completionListener) {
		MediaPlayer mp = soundCacheEmergencyMusic.get(resId);
		try {
			if (mp == null) {
				mp = MediaPlayer.create(context, resId);
				soundCacheEmergencyMusic.put(resId, mp);
				mp.setOnCompletionListener(completionListener);
				mp.start();
			} else {
				mp.setOnCompletionListener(completionListener);
				mp.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 播放录音
	 * @param resPath 录音文件路径
	 * @param completionListener
	 * @throws Exception
	 */
	public void playVoice(String resPath, OnCompletionListener completionListener) throws Exception{
		MediaPlayer mp = soundCacheEmergencyMusic.get(resPath.hashCode());// 以录音文件路径的hashCode作为KEY
		if (mp == null) {
			mp = new MediaPlayer();
			mp.setDataSource(resPath);
			soundCacheEmergencyMusic.put(resPath.hashCode(), mp);
			mp.setOnCompletionListener(completionListener);
			mp.prepare();
			mp.start();
		} else {
			mp.setOnCompletionListener(completionListener);
			mp.start();
		}
	}
	
	public void stopPlay(int resId){
		MediaPlayer mediaPlayer = soundCacheEmergencyMusic.get(resId);
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			//不能调用mediaPlayer.stop()方法
			mediaPlayer.pause();
			mediaPlayer.seekTo(0);
		}
	}

	public void stopPlay(String resPath) {
		MediaPlayer mp = soundCacheEmergencyMusic.get(resPath.hashCode());
		if (mp != null && mp.isPlaying()) {
			mp.pause();
			mp.seekTo(0);
		}
	}

	public void release() {
		for (int i = 0; i < soundCacheEmergencyMusic.size(); i++) {
			MediaPlayer mediaPlayer = soundCacheEmergencyMusic.get(soundCacheEmergencyMusic.keyAt(i));
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				mediaPlayer.release();
				soundCacheEmergencyMusic.remove(soundCacheEmergencyMusic.keyAt(i));
			}
		}
	}
}
