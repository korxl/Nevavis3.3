package nevavis.themes;

/**
 * Палитра темы. Цвета в формате 0xRRGGBB.
 * bg — фон окна, surface — стеклянные панели, accent/accent2 — акценты (градиент), text/dim — текст.
 */
public record Theme(String name, int bg, int surface, int accent, int accent2, int text, int dim) {
}
