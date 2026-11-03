package nevavis.presence;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nevavis.config.ConfigManager;

/** Значок «N» перед ником игроков, которые играют с NevaVis (глиф из шрифта nevavis:badge). */
public final class Badge {
	private static final String GLYPH = "";

	private Badge() {
	}

	public static boolean enabled() {
		return ConfigManager.get().data().showBadge;
	}

	public static Text prefix(Text original) {
		MutableText glyph = Text.literal(GLYPH).setStyle(Style.EMPTY.withFont(Identifier.of("nevavis", "badge")));
		return glyph.append(Text.literal(" ")).append(original);
	}

	/** Для списка игроков (Tab): ищет ник известного пользователя в готовой строке. */
	public static boolean textContainsUser(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		String self = PresenceManager.selfName();
		if (!self.isEmpty() && containsWord(text, self)) {
			return true;
		}
		for (String n : PresenceManager.users()) {
			if (containsWord(text.toLowerCase(), n)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsWord(String text, String word) {
		int from = 0;
		while (true) {
			int i = text.toLowerCase().indexOf(word.toLowerCase(), from);
			if (i < 0) {
				return false;
			}
			boolean left = i == 0 || !isNameChar(text.charAt(i - 1));
			int end = i + word.length();
			boolean right = end >= text.length() || !isNameChar(text.charAt(end));
			if (left && right) {
				return true;
			}
			from = i + 1;
		}
	}

	private static boolean isNameChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
}
