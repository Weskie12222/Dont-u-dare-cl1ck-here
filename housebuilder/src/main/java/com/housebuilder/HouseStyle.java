package com.housebuilder;

public enum HouseStyle {
	WOODEN("Wooden house"),
	MODERN("Modern house"),
	MILITARY("Military barracks");

	private final String displayName;

	HouseStyle(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
