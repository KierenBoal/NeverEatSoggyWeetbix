package com.nevereatsoggyweetbix;

public enum LetterRotation
{
	ALWAYS_UP("Upright"),
	ROTATION("Outward");

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
