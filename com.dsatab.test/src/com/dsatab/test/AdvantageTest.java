package com.dsatab.test;

import org.jdom.Element;

import com.dsatab.data.Advantage;
import com.dsatab.xml.Xml;

import android.test.AndroidTestCase;

public class AdvantageTest extends AndroidTestCase {

	public AdvantageTest(){
		super();
	}
	
	public void testToStringWithNoValue(){
		org.jdom.Element element = new Element("Advantage");
		element.setAttribute(Xml.KEY_NAME, Advantage.BEIDHAENDIG);
		Advantage advantage = new Advantage(element);
		assertEquals(Advantage.BEIDHAENDIG, advantage.toString());
	}
	
	public void testToStringWithOneValue(){
		org.jdom.Element element = new Element("Advantage");
		element.setAttribute(Xml.KEY_NAME, "Neugier");
		element.setAttribute(Xml.KEY_VALUE, "7");
		Advantage advantage = new Advantage(element);
		assertEquals("Neugier 7", advantage.toString());
	}
	
	public void testToStringWithMultipleValues(){
		org.jdom.Element element = new Element("Advantage");
		element.setAttribute(Xml.KEY_NAME, "Herausragender Sinn");
		element.setAttribute(Xml.KEY_VALUE, "Gehör");
		Advantage advantage = new Advantage(element);
		advantage.addValue("Geruch");
		assertEquals("Herausragender Sinn [Gehör, Geruch]", advantage.toString());
	}
	
	public void testToStringWithEmptyValue(){
		org.jdom.Element element = new Element("Advantage");
		element.setAttribute(Xml.KEY_NAME, Advantage.BEIDHAENDIG);
		element.setAttribute(Xml.KEY_VALUE, "");
		Advantage advantage = new Advantage(element);
		assertEquals(Advantage.BEIDHAENDIG, advantage.toString());
	}
}
