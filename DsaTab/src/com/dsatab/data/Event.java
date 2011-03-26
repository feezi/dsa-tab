package com.dsatab.data;

import org.w3c.dom.Element;

import com.dsatab.data.enums.EventCategory;
import com.dsatab.xml.Xml;

public class Event {

	private static final String SEPERATOR = ";";
	private static final String PREFIX_AUDIO = "AUDIO:";
	private static final String PREFIX_CATEGORY = "CATEGORY:";

	private transient Element element;

	private String audioPath;

	private String comment;

	private EventCategory category;

	public Event(Element element) {
		this.element = element;

		if (!element.hasAttribute(Xml.KEY_TEXT)) {
			element.setAttribute(Xml.KEY_TEXT, "Sonstiges Ereignis (Hinzugewinn)");
		}

		if (element.hasAttribute(Xml.KEY_KOMMENTAR)) {

			String s = element.getAttribute(Xml.KEY_KOMMENTAR);

			if (s.startsWith(PREFIX_CATEGORY)) {
				this.category = EventCategory.valueOf(s.substring(9, s.indexOf(SEPERATOR)));
				s = s.substring(s.indexOf(SEPERATOR) + 1);
			}

			if (s.startsWith(PREFIX_AUDIO)) {
				audioPath = s.substring(6, s.indexOf(SEPERATOR));
				s = s.substring(s.indexOf(SEPERATOR) + 1);
			}

			comment = s;

		}

	}

	private String getConcatComment() {
		StringBuilder sb = new StringBuilder();

		if (category != null) {
			sb.append(PREFIX_CATEGORY);
			sb.append(category.name());
			sb.append(SEPERATOR);
		}
		if (audioPath != null) {
			sb.append(PREFIX_AUDIO);
			sb.append(audioPath);
			sb.append(SEPERATOR);
		}
		if (sb != null)
			sb.append(comment);

		return sb.toString();
	}

	public void setComment(String message) {
		this.comment = message;
		element.setAttribute(Xml.KEY_KOMMENTAR, getConcatComment());
	}

	public String getComment() {
		return comment;
	}

	public String getType() {
		return element.getAttribute(Xml.KEY_TEXT);
	}

	public String getTime() {
		return element.getAttribute(Xml.KEY_TIME);
	}

	public String getVersion() {
		return element.getAttribute(Xml.KEY_VERSION);
	}

	public Element getElement() {
		return element;
	}

	public EventCategory getCategory() {
		return category;
	}

	public void setCategory(EventCategory category) {
		this.category = category;
		element.setAttribute(Xml.KEY_KOMMENTAR, getConcatComment());
	}

	public String getAudioPath() {
		return audioPath;
	}

	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
		element.setAttribute(Xml.KEY_KOMMENTAR, getConcatComment());
	}

}
