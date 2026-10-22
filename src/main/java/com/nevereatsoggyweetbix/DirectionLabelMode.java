package com.nevereatsoggyweetbix;

public enum DirectionLabelMode
{
	ABBREVIATIONS("Short"),
	FULL_NAMES("Full"),
	CUSTOM("Custom");

	private final String label;

	DirectionLabelMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
