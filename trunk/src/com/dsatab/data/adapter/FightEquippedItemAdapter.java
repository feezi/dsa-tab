package com.dsatab.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.MainActivity.ProbeListener;
import com.dsatab.common.StyleableSpannableStringBuilder;
import com.dsatab.common.Util;
import com.dsatab.data.CombatProbe;
import com.dsatab.data.Hero;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.filter.EquippedItemListFilter;
import com.dsatab.data.items.Armor;
import com.dsatab.data.items.DistanceWeapon;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.items.ItemSpecification;
import com.dsatab.data.items.Shield;
import com.dsatab.data.items.Weapon;
import com.dsatab.fragment.FightFragment.TargetListener;
import com.dsatab.view.FightFilterSettings;
import com.dsatab.view.ItemListItem;

public class FightEquippedItemAdapter extends OpenArrayAdapter<EquippedItem> {

	private Hero hero;

	private EquippedItemListFilter filter;

	private ProbeListener probeListener;
	private TargetListener targetListener;

	private LayoutInflater inflater;

	public FightEquippedItemAdapter(Context context, Hero hero, FightFilterSettings settings) {
		super(context, 0);
		this.hero = hero;
		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		getFilter().setSettings(settings);
	}

	public void filter(FightFilterSettings settings) {
		getFilter().setSettings(settings);
		filter.filter((String) null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public EquippedItemListFilter getFilter() {
		if (filter == null)
			filter = new EquippedItemListFilter(this);

		return filter;
	}

	public ProbeListener getProbeListener() {
		return probeListener;
	}

	public void setProbeListener(ProbeListener probeListener) {
		this.probeListener = probeListener;
	}

	public TargetListener getTargetListener() {
		return targetListener;
	}

	public void setTargetListener(TargetListener targetListener) {
		this.targetListener = targetListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// View view = super.getView(position, convertView, parent);

		ViewHolder holder;
		ItemListItem itemLayout;
		if (!(convertView instanceof ItemListItem)) {
			itemLayout = (ItemListItem) inflater.inflate(R.layout.item_listitem, parent, false);

			holder = new ViewHolder();
			holder.text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
			holder.text2 = (TextView) itemLayout.findViewById(android.R.id.text2);
			holder.icon1 = (ImageButton) itemLayout.findViewById(android.R.id.icon1);
			holder.icon2 = (ImageButton) itemLayout.findViewById(android.R.id.icon2);
			itemLayout.setTag(holder);
		} else {
			itemLayout = (ItemListItem) convertView;
			holder = (ViewHolder) itemLayout.getTag();
		}

		EquippedItem equippedItem = getItem(position);
		Item item = equippedItem.getItem();
		ItemSpecification itemSpecification = equippedItem.getItemSpecification();

		// if (equippedItem.getSecondaryItem() != null
		// &&
		// (equippedItem.getSecondaryItem().getItem().hasSpecification(Shield.class)
		// || (equippedItem
		// .getSecondaryItem().getItem().hasSpecification(Weapon.class) &&
		// equippedItem.getHand() == Hand.rechts))) {
		//
		// } else {
		// fightItemsOdd = !fightItemsOdd;
		// }

		StyleableSpannableStringBuilder title = new StyleableSpannableStringBuilder();
		title.append(item.getTitle());

		holder.text2.setText(itemSpecification.getInfo());

		holder.icon1.setImageResource(itemSpecification.getResourceId());
		holder.icon1.setVisibility(View.VISIBLE);

		if (itemSpecification instanceof DistanceWeapon) {
			DistanceWeapon distanceWeapon = (DistanceWeapon) itemSpecification;

			holder.icon2.setImageResource(R.drawable.icon_target);
			holder.icon2.setVisibility(View.VISIBLE);

			if (equippedItem.getTalent() != null) {
				CombatProbe probe = equippedItem.getCombatProbeAttacke();
				Util.appendValue(hero, title, probe, null, getFilter().getSettings().isIncludeModifiers());
				holder.icon2.setEnabled(true);
				holder.icon1.setEnabled(true);
				holder.icon2.setTag(equippedItem);
				holder.icon2.setOnClickListener(targetListener);
				holder.icon1.setTag(probe);
				holder.icon1.setOnClickListener(probeListener);
			} else {
				holder.icon2.setEnabled(false);
				holder.icon1.setEnabled(false);
			}

			holder.text2.setText(distanceWeapon.getInfo(hero.getModifierTP(equippedItem)));
		} else if (itemSpecification instanceof Shield) {
			holder.icon1.setVisibility(View.INVISIBLE);
			holder.icon2.setImageResource(item.getResourceId());
			holder.icon2.setVisibility(View.VISIBLE);

			if (equippedItem.getTalent() != null) {
				holder.icon2.setEnabled(true);
				CombatProbe probe = equippedItem.getCombatProbeDefense();
				Util.appendValue(hero, title, probe, null, getFilter().getSettings().isIncludeModifiers());
				holder.icon2.setTag(probe);
				holder.icon2.setOnClickListener(probeListener);
			} else {
				holder.icon2.setEnabled(false);
			}
		} else if (itemSpecification instanceof Weapon) {
			Weapon weapon = (Weapon) itemSpecification;

			holder.icon2.setImageResource(R.drawable.icon_shield);
			holder.icon2.setVisibility(View.VISIBLE);
			if (equippedItem.getTalent() != null) {
				holder.icon2.setEnabled(true);
				holder.icon1.setEnabled(true);

				CombatProbe at = equippedItem.getCombatProbeAttacke();
				holder.icon1.setTag(at);
				holder.icon1.setOnClickListener(probeListener);
				CombatProbe pa = equippedItem.getCombatProbeDefense();
				holder.icon2.setTag(pa);
				holder.icon2.setOnClickListener(probeListener);

				Util.appendValue(hero, title, at, pa, getFilter().getSettings().isIncludeModifiers());
			} else {
				holder.icon2.setEnabled(false);
				holder.icon1.setEnabled(false);
			}
			if (getFilter().getSettings().isIncludeModifiers())
				holder.text2.setText(weapon.getInfo(hero.getModifiedValue(AttributeType.Körperkraft),
						hero.getModifierTP(equippedItem)));
			else
				holder.text2.setText(weapon.getInfo());
		} else if (itemSpecification instanceof Armor) {
			Armor armor = (Armor) itemSpecification;
			holder.icon2.setVisibility(View.GONE);
		}

		if (hero.getHuntingWeapon() != null && hero.getHuntingWeapon().equals(equippedItem)) {
			title.append(" (Jagdwaffe)");
		}

		holder.text1.setText(title);

		Util.applyRowStyle(itemLayout, position);

		return itemLayout;
	}

	private static class ViewHolder {
		TextView text1, text2;
		ImageView icon1, icon2;
	}

}
