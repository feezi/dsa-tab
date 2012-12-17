package com.dsatab.data.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.dsatab.R;
import com.dsatab.activity.MainActivity;
import com.dsatab.activity.ModificatorEditActivity;
import com.dsatab.common.Util;
import com.dsatab.data.CustomModificator;
import com.dsatab.data.modifier.AbstractModificator;
import com.dsatab.data.modifier.Modificator;

public class FightModificatorAdapter extends OpenArrayAdapter<Modificator> implements OnClickListener {

	private LayoutInflater inflater;

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;

	public static String HEADER_TAG = "header";

	private Activity activity;

	public FightModificatorAdapter(Activity context, List<Modificator> objects) {
		super(context, 0, objects);
		this.activity = context;
		init();
	}

	public FightModificatorAdapter(Activity context) {
		super(context, 0);
		this.activity = context;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		return position != 0;
	}

	/**
	 * 
	 */
	private void init() {
		add(null);
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		activity.startActivityForResult(new Intent(activity, ModificatorEditActivity.class),
				MainActivity.ACTION_ADD_MODIFICATOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.data.adapter.OpenArrayAdapter#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		add(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return TYPE_HEADER;
		else
			return TYPE_ITEM;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {

			View titleLayout;
			if (!(convertView instanceof LinearLayout)) {
				// We need the layoutinflater to pick up the view from xml
				// Pick up the TwoLineListItem defined in the xml file
				titleLayout = (LinearLayout) inflater.inflate(R.layout.fight_sheet_modifier_title, parent, false);
			} else {
				titleLayout = (LinearLayout) convertView;
			}
			titleLayout.findViewById(R.id.fight_modifiers_add).setOnClickListener(this);

			titleLayout.setTag(HEADER_TAG);
			return titleLayout;
		} else {

			ViewHolder holder;

			TwoLineListItem itemLayout;

			if (!(convertView instanceof TwoLineListItem)) {
				// We need the layoutinflater to pick up the view from xml
				// Pick up the TwoLineListItem defined in the xml file
				itemLayout = (TwoLineListItem) inflater.inflate(R.layout.fight_sheet_modifier, parent, false);

				holder = new ViewHolder();
				holder.text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
				holder.text2 = (TextView) itemLayout.findViewById(android.R.id.text2);
				holder.icon1 = (ImageView) itemLayout.findViewById(android.R.id.icon1);
				holder.active = (CheckBox) itemLayout.findViewById(R.id.active);

				itemLayout.setTag(holder);
			} else {
				itemLayout = (TwoLineListItem) convertView;
				holder = (ViewHolder) convertView.getTag();
			}

			Modificator item = getItem(position);

			if (item instanceof AbstractModificator) {
				AbstractModificator modificator = (AbstractModificator) item;
				holder.active.setVisibility(View.VISIBLE);
				holder.active.setChecked(modificator.isActive());
				holder.active.setClickable(false);
				holder.active.setFocusable(false);
				holder.active.setTag(modificator);
			} else {
				holder.active.setVisibility(View.GONE);
			}

			if (item instanceof CustomModificator) {
				holder.icon1.setVisibility(View.INVISIBLE);
			} else {
				holder.icon1.setVisibility(View.VISIBLE);
			}

			if (item != null) {
				holder.text1.setText(item.getModificatorName());
				holder.text2.setText(item.getModificatorInfo());
			} else {
				holder.text1.setText(null);
				holder.text2.setText(null);
			}

			Util.applyRowStyle(itemLayout, position);

			return itemLayout;
		}
	}

	private static class ViewHolder {
		TextView text1, text2;
		ImageView icon1;
		CheckBox active;
	}
}
