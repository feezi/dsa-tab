﻿package com.dsatab.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.ArmorAttribute;
import com.dsatab.data.Value;
import com.dsatab.data.WoundAttribute;
import com.dsatab.data.enums.Position;
import com.dsatab.view.listener.ValueChangedListener;
import com.gandulf.guilib.util.Debug;

public class BodyLayout extends FrameLayout implements View.OnClickListener, ValueChangedListener {

	private static final double OFFSET_LOWER_LEG_X = 0.70;
	private static final double OFFSET_UPPER_LEG_X = 0.35;
	private static final double OFFSET_RIGHT_ARM_X = 0.83;
	private static final double OFFSET_RIGHT_UPPER_ARM_X = 0.7;
	private static final double OFFSET_LEFT_ARM_X = 0.15;
	private static final double OFFSET_LEFT_UPPER_ARM_X = 0.26;
	private static final double OFFSET_LEFT_SHOULDER_X = 0.30;
	private static final double OFFSET_STOMACH_X = 0.5;
	private static final double OFFSET_CHEST_X = 0.45;
	private static final double OFFSET_BACK_X = 0.55;
	private static final double OFFSET_HEAD_X = 0.5;
	private static final double OFFSET_HEAD_SIDE_X = 0.4;

	private static final double OFFSET_RIGHT_SHOULDER_Y = 0.18;
	private static final double OFFSET_RIGHT_UPPER_ARM_Y = 0.18;
	private static final double OFFSET_RIGHT_ARM_Y = 0.35;

	private static final double OFFSET_LEFT_SHOULDER_Y = 0.175;
	private static final double OFFSET_LEFT_UPPER_ARM_Y = 0.17;
	private static final double OFFSET_LEFT_ARM_Y = 0.35;

	private static final double OFFSET_HEAD_UP_Y = 0.005;
	private static final double OFFSET_HEAD_Y = 0.05;
	private static final double OFFSET_NECK_Y = 0.18;
	private static final double OFFSET_CHEST_Y = 0.240;
	private static final double OFFSET_BACK_Y = 0.240;
	private static final double OFFSET_STOMACH_Y = 0.37;
	private static final double OFFSET_PELVIS_Y = 0.5;
	private static final double OFFSET_UPPER_LEG_Y = 0.6;
	private static final double OFFSET_LOWER_LEG_Y = 0.7;

	private int childWidthMeasureSpec;
	private int childHeightMeasureSpec;

	private int woundSize;
	private int rsSize, rsTextSize;

	private Map<Position, TextView> armorButtons = new HashMap<Position, TextView>(Position.values().length);
	private Map<Position, ImageButton[]> woundButtons = new HashMap<Position, ImageButton[]>(Position.values().length);

	public class LayoutParams extends FrameLayout.LayoutParams {
		private Position position;
		private int row;

		public LayoutParams(int width, int height, Position position) {
			this(width, height, position, 0);
		}

		public LayoutParams(int width, int height, Position position, int row) {
			super(width, height);
			this.position = position;
			this.row = row;
		}

		public Position getPosition() {
			return position;
		}

		public void setPosition(Position position) {
			this.position = position;
		}

		public int getRow() {
			return row;
		}

		public void setRow(int row) {
			this.row = row;
		}

	}

	public BodyLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BodyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BodyLayout(Context context) {
		super(context);
		init();
	}

	public void setArmorAttributes(Map<Position, ArmorAttribute> attributes) {

		Debug.verbose("setting armorattr " + attributes.size());
		// remove old buttons if existing
		for (TextView tv : armorButtons.values()) {
			removeView(tv);
		}
		armorButtons.clear();

		// add new ones
		for (ArmorAttribute attr : attributes.values()) {

			TextView rsText = armorButtons.get(attr.getPosition());
			if (rsText == null) {
				rsText = addArmorButton(attr.getPosition());
			}
			rsText.setTag(attr);
			if (attr.getValue() != null)
				rsText.setText(Util.toString(attr.getValue()));
			else
				rsText.setText("0");
		}

		requestLayout();
	}

	public void setWoundAttributes(Map<Position, WoundAttribute> attributes) {

		for (WoundAttribute attr : attributes.values()) {

			ImageButton[] buttons = woundButtons.get(attr.getPosition());

			if (buttons == null) {
				buttons = new ImageButton[3];
				woundButtons.put(attr.getPosition(), buttons);
			}

			for (int i = 0; i < 3; i++) {
				ImageButton ib = buttons[i];

				if (ib == null) {
					ib = addWoundButton(attr);
					buttons[i] = ib;
				}

				ib.setSelected(attr.getValue() > i);

				if (ib.isSelected())
					ib.setBackgroundResource(R.drawable.icon_wound_s);
				else
					ib.setBackgroundResource(R.drawable.icon_wound_btn);
			}

		}
		requestLayout();
	}

