package nevavis.music;

import java.nio.file.Path;

/** Трек в библиотеке. file — путь относительно папки музыки (с '/'), durationMs = 0 если неизвестно. */
public record Track(Path path, String file, String title, String artist, long durationMs) {
}
