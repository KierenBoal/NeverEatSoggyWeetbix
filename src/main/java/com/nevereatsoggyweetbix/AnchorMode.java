package com.nevereatsoggyweetbix;

public enum AnchorMode
{
	PLAYER_FEET("Snap to player feet"),
	PLAYER_HEAD("Snap to player head"),
	PLAYER_TRUE_TILE("Snap to player true tile"),
	SCREEN_CENTER("Middle of screen"),
	MOUSE_POSITION("Snap to mouse position"),
	MOUSE_TILE("Snap to mouse hovered tile");

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
