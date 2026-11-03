package nevavis.music;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.EXTThreadLocalContext;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;

/**
 * Запасной вывод через OpenAL (тот же движок, что использует Minecraft). Создаёт собственное
 * устройство и контекст, привязанный к этому потоку, поэтому не мешает звукам игры.
 */
final class AlSink implements AudioSink {
	private static final int BUFFERS = 6;

	private final long device;
	private final long context;
	private final int source;
	private final int[] buffers = new int[BUFFERS];
	private final int[] bufFrames = new int[BUFFERS];
	private final ArrayDeque<Integer> free = new ArrayDeque<>();
	private final int format;
	private final int rate;
	private final int frameBytes;
	private long processedFrames;
	private boolean paused;

	AlSink(int rate, int channels) throws Exception {
		if (channels != 1 && channels != 2) {
			throw new IOException("Unsupported channel count: " + channels);
		}
		this.rate = rate;
		this.frameBytes = channels * 2;
		this.format = channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;

		device = ALC10.alcOpenDevice((ByteBuffer) null);
		if (device == 0L) {
			throw new IOException("OpenAL: no output device");
		}
		ALCCapabilities caps = ALC.createCapabilities(device);
		context = ALC10.alcCreateContext(device, (IntBuffer) null);
		if (!caps.ALC_EXT_thread_local_context || !EXTThreadLocalContext.alcSetThreadContext(context)) {
			ALC10.alcDestroyContext(context);
			ALC10.alcCloseDevice(device);
			throw new IOException("OpenAL: thread-local context is not supported");
		}
		AL.createCapabilities(caps);
		source = AL10.alGenSources();
		for (int i = 0; i < BUFFERS; i++) {
			buffers[i] = AL10.alGenBuffers();
			free.add(i);
		}
	}

	private int acquire() throws InterruptedException {
		while (true) {
			reclaim();
			if (!free.isEmpty()) {
				return free.poll();
			}
			Thread.sleep(5);
		}
	}

	private void reclaim() {
		int n = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
		while (n-- > 0) {
			int name = AL10.alSourceUnqueueBuffers(source);
			for (int i = 0; i < BUFFERS; i++) {
				if (buffers[i] == name) {
					processedFrames += bufFrames[i];
					free.add(i);
					break;
				}
			}
		}
	}

	@Override
	public void write(byte[] data, int len) throws Exception {
		int idx = acquire();
		ByteBuffer bb = MemoryUtil.memAlloc(len);
		try {
			bb.put(data, 0, len).flip();
			AL10.alBufferData(buffers[idx], format, bb, rate);
		} finally {
			MemoryUtil.memFree(bb);
		}
		bufFrames[idx] = len / frameBytes;
		AL10.alSourceQueueBuffers(source, buffers[idx]);
		if (!paused && AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
			AL10.alSourcePlay(source);
		}
	}

	@Override
	public void pause() {
		paused = true;
		AL10.alSourcePause(source);
	}

	@Override
	public void resume() {
		paused = false;
		AL10.alSourcePlay(source);
	}

	@Override
	public long positionMs() {
		reclaim();
		return processedFrames * 1000L / rate;
	}

	@Override
	public void drain() throws Exception {
		while (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
			Thread.sleep(20);
		}
	}

	@Override
	public void flush() {
		AL10.alSourceStop(source);
	}

	@Override
	public void close() {
		try {
			AL10.alSourceStop(source);
			AL10.alDeleteSources(source);
			for (int b : buffers) {
				AL10.alDeleteBuffers(b);
			}
		} finally {
			EXTThreadLocalContext.alcSetThreadContext(0L);
			ALC10.alcDestroyContext(context);
			ALC10.alcCloseDevice(device);
		}
	}
}
