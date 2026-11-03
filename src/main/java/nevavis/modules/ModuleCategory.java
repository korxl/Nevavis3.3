package nevavis.modules;

public enum ModuleCategory {
	PLAYER("Player"),
	ENTITIES("Entities"),
	ITEMS("Items"),
	WORLD("World"),
	ANIMATIONS("Animations");

	private final String display;

	ModuleCategory(String display) {
		this.display = display;
	}

	public String display() {
		return display;
	}
}
