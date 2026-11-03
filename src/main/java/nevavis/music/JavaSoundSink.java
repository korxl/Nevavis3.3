package nevavis.music;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/** Вывод через javax.sound. Пробует стандартную линию, затем все микшеры по очереди. */
final class JavaSoundSink implements AudioSink {
	private final SourceDataLine line;
	private final int rate;

	JavaSoundSink(int rate, int channels) throws Exception {
		this.rate = rate;
		AudioFormat fmt = new AudioFormat(rate, 16, channels, true, false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
		SourceDataLine l = null;
		Exception first = null;
		try {
			l = (SourceDataLine) AudioSystem.getLine(info);
		} catch (Exception e) {
			first = e;
		}
		if (l == null) {
			for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
				try {
					Mixer mixer = AudioSystem.getMixer(mi);
					if (mixer.isLineSupported(info)) {
						Line cand = mixer.getLine(info);
						l = (SourceDataLine) cand;
						break;
					}
				} catch (Exception ignored) {
					// пробуем следующий микшер
				}
			}
		}
		if (l == null) {
			throw first != null ? first : new IllegalStateException("No audio output line available");
		}
		l.open(fmt, fmt.getFrameSize() * (rate / 4));
		l.start();
		this.line = l;
	}

	@Override
	public void write(byte[] data, int len) {
		line.write(data, 0, len);
	}

	@Override
	public void pause() {
		line.stop();
	}

	@Override
	public void resume() {
		line.start();
	}

	@Override
	public long positionMs() {
		return line.getLongFramePosition() * 1000L / rate;
	}

	@Override
	public void drain() {
		line.drain();
	}

	@Override
	public void flush() {
		line.flush();
	}

	@Override
	public void close() {
		line.close();
	}
}
