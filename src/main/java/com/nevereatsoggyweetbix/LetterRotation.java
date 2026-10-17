package com.nevereatsoggyweetbix;

public enum LetterRotation
{
	ALWAYS_UP("Always up"),
	ROTATION("Rotation");

	private final String label;

	LetterRotation(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
