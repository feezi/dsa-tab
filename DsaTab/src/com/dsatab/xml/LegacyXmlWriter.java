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
package com.dsatab.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dsatab.data.Hero;
import com.gandulf.guilib.util.Debug;

/**
 * @author Seraphim
 * 
 */
public class LegacyXmlWriter {

	private static String getStringFromNode(Node root) throws IOException {

		StringBuilder result = new StringBuilder();

		if (root.getNodeName().equalsIgnoreCase("xml-stylesheet")) {
			return "";
		}

		if (root.getNodeType() == 3)
			result.append(root.getNodeValue());
		else {
			if (root.getNodeType() != 9) {

				StringBuffer attrs = new StringBuffer();
				if (root.getAttributes() != null) {
					for (int k = 0; k < root.getAttributes().getLength(); ++k) {

						String nodeValue = root.getAttributes().item(k).getNodeValue();
						nodeValue = nodeValue.replace("&", "&amp;");
						attrs.append(" ").append(root.getAttributes().item(k).getNodeName()).append("=\"")
								.append(nodeValue).append("\" ");
					}
				}
				result.append("<").append(root.getNodeName()).append(" ").append(attrs).append(">");

			} else {
				result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			}

			NodeList nodes = root.getChildNodes();
			if (nodes != null) {
				for (int i = 0, j = nodes.getLength(); i < j; i++) {
					Node node = nodes.item(i);
					result.append(getStringFromNode(node));
				}
			}

			if (root.getNodeType() != 9) {
				result.append("</").append(root.getNodeName()).append(">");
			}
		}
		return result.toString();
	}

	public static void writeHero(Hero hero, OutputStream out) {

		try {
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
			w.write(getStringFromNode(hero.getDocument()));
			w.close();
		} catch (IOException e) {
			Debug.error(e);
		}
	}
}
