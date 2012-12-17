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
package com.dsatab;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.dsatab.data.JSONable;
import com.dsatab.fragment.ArtFragment;
import com.dsatab.fragment.BaseFragment;
import com.dsatab.fragment.BaseListFragment;
import com.dsatab.fragment.FightFragment;
import com.dsatab.fragment.MapFragment;
import com.dsatab.view.FightFilterSettings;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.ListFilterSettings;

/**
 * @author Ganymede
 * 
 */
public class TabInfo implements Parcelable, JSONable {

	public static final int MAX_TABS_PER_PAGE = 2;

	private static final String FIELD_ACTIVITY_CLAZZ = "activityClazz";
	private static final String FIELD_TAB_RESOURCE_INDEX = "tabResourceId";
	private static final String FIELD_TAB_FLING_ENABLED = "tabFlingenabled";
	private static final String FIELD_PRIMARY_ACTIVITY_CLAZZ = "activityClazz1";
	private static final String FIELD_SECONDARY_ACTIVITY_CLAZZ = "activityClazz2";
	private static final String FIELD_DICE_SLIDER = "diceSlider";
	private static final String FIELD_FILTER_SETTINGS = "filterSettings";

	@SuppressWarnings("unchecked")
	private Class<? extends BaseFragment>[] activityClazz = new Class[MAX_TABS_PER_PAGE];

	private int tabResourceIndex;

	private boolean diceSlider = true;
	private boolean tabFlingEnabled = true;

	private transient UUID id;
	private transient int containerId;

	private FilterSettings[] filterSettings = new FilterSettings[MAX_TABS_PER_PAGE];

	private static final int indexToResourceId(int index) {
		if (index < 0 || index >= DSATabApplication.getInstance().getConfiguration().getTabIcons().size())
			index = 0;

		return DSATabApplication.getInstance().getConfiguration().getTabIcons().get(index);
	}

	private static final int resourceIdToIndex(int id) {
		int index = DSATabApplication.getInstance().getConfiguration().getTabIcons().indexOf(id);

		if (index < 0 || index >= DSATabApplication.getInstance().getConfiguration().getTabIcons().size())
			return 0;
		else
			return index;
	}

	public TabInfo(Class<? extends BaseFragment> activityClazz1, Class<? extends BaseFragment> activityClazz2,
			int tabResourceId, boolean diceSlider) {
		super();
		this.activityClazz[0] = activityClazz1;
		this.activityClazz[1] = activityClazz2;

		this.tabResourceIndex = resourceIdToIndex(tabResourceId);
		this.diceSlider = diceSlider;
		this.id = UUID.randomUUID();

		refreshAdditionalSettings();
	}

	public TabInfo(Class<? extends BaseFragment> activityClazz1, Class<? extends BaseFragment> activityClazz2,
			int tabResourceId) {
		this(activityClazz1, activityClazz2, tabResourceId, true);

	}

	public TabInfo(Class<? extends BaseFragment> activityClazz1, int tabResourceId, boolean diceSlider) {
		this(activityClazz1, null, tabResourceId, diceSlider);
	}

	public TabInfo(Class<? extends BaseFragment> activityClazz1, int tabResourceId) {
		this(activityClazz1, null, tabResourceId, true);
	}

	public TabInfo() {
		this(null, null, -1, true);
	}

	/**
	 * 
	 */
	public TabInfo(Parcel in) {
		this.activityClazz = (Class<? extends BaseFragment>[]) in.readSerializable();
		this.tabResourceIndex = in.readInt();
		this.diceSlider = in.readInt() == 0 ? false : true;
		this.id = UUID.randomUUID();
		this.tabFlingEnabled = in.readInt() == 0 ? false : true;
		this.filterSettings = (FilterSettings[]) in.readSerializable();
	}