	public void onValueChanged(Value value) {
		if (value instanceof ArmorAttribute) {
			ArmorAttribute rs = (ArmorAttribute) value;
			TextView rsText = armorButtons.get(rs.getPosition());

			if (rs.getValue() != null)
				rsText.setText(Util.toString(rs.getValue()));
			else
				rsText.setText("0");
		}
	}

	protected void init() {

		woundSize = getResources().getDimensionPixelSize(R.dimen.wound_icon_size);
		rsSize = getResources().getDimensionPixelSize(R.dimen.rs_icon_size);
		rsTextSize = getResources().getDimensionPixelSize(R.dimen.rs_icon_text_size);

		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(32, MeasureSpec.UNSPECIFIED);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(32, MeasureSpec.UNSPECIFIED);

		setBackgroundResource(R.drawable.character);
	}

	protected ImageButton addWoundButton(WoundAttribute attr) {
		ImageButton woundButton = new ImageButton(getContext());
		woundButton.setPadding(5, 5, 5, 5);
		woundButton.setTag(attr);
		woundButton.setBackgroundResource(R.drawable.icon_wound_btn);
		woundButton.setOnClickListener(this);
		woundButton.setMinimumWidth(woundSize);
		woundButton.setMinimumHeight(woundSize);

		addView(woundButton, new LayoutParams(woundSize, woundSize, attr.getPosition()));

		return woundButton;
	}

	protected TextView addArmorButton(Position pos) {
		TextView rsText = new TextView(getContext());

		rsText.setBackgroundResource(R.drawable.icon_armor_btn);
		rsText.setOnClickListener(this);
		rsText.setGravity(Gravity.CENTER);
		rsText.setTextSize(rsTextSize);
		rsText.setMinimumWidth(rsSize);
		rsText.setMinimumHeight(rsSize);
		addView(rsText, new LayoutParams(rsSize, rsSize, pos, 1));

		armorButtons.put(pos, rsText);

		return rsText;
	}

