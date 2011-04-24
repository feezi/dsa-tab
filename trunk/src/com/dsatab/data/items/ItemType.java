/*
 * Copyright (C) 2010 Gandulf Kohlweiss
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.dsatab.data.items;

public enum ItemType {
	Waffen('W', "weapons", true), Fernwaffen('D', "weapons", true), Rüstung('A', "weapons", true), Schilde('S',
			"weapons", true), Sonstiges('M', "misc"), Behälter('B', "bags"), Kleidung('C', "cloths"), Special('X',
			"special");

	private final String path;

	private final char character;

	private final boolean equipable;

	/**
	 * 
	 */

	private ItemType(char c, String path, boolean equipable) {
		this.path = path;
		this.character = c;
		this.equipable = equipable;
	}

	private ItemType(char c, String path) {
		this.path = path;
		this.character = c;
		this.equipable = false;
	}

	public String getPath() {
		return path;
	}

	public char character() {
		return character;
	}

	public boolean isEquipable() {
		return equipable;
	}

	public static ItemType fromColor(String s) {

		if ("gruen".equalsIgnoreCase(s)) {
			return Behälter;
		} else if ("gelb".equalsIgnoreCase(s)) {
			return Waffen;
		} else if ("grau".equalsIgnoreCase(s)) {
			return Kleidung;
		} else if ("rot".equalsIgnoreCase(s)) {
			return Sonstiges;
		} else if ("blau".equalsIgnoreCase(s)) {
			return Special;
		} else {
			return null;
		}
	}

	public static ItemType fromCharacter(char c) {

		switch (c) {
		case 'W':
			return Waffen;
		case 'D':
			return Fernwaffen;
		case 'A':
			return Rüstung;
		case 'S':
			return Schilde;
		case 'M':
			return Sonstiges;
		case 'B':
			return Behälter;
		case 'C':
			return Kleidung;
		case 'X':
			return Special;
		default:
			return null;
		}
	}
}
