package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.common.Util;
import com.dsatab.xml.Xml;

public class Advantage {

	private String name;
	private String comment;
	private String valueString;

	public Advantage(Element element) {

		this.name = element.getAttribute(Xml.KEY_NAME);

		if (name.startsWith("Begabung für ") && getValueAsString() != null) {
			this.name = "Begabung für " + getValueAsString();
		}

		this.comment = element.getAttribute(Xml.KEY_COMMENT);

		if (element.hasAttribute(Xml.KEY_VALUE))
			this.valueString = element.getAttribute(Xml.KEY_VALUE);

	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public Integer getValue() {
		if (valueString != null)
			return Util.parseInt(valueString);
		else
			return null;
	}

	public String getValueAsString() {
		return valueString;
	}

	@Override
	public String toString() {
		return getName();
	}
}