	public void onClick(View v) {
		// wounds
		if (v instanceof ImageView) {

			ImageView iv = (ImageButton) v;
			WoundAttribute attribute = (WoundAttribute) v.getTag();

			if (iv.isSelected()) {
				attribute.setValue(attribute.getValue() - 1);
			} else {
				attribute.setValue(attribute.getValue() + 1);
			}
			iv.setSelected(!iv.isSelected());

			if (iv.isSelected())
				iv.setBackgroundResource(R.drawable.icon_wound_s);
			else
				iv.setBackgroundResource(R.drawable.icon_wound_btn);
		}

		else if (v.getTag() instanceof Value) {
			Value value = (Value) v.getTag();

			InlineEditDialog inlineEditdialog = new InlineEditDialog(getContext(), null);
			inlineEditdialog.setOnValueChangedListener(this);
			inlineEditdialog.setValue(value);
			inlineEditdialog.setTitle(value.getName());
			inlineEditdialog.show();
		}

	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		int width = r - l;
		int height = b - t;
		int count = getChildCount();

		int headWidth = 0;
		int torsoWidth = 0;
		int leftArmWidth = 0;
		int rightArmWidth = 0;
		int upperLegWidth = 0;
		int lowerLegWidth = 0;

		for (int i = 0; i < count; i++) {

			View child = getChildAt(i);

			if (child.getVisibility() != GONE) {

				LayoutParams lp = (LayoutParams) child.getLayoutParams();

				if (lp.getRow() == 0) {
					switch (lp.getPosition()) {
					case Kopf:
						headWidth += child.getMeasuredWidth();
						break;
					case Bauch:
						torsoWidth += child.getMeasuredWidth();
						break;
					case LeftLowerArm:
						leftArmWidth += child.getMeasuredWidth();
						break;
					case RightLowerArm:
						rightArmWidth += child.getMeasuredWidth();
						break;
					case UpperLeg:
						upperLegWidth += child.getMeasuredWidth();
						break;
					case LowerLeg:
						lowerLegWidth += child.getMeasuredWidth();
						break;
					}
				}
			}
		}

		int headX = (int) (width * OFFSET_HEAD_X) - (headWidth / 2);
		int headY = (int) (height * OFFSET_HEAD_Y);

		int torsoX = (int) (width * OFFSET_STOMACH_X) - (torsoWidth / 2);
		int torsoY = (int) (height * OFFSET_STOMACH_Y);

		int leftArmX = (int) (width * OFFSET_LEFT_ARM_X) - (leftArmWidth / 2);
		int leftArmY = (int) (height * OFFSET_LEFT_ARM_Y);

		int rightArmX = (int) (width * OFFSET_RIGHT_ARM_X) - (rightArmWidth / 2);
		int rightArmY = (int) (height * OFFSET_RIGHT_ARM_Y);

		int upperLegX = (int) (width * OFFSET_UPPER_LEG_X) - (upperLegWidth / 2);
		int upperLegY = (int) (height * OFFSET_UPPER_LEG_Y);

		int lowerLegX = (int) (width * OFFSET_LOWER_LEG_X) - (lowerLegWidth / 2);
		int lowerLegY = (int) (height * OFFSET_LOWER_LEG_Y);

		int cl = 0, ct = 0, cr = 0, cb = 0;
		for (int i = 0; i < count; i++) {

			View child = getChildAt(i);

			if (child.getVisibility() != GONE) {
				LayoutParams lp = (LayoutParams) child.getLayoutParams();

				// wounds
				if (lp.getRow() == 0) {
					switch (lp.getPosition()) {
					case Kopf:
						cl = headX;
						cr = headX = headX + child.getMeasuredWidth();
						ct = headY;
						cb = ct + child.getMeasuredHeight();
						break;
					case Bauch:
						cl = torsoX;
						cr = torsoX = torsoX + child.getMeasuredWidth();
						ct = torsoY;
						cb = ct + child.getMeasuredHeight();
						break;
					case LeftLowerArm:
						cl = leftArmX;
						cr = leftArmX = leftArmX + child.getMeasuredWidth();
						ct = leftArmY;
						cb = ct + child.getMeasuredHeight();
						break;
					case RightLowerArm:
						cl = rightArmX;
						cr = rightArmX = rightArmX + child.getMeasuredWidth();
						ct = rightArmY;
						cb = ct + child.getMeasuredHeight();
						break;
					case UpperLeg:
						cl = upperLegX;
						cr = upperLegX = upperLegX + child.getMeasuredWidth();
						ct = upperLegY;
						cb = ct + child.getMeasuredHeight();
						break;
					case LowerLeg:
						cl = lowerLegX;
						cr = lowerLegX = lowerLegX + child.getMeasuredWidth();
						ct = lowerLegY;
						cb = ct + child.getMeasuredHeight();
						break;
					}
					// armor
				} else {
					Debug.verbose("Layout " + child + "," + lp);

					switch (lp.getPosition()) {
					case Head_Up:
						cl = (int) (width * OFFSET_HEAD_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_HEAD_UP_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case Head_Side:
						cl = (int) (width * OFFSET_HEAD_SIDE_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_HEAD_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case Kopf:
					case Head_Face:
						cl = (int) (width * OFFSET_HEAD_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_HEAD_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case Neck:
						cl = (int) (width * OFFSET_HEAD_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_NECK_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case Bauch:
						cl = (int) (width * OFFSET_STOMACH_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_STOMACH_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case Pelvis:
						cl = (int) (width * OFFSET_STOMACH_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_PELVIS_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case Brust:
						cl = (int) (width * OFFSET_CHEST_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_CHEST_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case Ruecken:
						cl = (int) (width * OFFSET_BACK_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_BACK_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case LeftShoulder:
						cl = (int) (width * OFFSET_LEFT_SHOULDER_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_LEFT_SHOULDER_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case LeftLowerArm:
						cl = (int) (width * OFFSET_LEFT_ARM_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_LEFT_ARM_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case LeftUpperArm:
						cl = (int) (width * OFFSET_LEFT_UPPER_ARM_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_LEFT_UPPER_ARM_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case RightShoulder:
						cl = (int) (width * OFFSET_RIGHT_UPPER_ARM_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_RIGHT_SHOULDER_Y));
						cb = ct + child.getMeasuredHeight();
						break;
					case RightLowerArm:
						cl = (int) (width * OFFSET_RIGHT_ARM_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_RIGHT_ARM_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case RightUpperArm:
						cl = (int) (width * OFFSET_RIGHT_UPPER_ARM_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_RIGHT_UPPER_ARM_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case LinkesBein:
					case UpperLeg:
						cl = (int) (width * OFFSET_UPPER_LEG_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_UPPER_LEG_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					case RechtesBein:
					case LowerLeg:
						cl = (int) (width * OFFSET_LOWER_LEG_X) - (child.getMeasuredWidth() / 2);
						cr = cl + child.getMeasuredWidth();
						ct = (int) (height * (OFFSET_LOWER_LEG_Y) + woundSize);
						cb = ct + child.getMeasuredHeight();
						break;
					}
				}

				child.layout(cl, ct, cr, cb);
			}

		}

	}

	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Position.Kopf);
	}

	protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {

		// if the layout params is invalid, the android will throw a runtime
		// exception.
		if (p instanceof LayoutParams) {
			return true;
		} else {
			return false;
		}

	}

}
