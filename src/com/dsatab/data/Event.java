package com.dsatab.data;

import java.util.StringTokenizer;

import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import com.dsatab.common.Util;
import com.dsatab.data.enums.EventCategory;
import com.dsatab.xml.Xml;

public class Event implements JSONable {

	private static final String SEPERATOR = ";";
	private static final String PREFIX_AUDIO = "AUDIO:";
	private static final String PREFIX_CATEGORY = "CATEGORY:";

	private static final String FIELD_NAME = "name";
	private static final String FIELD_COMMENT = "comment";
	private static final String FIELD_CATEGORY = "category";
	private static final String FIELD_AUDIO_PATH = "auidoPath";
	private static final String FIELD_TIME = "time";

	private String audioPath;

	private String name;

	private String comment;

	private EventCategory category;

	private long time;

	private Element element;

	public Event() {
		this.time = System.currentTimeMillis();
		this.category = EventCategory.Misc;
	}

	public Event(JSONObject json) throws JSONException {

		if (json.has(FIELD_COMMENT))
			this.comment = json.getString(FIELD_COMMENT);

		if (json.has(FIELD_NAME))
			this.name = json.getString(FIELD_NAME);

		if (json.has(FIELD_AUDIO_PATH))
			this.audioPath = json.getString(FIELD_AUDIO_PATH);

		if (json.has(FIELD_TIME))
			this.time = json.getLong(FIELD_TIME);
		else
			this.time = System.currentTimeMillis();

		if (json.has(FIELD_CATEGORY))
			this.category = EventCategory.valueOf(json.getString(FIELD_CATEGORY));
		else
			this.category = EventCategory.Misc;

	}

	public Event(Element element) {
		this.category = EventCategory.Misc;

		if (element.getAttribute(Xml.KEY_KOMMENTAR) != null) {

			String s = element.getAttributeValue(Xml.KEY_KOMMENTAR);
			if (s != null) {
				if (s.startsWith(PREFIX_CATEGORY)) {
					this.category = EventCategory.valueOf(s.substring(PREFIX_CATEGORY.length(), s.indexOf(SEPERATOR)));
					s = s.substring(s.indexOf(SEPERATOR) + 1);
				}

				if (s.startsWith(PREFIX_AUDIO)) {
					this.audioPath = s.substring(PREFIX_AUDIO.length(), s.indexOf(SEPERATOR));
					s = s.substring(s.indexOf(SEPERATOR) + 1);
				}

				this.comment = s;
			}

			this.time = Util.parseLong(element.getAttributeValue(Xml.KEY_TIME));

			// UPGRADE: remove element since we will handle it in our json
			// configration
			// from now one
			if (element.getParentElement() != null) {
				element.getParentElement().removeContent(element);
			}
		}

		// special case for notiz elements they will be keep in the xml
		if (element.getAttribute(Xml.KEY_NOTIZ_PREFIX + "0") != null) {
			this.element = element;
			this.category = EventCategory.Heldensoftware;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i <= 11; i++) {
				sb.append(element.getAttributeValue(Xml.KEY_NOTIZ_PREFIX + i));
				sb.append("\n");
			}
			this.comment = sb.toString().trim();
		}

	}

	public void setComment(String message) {
		this.comment = message;

		if (element != null && element.getAttribute(Xml.KEY_NOTIZ_PREFIX + "0") != null) {
			StringTokenizer st = new StringTokenizer(message, "\n");

			int tokens = st.countTokens();
			for (int i = 0; i < tokens; i++) {
				element.setAttribute(Xml.KEY_NOTIZ_PREFIX + i, st.nextToken());
			}
			for (int i = tokens; i <= 11; i++) {
				element.setAttribute(Xml.KEY_NOTIZ_PREFIX + i, "");
			}
		}
	}

	public String getComment() {
		return comment;
	}

	public long getTime() {
		return time;
	}

	public EventCategory getCategory() {
		return category;
	}

	public void setCategory(EventCategory category) {
		this.category = category;
	}

	public String getAudioPath() {
		return audioPath;
	}

	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		// do not return json for events with an element they are stored in the
		// xml data
		if (element != null)
			return null;

		JSONObject out = new JSONObject();

		out.put(FIELD_NAME, name);
		out.put(FIELD_COMMENT, comment);
		out.put(FIELD_CATEGORY, category.name());
		out.put(FIELD_AUDIO_PATH, audioPath);
		out.put(FIELD_TIME, time);

		return out;
	}

}
