package com.nevereatsoggyweetbix;

public enum AnchorMode
{
	PLAYER_FEET("Player feet"),
	PLAYER_HEAD("Player head"),
	PLAYER_TRUE_TILE("True tile"),
	SCREEN_CENTER("Screen center"),
	MOUSE_POSITION("Mouse"),
	MOUSE_TILE("Hovered tile");

	private final String label;

	AnchorMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
