package nevavis.music;

/** Поток декодированного звука: 16-bit PCM little-endian, чередующиеся каналы. */
public interface PcmStream extends AutoCloseable {
	int sampleRate();

	int channels();

	/** Длительность в мс или 0, если неизвестна. */
	long durationMs();

	/** Следующий блок сэмплов или null в конце трека. */
	short[] next() throws Exception;

	@Override
	void close();
}
