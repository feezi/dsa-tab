package com.dsatab.test;

import org.jdom.Element;

import com.dsatab.data.Advantage;
import com.dsatab.xml.Xml;

import android.test.AndroidTestCase;

public class AdvantageTest extends AndroidTestCase {

	public AdvantageTest(){
		super();
	}
	
	//Advantages without a value consist of only the name
	public void testToStringWithNoValue(){
		org.jdom.Element element = new Element(Xml.KEY_VORTEIL);
		element.setAttribute(Xml.KEY_NAME, Advantage.BEIDHAENDIG);
		Advantage advantage = new Advantage(element);
		assertEquals(Advantage.BEIDHAENDIG, advantage.toString());
	}
	
	//Advantages with exactly one value consist of the name and the value
	public void testToStringWithOneValue(){
		org.jdom.Element element = new Element(Xml.KEY_VORTEIL);
		element.setAttribute(Xml.KEY_NAME, "Neugier");
		element.setAttribute(Xml.KEY_VALUE, "7");
		Advantage advantage = new Advantage(element);
		assertEquals("Neugier 7", advantage.toString());
	}
	
	//Multiple values in advantages are shown in brackets
	public void testToStringWithMultipleValues(){
		org.jdom.Element element = new Element(Xml.KEY_VORTEIL);
		element.setAttribute(Xml.KEY_NAME, "Herausragender Sinn");
		element.setAttribute(Xml.KEY_VALUE, "Geh�r");
		Advantage advantage = new Advantage(element);
		advantage.addValue("Geruch");
		assertEquals("Herausragender Sinn [Geh�r, Geruch]", advantage.toString());
	}
	
	//Empty values will be omitted
	public void testToStringWithEmptyValue(){
		org.jdom.Element element = new Element(Xml.KEY_VORTEIL);
		element.setAttribute(Xml.KEY_NAME, Advantage.BEIDHAENDIG);
		element.setAttribute(Xml.KEY_VALUE, "");
		Advantage advantage = new Advantage(element);
		assertEquals(Advantage.BEIDHAENDIG, advantage.toString());
	}
}