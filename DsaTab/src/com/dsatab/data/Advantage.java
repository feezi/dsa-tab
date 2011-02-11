package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

public class Advantage {

	private Element element;

	public Advantage(Element element) {
		this.element = element;
	}

	public String getName() {
		return element.getAttribute(Xml.KEY_NAME);
	}

	public void setName(String name) {
		element.setAttribute(Xml.KEY_NAME, name);
	}

	public String getComment() {
		return element.getAttribute(Xml.KEY_COMMENT);
	}

	public Integer getValue() {
		if (element.hasAttribute(Xml.KEY_VALUE))
			return Util.parseInt(element.getAttribute(Xml.KEY_VALUE));
		else
			return null;
	}

	public void setValue(Integer value) {
		element.setAttribute(Xml.KEY_VALUE, Util.toString(value));
	}

	@Override
	public String toString() {
		return getName();
	}

}