	/**
	 * JSONObject constructor
	 * 
	 * @param in
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public TabInfo(JSONObject in) throws JSONException, ClassNotFoundException {
		tabResourceIndex = in.getInt(FIELD_TAB_RESOURCE_INDEX);
		if (in.has(FIELD_DICE_SLIDER))
			diceSlider = in.getBoolean(FIELD_DICE_SLIDER);

		// old delegate version
		if (!in.isNull(FIELD_PRIMARY_ACTIVITY_CLAZZ)) {
			String className = in.getString(FIELD_PRIMARY_ACTIVITY_CLAZZ);
			if ("com.dsatab.fragment.LiturgieFragment".equals(className)) {
				className = ArtFragment.class.getName();
			}

			activityClazz[0] = (Class<? extends BaseFragment>) Class.forName(className, true,
					BaseFragment.class.getClassLoader());
		}
		// old delegate version
		if (!in.isNull(FIELD_SECONDARY_ACTIVITY_CLAZZ)) {

			String className = in.getString(FIELD_SECONDARY_ACTIVITY_CLAZZ);
			if ("com.dsatab.fragment.LiturgieFragment".equals(className)) {
				className = ArtFragment.class.getName();
			}

			activityClazz[1] = (Class<? extends BaseFragment>) Class.forName(className, true,
					BaseFragment.class.getClassLoader());
		}

		if (!in.isNull(FIELD_ACTIVITY_CLAZZ)) {

			JSONArray jsonArray = in.getJSONArray(FIELD_ACTIVITY_CLAZZ);
			activityClazz = new Class[MAX_TABS_PER_PAGE];
			for (int i = 0; i < jsonArray.length(); i++) {
				if (!jsonArray.isNull(i)) {
					String className = jsonArray.getString(i);
					if ("com.dsatab.fragment.LiturgieFragment".equals(className)) {
						className = ArtFragment.class.getName();
					}

					activityClazz[i] = (Class<? extends BaseFragment>) Class.forName(className, true,
							BaseFragment.class.getClassLoader());
				}
			}
		}

		this.id = UUID.randomUUID();

		if (in.has(FIELD_TAB_FLING_ENABLED))
			tabFlingEnabled = in.getBoolean(FIELD_TAB_FLING_ENABLED);

		if (!in.isNull(FIELD_FILTER_SETTINGS)) {
			JSONArray jsonArray = in.getJSONArray(FIELD_FILTER_SETTINGS);
			filterSettings = new FilterSettings[MAX_TABS_PER_PAGE];

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject filterJson = jsonArray.optJSONObject(i);

				if (filterJson != null) {
					if (filterJson.has(ListFilterSettings.class.getName())) {
						filterSettings[i] = new ListFilterSettings(filterJson.getJSONObject(ListFilterSettings.class
								.getName()));
					} else if (filterJson.has(FightFilterSettings.class.getName())) {
						filterSettings[i] = new FightFilterSettings(filterJson.getJSONObject(FightFilterSettings.class
								.getName()));
					}
				}
			}
		}

		refreshAdditionalSettings();
	}

	public UUID getId() {
		return id;
	}

	public Class<? extends BaseFragment> getActivityClazz(int pos) {
		return activityClazz[pos];
	}

	public Class<? extends BaseFragment>[] getActivityClazzes() {
		return activityClazz;
	}

	public BaseFragment getFragment(int pos) throws InstantiationException, IllegalAccessException {
		BaseFragment fragment = null;
		if (activityClazz[pos] != null) {
			fragment = activityClazz[pos].newInstance();
			fragment.setTabInfo(filterSettings[pos]);
		}
		return fragment;
	}

	public void setActivityClazz(int pos, Class<? extends BaseFragment> activityClazz) {
		this.activityClazz[pos] = activityClazz;
		refreshAdditionalSettings();
	}

	public boolean isTabFlingEnabled() {
		return activityClazz[0] != MapFragment.class && activityClazz[1] != MapFragment.class;
	}

	public void setTabFlingEnabled(boolean tabFlingEnabled) {
		this.tabFlingEnabled = tabFlingEnabled;
	}

	public int getTabResourceId() {
		return indexToResourceId(tabResourceIndex);
	}

	public void setTabResourceId(int tabResourceId) {
		this.tabResourceIndex = resourceIdToIndex(tabResourceId);
	}

	public int getTabResourceIndex() {
		return tabResourceIndex;
	}

	public void setTabResourceIndex(int tabResourceIndex) {
		this.tabResourceIndex = tabResourceIndex;
	}

	public boolean isDiceSlider() {
		return diceSlider;
	}

	public void setDiceSlider(boolean diceSlider) {
		this.diceSlider = diceSlider;
	}

	public int getContainerId() {
		return containerId;
	}

	public void setContainerId(int containerId) {
		this.containerId = containerId;
	}

	public int getTabCount() {
		int count = 0;
		for (int i = 0; i < activityClazz.length; i++) {
			if (activityClazz[i] != null)
				count++;
		}

		return count;

	}

	private void refreshAdditionalSettings() {

		for (int i = 0; i < activityClazz.length; i++) {
			if (activityClazz[i] != null) {
				if (FightFragment.class.isAssignableFrom(activityClazz[i])) {
					if (!(filterSettings[i] instanceof FightFilterSettings)) {
						filterSettings[i] = new FightFilterSettings(true, true, true, true);
					}
				} else if (BaseListFragment.class.isAssignableFrom(activityClazz[i])) {
					if (!(filterSettings[i] instanceof ListFilterSettings)) {
						filterSettings[i] = new ListFilterSettings(true, true, true, true);
					}
				} else {
					filterSettings[i] = null;
				}
			}
		}
	}

	public FilterSettings getFilterSettings(BaseFragment baseFragment) {
		for (int i = 0; i < activityClazz.length; i++) {
			if (activityClazz[i] == baseFragment.getClass()) {
				return filterSettings[i];
			}
		}
		return null;
	}

	public FilterSettings[] getFilterSettings() {
		return filterSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Creator for the Parcelable
	 */
	public static final Parcelable.Creator<TabInfo> CREATOR = new Parcelable.Creator<TabInfo>() {
		public TabInfo createFromParcel(Parcel in) {
			return new TabInfo(in);
		}

		public TabInfo[] newArray(int size) {
			return new TabInfo[size];
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(activityClazz);
		dest.writeInt(tabResourceIndex);
		dest.writeInt(diceSlider ? 1 : 0);
		dest.writeInt(tabFlingEnabled ? 1 : 0);
		dest.writeSerializable(filterSettings);
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();

		if (activityClazz != null) {
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < activityClazz.length; i++) {
				if (activityClazz[i] != null) {
					jsonArray.put(i, activityClazz[i].getName());
				}
			}
			out.put(FIELD_ACTIVITY_CLAZZ, jsonArray);
		}

		out.put(FIELD_TAB_RESOURCE_INDEX, tabResourceIndex);
		out.put(FIELD_DICE_SLIDER, diceSlider);
		out.put(FIELD_TAB_FLING_ENABLED, tabFlingEnabled);
		if (filterSettings != null) {

			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < filterSettings.length; i++) {
				if (filterSettings[i] != null) {
					JSONObject json = new JSONObject();
					json.put(filterSettings[i].getClass().getName(), filterSettings[i].toJSONObject());
					jsonArray.put(i, json);
				}
			}

			out.put(FIELD_FILTER_SETTINGS, jsonArray);
		}
		return out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TabInfo :" + activityClazz;
	}

}