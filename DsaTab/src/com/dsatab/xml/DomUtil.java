/**
 *  This file is part of DsaTab.
 *
 *  DsaTab is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DsaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DsaTab.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsatab.xml;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ganymede
 * 
 */
public class DomUtil {

	public static Element getChildByTagName(Element parent, String tagName) {

		NodeList nodeList = parent.getElementsByTagName(tagName);

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node element = (Node) nodeList.item(i);

			if (element.getParentNode().equals(parent))
				return (Element) element;
		}

		return null;
	}

	public static List<Node> getChildrenByTagName(Element parent, String tagName) {

		List<Node> children = new LinkedList<Node>();

		NodeList spellList = parent.getElementsByTagName(tagName);

		for (int i = 0; i < spellList.getLength(); i++) {
			Node element = (Node) spellList.item(i);

			if (element.getParentNode().equals(parent))
				children.add(element);
		}

		return children;

	}
}
