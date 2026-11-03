package nevavis.music;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/** Декодеры форматов: WAV (JDK), OGG (STB Vorbis из LWJGL), MP3 (JLayer). */
public final class Decoders {
	private Decoders() {
	}

	public static boolean isSupported(Path p) {
		String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
		return n.endsWith(".mp3") || n.endsWith(".ogg") || n.endsWith(".wav");
	}

	public static PcmStream open(Path p) throws Exception {
		String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
		if (n.endsWith(".mp3")) {
			return new Mp3(p);
		}
		if (n.endsWith(".ogg")) {
			return new Ogg(p);
		}
		if (n.endsWith(".wav")) {
			return new Wav(p);
		}
		throw new IOException("Unsupported format: " + n);
	}

	/** Длительность WAV из заголовка (дёшево), для остальных форматов 0. */
	public static long quickDurationMs(Path p) {
		if (!p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".wav")) {
			return 0L;
		}
		try {
			AudioFormat f = AudioSystem.getAudioFileFormat(p.toFile()).getFormat();
			long frames = AudioSystem.getAudioFileFormat(p.toFile()).getFrameLength();
			if (frames > 0 && f.getFrameRate() > 0) {
				return (long) (frames * 1000.0 / f.getFrameRate());
			}
		} catch (Exception ignored) {
			// файл повреждён — длительность неизвестна
		}
		return 0L;
	}

	// ---------------- WAV ----------------
	static final class Wav implements PcmStream {
		private final AudioInputStream pcm;
		private final int rate, ch;
		private final long dur;
		private final byte[] buf = new byte[8192];

		Wav(Path p) throws Exception {
			AudioInputStream in = AudioSystem.getAudioInputStream(p.toFile());
			AudioFormat f = in.getFormat();
			rate = (int) f.getSampleRate();
			ch = f.getChannels();
			long frames = in.getFrameLength();
			dur = frames > 0 && rate > 0 ? frames * 1000L / rate : 0L;
			AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
			pcm = AudioSystem.getAudioInputStream(target, in);
		}

		@Override
		public int sampleRate() {
			return rate;
		}

		@Override
		public int channels() {
			return ch;
		}

		@Override
		public long durationMs() {
			return dur;
		}

		@Override
		public short[] next() throws IOException {
			int frameBytes = ch * 2;
			int n = pcm.read(buf, 0, buf.length - (buf.length % frameBytes));
			if (n <= 0) {
				return null;
			}
			n -= n % frameBytes;
			short[] out = new short[n / 2];
			for (int i = 0; i < out.length; i++) {
				out[i] = (short) ((buf[2 * i] & 0xFF) | (buf[2 * i + 1] << 8));
			}
			return out;
		}

		@Override
		public void close() {
			try {
				pcm.close();
			} catch (IOException ignored) {
				// ничего
			}
		}
	}

	// ---------------- OGG ----------------
	static final class Ogg implements PcmStream {
		private static final int CHUNK = 8192;
		private final short[] data;
		private final int rate, ch;
		private int pos;

		Ogg(Path p) throws IOException {
			byte[] bytes = Files.readAllBytes(p);
			ByteBuffer mem = MemoryUtil.memAlloc(bytes.length);
			int c, r;
			short[] decoded;
			try {
				mem.put(bytes).flip();
				try (MemoryStack st = MemoryStack.stackPush()) {
					IntBuffer chBuf = st.mallocInt(1);
					IntBuffer rateBuf = st.mallocInt(1);
					ShortBuffer pcm = STBVorbis.stb_vorbis_decode_memory(mem, chBuf, rateBuf);
					if (pcm == null) {
						throw new IOException("Cannot decode OGG: " + p.getFileName());
					}
					try {
						c = chBuf.get(0);
						r = rateBuf.get(0);
						decoded = new short[pcm.remaining()];
						pcm.get(decoded);
					} finally {
						LibCStdlib.free(pcm);
					}
				}
			} finally {
				MemoryUtil.memFree(mem);
			}
			this.ch = c;
			this.rate = r;
			this.data = decoded;
		}

		@Override
		public int sampleRate() {
			return rate;
		}

		@Override
		public int channels() {
			return ch;
		}

		@Override
		public long durationMs() {
			return rate > 0 && ch > 0 ? (long) data.length / ch * 1000L / rate : 0L;
		}

		@Override
		public short[] next() {
			if (pos >= data.length) {
				return null;
			}
			int end = Math.min(data.length, pos + CHUNK - (CHUNK % ch));
			short[] out = Arrays.copyOfRange(data, pos, end);
			pos = end;
			return out;
		}

		@Override
		public void close() {
		}
	}

	// ---------------- MP3 ----------------
	static final class Mp3 implements PcmStream {
		private final Bitstream bs;
		private final Decoder dec = new Decoder();
		private final int rate, ch;
		private final long dur;
		private short[] first;

		Mp3(Path p) throws Exception {
			InputStream in = new BufferedInputStream(Files.newInputStream(p));
			bs = new Bitstream(in);
			Header h = bs.readFrame();
			if (h == null) {
				throw new IOException("Empty MP3: " + p.getFileName());
			}
			dur = (long) h.total_ms((int) Math.min(Integer.MAX_VALUE, Files.size(p)));
			SampleBuffer sb = (SampleBuffer) dec.decodeFrame(h, bs);
			rate = sb.getSampleFrequency();
			ch = sb.getChannelCount();
			first = Arrays.copyOf(sb.getBuffer(), sb.getBufferLength());
			bs.closeFrame();
		}

		@Override
		public int sampleRate() {
			return rate;
		}

		@Override
		public int channels() {
			return ch;
		}

		@Override
		public long durationMs() {
			return dur;
		}

		@Override
		public short[] next() throws Exception {
			if (first != null) {
				short[] f = first;
				first = null;
				return f;
			}
			Header h = bs.readFrame();
			if (h == null) {
				return null;
			}
			SampleBuffer sb = (SampleBuffer) dec.decodeFrame(h, bs);
			short[] out = Arrays.copyOf(sb.getBuffer(), sb.getBufferLength());
			bs.closeFrame();
			return out;
		}

		@Override
		public void close() {
			try {
				bs.close();
			} catch (Exception ignored) {
				// ничего
			}
		}
	}
}
