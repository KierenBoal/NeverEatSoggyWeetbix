package com.nevereatsoggyweetbix;

public enum KeybindMode
{
	TOGGLE("Toggle"),
	HOLD("Hold"),
	HOLD_INVISIBLE("Hold invisible");

	private final String label;

	KeybindMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
