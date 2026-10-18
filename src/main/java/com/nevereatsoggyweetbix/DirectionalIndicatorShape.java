package com.nevereatsoggyweetbix;

public enum DirectionalIndicatorShape
{
	TRIANGLE("Triangle"),
	ARROW("Arrow"),
	SQUARE("Square"),
	CIRCLE("Circle");

	private final String label;

	DirectionalIndicatorShape(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
