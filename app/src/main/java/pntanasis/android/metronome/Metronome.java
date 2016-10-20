package pntanasis.android.metronome;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.andreyaleev.metrosong.metronome.SongSnippet;

import java.util.ArrayList;

public class Metronome {

	public static int SONG_ENDED = 1000;
	public final static String BPM = "bpm";
	public final static String CURRENT_BEAT = "currentBeat";
	public final static String NOTE_VALUE = "noteValue";
	public final static String BARS_COUNT = "barsCount";
	public final static String BARS_PASSED = "barsPassed";
	public final static String BEATS_PER_BAR = "beatsPerBar";
	public final static String SNIPPET_NUMBER = "snippestNumber";

	private final static String TAG = "Metronome";


	private double bpm;
	private int beat;
	private int noteValue;
	private int silence;

	private double beatSound;
	private double sound;
	private final int tick = 1000; // samples of tick

	private boolean play = true;
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
	private Handler mHandler;
	private double[] soundTickArray;
	private double[] soundTockArray;
	private double[] silenceSoundArray;
	private Message msg;
	private int currentBeat = 1;

	private boolean isPlay = false;

	public boolean isPlay() {
		return this.isPlay;
	}

	public Metronome() {
		audioGenerator.createPlayer();
	}

	public Metronome(Handler handler) {
		audioGenerator.createPlayer();
		this.mHandler = handler;
	}

	public void calcSilence() {
		silence = (int) (((60 / bpm) * 8000) - tick);
		soundTickArray = new double[this.tick];
		soundTockArray = new double[this.tick];
		silenceSoundArray = new double[this.silence];
		msg = new Message();
		msg.obj = "" + currentBeat;
		double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
		double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
		for (int i = 0; i < this.tick; i++) {
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
//		for(int i=0;i<silence;i++)
//			silenceSoundArray[i] = 0;
	}

	public void play() {
		calcSilence();
		do {
			isPlay = true;
			if (mHandler != null) {
				Log.d(TAG, "mHandler != null");
				msg = new Message();
				msg.obj = "" + currentBeat;
				Bundle data = new Bundle();
				data.putInt("currentBeat", currentBeat);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
			if (currentBeat == 1) {
				audioGenerator.writeSound(soundTickArray);
			} else {
				audioGenerator.writeSound(soundTockArray);
			}
			audioGenerator.writeSilence(silence);
			currentBeat++;
			if (currentBeat > beat)
				currentBeat = 1;
		} while (play);
		isPlay = false;
	}


	public void playProgram(ArrayList<SongSnippet> snippets) {
		double[] tickArray = audioGenerator.getSineWave(this.tick, 8000, beatSound);
		double[] tockArray = audioGenerator.getSineWave(this.tick, 8000, sound);

		for (int k = 0; k < snippets.size(); k++) {
//		for(SongSnippet snippet: snippets){
			SongSnippet snippet = snippets.get(k);
			int bpm = snippet.getBpm();
			int barsCount = snippet.getBarsCount();
			int beatsPerBar = snippet.getBeatsPerBar();
			int noteValue = snippet.getNoteValue();
			setNoteValue(noteValue); // ???

			// init arrays for snippet
			int silenceCoef = 480000 / bpm;
			int silence = silenceCoef - tick;
			double[] soundTickArray = new double[this.tick];
			double[] soundTockArray = new double[this.tick];
			double[] silenceSoundArray = new double[silence];
			for (int i = 0; i < this.tick; i++) {
				soundTickArray[i] = tickArray[i];
				soundTockArray[i] = tockArray[i];
			}
			for (int i = 0; i < silence; i++) {
				silenceSoundArray[i] = 0;
			}

			int currentBeatProgram = 1;
			int i = 0;
			while (i < barsCount) {
				isPlay = true;

				if (mHandler != null) {
					msg = new Message();
					msg.obj = "" + currentBeat;
					Bundle data = new Bundle();
					data.putInt(BPM, bpm);
					data.putInt(CURRENT_BEAT, currentBeatProgram);
					data.putInt(NOTE_VALUE, noteValue);
					data.putInt(BARS_COUNT, barsCount);
					data.putInt(BARS_PASSED, i);
					data.putInt(BEATS_PER_BAR, beatsPerBar);
					data.putInt(SNIPPET_NUMBER, k);
					msg.setData(data);
					mHandler.sendMessage(msg);
				}

				if (currentBeatProgram == 1) {
					audioGenerator.writeSound(soundTickArray);
				} else {
					audioGenerator.writeSound(soundTockArray);
				}
				audioGenerator.writeSilence(silence);

				currentBeatProgram++;
				if (currentBeatProgram > beatsPerBar) {
					currentBeatProgram = 1;
					i++;
				}
			}
		}
		mHandler.sendEmptyMessage(SONG_ENDED);
		isPlay = false;
	}

	public void stop() {
		play = false;
		isPlay = false;
		audioGenerator.destroyAudioTrack();
	}

	public double getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public int getNoteValue() {
		return noteValue;
	}

	public void setNoteValue(int bpmetre) {
		this.noteValue = bpmetre;
	}

	public int getBeat() {
		return beat;
	}

	public void setBeat(int beat) {
		this.beat = beat;
	}

	public double getBeatSound() {
		return beatSound;
	}

	public void setBeatSound(double sound1) {
		this.beatSound = sound1;
	}

	public double getSound() {
		return sound;
	}

	public void setSound(double sound2) {
		this.sound = sound2;
	}

}
