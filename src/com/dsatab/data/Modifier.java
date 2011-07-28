package com.dsatab.data;

import java.util.Collection;

import com.dsatab.common.Util;

public class Modifier {
	private int modifier;

	private String title;

	private String description;

	public Modifier(int modifier, String title, String description) {
		super();
		this.modifier = modifier;
		this.title = title;
		this.description = description;
	}

	@Override
	public String toString() {
		return title + " " + Util.toProbe(modifier) + " | " + description;
	}

	public static final int sum(Collection<Modifier> mods) {
		int sum = 0;
		for (Modifier m : mods) {
			sum += m.modifier;
		}
		return sum;
	}

	public int getModifier() {
		return modifier;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

}