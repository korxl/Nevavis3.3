package nevavis.music;

/** Приёмник звука: принимает 16-bit little-endian PCM. Реализации: JavaSound и OpenAL (запасной). */
public interface AudioSink extends AutoCloseable {
	/** Записывает данные; блокируется, пока в буфере нет места. */
	void write(byte[] data, int len) throws Exception;

	void pause();

	void resume();

	/** Позиция воспроизведения в мс. */
	long positionMs();

	/** Дождаться конца проигрывания буфера. */
	void drain() throws Exception;

	/** Сбросить буфер (переключение трека). */
	void flush();

	@Override
	void close();
}
