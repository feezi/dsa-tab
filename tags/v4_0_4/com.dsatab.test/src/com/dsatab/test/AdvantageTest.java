package com.dsatab.test;

import android.test.AndroidTestCase;

import com.dsatab.data.Advantage;

public class AdvantageTest extends AndroidTestCase {

	public AdvantageTest() {
		super();
	}

	// Advantages without a value consist of only the name
	public void testToStringWithNoValue() {
		Advantage advantage = new Advantage(Advantage.BEIDHAENDIG);
		assertEquals(Advantage.BEIDHAENDIG, advantage.toString());
	}

	// Advantages with exactly one value consist of the name and the value
	public void testToStringWithOneValue() {
		Advantage advantage = new Advantage();
		advantage.setName("Neugier");
		advantage.addValue("7");
		assertEquals("Neugier 7", advantage.toString());
	}

	// Multiple values in advantages are shown in brackets
	public void testToStringWithMultipleValues() {
		Advantage advantage = new Advantage("Herausragender Sinn");
		advantage.addValue("Gehör");
		advantage.addValue("Geruch");
		assertEquals("Herausragender Sinn [Gehör, Geruch]", advantage.toString());
	}

	// Empty values will be omitted
	public void testToStringWithEmptyValue() {
		Advantage advantage = new Advantage(Advantage.BEIDHAENDIG);
		advantage.addValue("");
		assertEquals(Advantage.BEIDHAENDIG, advantage.toString());
	}
}
