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

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.dsatab.fragment.BaseFragment;

/**
 * @author Ganymede
 * 
 */
public class TabInfo implements Parcelable {

	private static final String FIELD_TAB_RESOURCE_INDEX = "tabResourceId";
	private static final String FIELD_PRIMARY_ACTIVITY_CLAZZ = "activityClazz1";
	private static final String FIELD_SECONDARY_ACTIVITY_CLAZZ = "activityClazz2";
	private static final String FIELD_DICE_SLIDER = "diceSlider";

	private Class<? extends BaseFragment> primaryActivityClazz;

	private Class<? extends BaseFragment> secondaryActivityClazz;
	private int tabResourceIndex;

	private boolean diceSlider = true;

	public TabInfo(Class<? extends BaseFragment> activityClazz1, Class<? extends BaseFragment> activityClazz2,
			int tabResourceId, boolean diceSlider) {
		super();
		this.primaryActivityClazz = activityClazz1;
		this.secondaryActivityClazz = activityClazz2;

		this.tabResourceIndex = resourceIdToIndex(tabResourceId);
		this.diceSlider = diceSlider;
	}

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
			int tabResourceId) {
		this(activityClazz1, activityClazz2, tabResourceId, true);

	}

	public TabInfo(Class<? extends BaseFragment> activityClazz1, int tabResourceId, boolean diceSlider) {
		this(activityClazz1, null, tabResourceId, diceSlider);
	}

	public TabInfo(Class<? extends BaseFragment> activityClazz1, int tabResourceId) {
		this(activityClazz1, null, tabResourceId, true);
	}

	/**
	 * 
	 */
	public TabInfo(Parcel in) {
		this.primaryActivityClazz = (Class<? extends BaseFragment>) in.readSerializable();
		this.secondaryActivityClazz = (Class<? extends BaseFragment>) in.readSerializable();
		this.tabResourceIndex = in.readInt();
		this.diceSlider = in.readInt() == 0 ? false : true;
	}

	/**
	 * JSONObject constructor
	 * 
	 * @param in
	 * @throws JSONException
	 */
	public TabInfo(JSONObject in) throws JSONException, ClassNotFoundException {
		tabResourceIndex = in.getInt(FIELD_TAB_RESOURCE_INDEX);
		diceSlider = in.getBoolean(FIELD_DICE_SLIDER);

		if (!in.isNull(FIELD_PRIMARY_ACTIVITY_CLAZZ)) {
			primaryActivityClazz = (Class<? extends BaseFragment>) Class.forName(
					in.getString(FIELD_PRIMARY_ACTIVITY_CLAZZ), true, BaseFragment.class.getClassLoader());
		}

		if (!in.isNull(FIELD_SECONDARY_ACTIVITY_CLAZZ)) {
			secondaryActivityClazz = (Class<? extends BaseFragment>) Class.forName(
					in.getString(FIELD_SECONDARY_ACTIVITY_CLAZZ), true, BaseFragment.class.getClassLoader());
		}

	}

	public Class<? extends BaseFragment> getPrimaryActivityClazz() {
		return primaryActivityClazz;
	}

	public void setPrimaryActivityClazz(Class<? extends BaseFragment> activityClazz) {
		this.primaryActivityClazz = activityClazz;
	}

	public Class<? extends BaseFragment> getSecondaryActivityClazz() {
		return secondaryActivityClazz;
	}

	public void setSecondaryActivityClazz(Class<? extends BaseFragment> secondaryActivityClazz) {
		this.secondaryActivityClazz = secondaryActivityClazz;
	}

	public int getTabResourceId() {
		return indexToResourceId(tabResourceIndex);
	}

	public void setTabResourceId(int tabResourceId) {
		this.tabResourceIndex = resourceIdToIndex(tabResourceId);
	}

	public boolean isDiceSlider() {
		return diceSlider;
	}

	public void setDiceSlider(boolean diceSlider) {
		this.diceSlider = diceSlider;
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
		dest.writeSerializable(primaryActivityClazz);
		dest.writeSerializable(secondaryActivityClazz);
		dest.writeInt(tabResourceIndex);
		dest.writeInt(diceSlider ? 1 : 0);
	}

	/**
	 * Constructs a json object with the current data
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject() throws JSONException {
		JSONObject out = new JSONObject();
		if (primaryActivityClazz != null)
			out.put(FIELD_PRIMARY_ACTIVITY_CLAZZ, primaryActivityClazz.getName());
		if (secondaryActivityClazz != null)
			out.put(FIELD_SECONDARY_ACTIVITY_CLAZZ, secondaryActivityClazz.getName());

		out.put(FIELD_TAB_RESOURCE_INDEX, tabResourceIndex);
		out.put(FIELD_DICE_SLIDER, diceSlider);
		return out;
	}

}
