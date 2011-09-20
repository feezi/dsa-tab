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
package com.dsatab.fragment;

import java.util.List;

import yuku.androidsdk.com.android.internal.view.menu.MenuBuilder;
import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dsatab.DSATabApplication;
import com.dsatab.R;
import com.dsatab.activity.BaseMainActivity;
import com.dsatab.common.Util;
import com.dsatab.data.Attribute;
import com.dsatab.data.Hero;
import com.dsatab.data.Value;
import com.dsatab.data.enums.AttributeType;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.modifier.Modificator;
import com.dsatab.view.listener.HeroChangedListener;
import com.gandulf.guilib.util.Debug;

/**
 * @author Ganymede
 * 
 */
public abstract class BaseFragment extends Fragment implements HeroChangedListener, IconContextItemSelectedListener {

	protected SharedPreferences preferences;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		Debug.verbose(getClass().getName() + " attached");
		if (activity instanceof BaseMainActivity) {
			((BaseMainActivity) activity).addFragment(this);
		}
		super.onAttach(activity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Hero hero = getHero();
		Debug.verbose(getClass().getName() + " activity created");
		if (hero != null) {
			onHeroLoaded(hero);
			onAttachListener(hero);
		}
	}

	protected void onAttachListener(Hero hero) {
		if (hero != null) {
			hero.addHeroChangedListener(this);
		}
	}

	protected void onDetachListener(Hero hero) {
		if (hero != null) {
			hero.removeHeroChangedListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Debug.verbose(getClass().getName() + " destroyView");
		if (getActivity() instanceof BaseMainActivity) {
			((BaseMainActivity) getActivity()).removeFragment(this);
		}

		Hero hero = getHero();
		if (hero != null) {
			onDetachListener(hero);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		Debug.verbose(getClass().getName() + " detached");

		if (getActivity() instanceof BaseMainActivity) {
			((BaseMainActivity) getActivity()).removeFragment(this);
		}

		Hero hero = getHero();
		if (hero != null) {
			onDetachListener(hero);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Debug.verbose(getClass().getName() + " createView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.verbose(getClass().getName() + " create");
		preferences = DSATabApplication.getPreferences();
	}

	public Hero getHero() {
		return DSATabApplication.getInstance().getHero();
	}

	public final void loadHero(Hero hero) {
		onHeroLoaded(hero);
		onAttachListener(hero);
	}

	public final void unloadHero(Hero hero) {
		onDetachListener(hero);
		onHeroUnloaded(hero);
	}

	public abstract void onHeroLoaded(Hero hero);

	public abstract void onHeroUnloaded(Hero hero);

	protected BaseMainActivity getBaseActivity() {
		if (getActivity() instanceof BaseMainActivity)
			return (BaseMainActivity) getActivity();
		else
			return null;
	}

	protected void fillAttributesList(View view) {

		fillAttributeValue((TextView) view.findViewById(R.id.attr_mu), AttributeType.Mut);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_kl), AttributeType.Klugheit);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_in), AttributeType.Intuition);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ch), AttributeType.Charisma);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ff), AttributeType.Fingerfertigkeit);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ge), AttributeType.Gewandtheit);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_ko), AttributeType.Konstitution);
		fillAttributeValue((TextView) view.findViewById(R.id.attr_kk), AttributeType.Körperkraft);

		fillAttributeLabel((TextView) view.findViewById(R.id.attr_mu_label), AttributeType.Mut);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_kl_label), AttributeType.Klugheit);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_in_label), AttributeType.Intuition);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ch_label), AttributeType.Charisma);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ff_label), AttributeType.Fingerfertigkeit);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ge_label), AttributeType.Gewandtheit);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_ko_label), AttributeType.Konstitution);
		fillAttributeLabel((TextView) view.findViewById(R.id.attr_kk_label), AttributeType.Körperkraft);

	}

	protected void fillAttributeLabel(TextView tv, AttributeType type) {

		if (!tv.isLongClickable()) {
			if (type == AttributeType.Lebensenergie || type == AttributeType.Karmaenergie
					|| type == AttributeType.Astralenergie || type == AttributeType.Ausdauer
					|| type == AttributeType.Behinderung) {
				tv.setOnClickListener(getBaseActivity().getEditListener());
			} else if (type.probable()) {
				tv.setOnClickListener(getBaseActivity().getProbeListener());
			}
			tv.setOnLongClickListener(getBaseActivity().getEditListener());
		}
		if (getHero() != null) {
			tv.setTag(getHero().getAttribute(type));
		}
	}

	protected void fillAttribute(View view, Attribute attr) {
		switch (attr.getType()) {
		case Mut:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_mu), AttributeType.Mut);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_mu_label), AttributeType.Mut);
			break;
		case Klugheit:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_kl), AttributeType.Klugheit);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_kl_label), AttributeType.Klugheit);
			break;
		case Intuition:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_in), AttributeType.Intuition);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_in_label), AttributeType.Intuition);
			break;
		case Charisma:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ch), AttributeType.Charisma);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ch_label), AttributeType.Charisma);
			break;
		case Fingerfertigkeit:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ff), AttributeType.Fingerfertigkeit);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ff_label), AttributeType.Fingerfertigkeit);
			break;
		case Gewandtheit:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ge), AttributeType.Gewandtheit);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ge_label), AttributeType.Gewandtheit);
			break;
		case Konstitution:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_ko), AttributeType.Konstitution);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_ko_label), AttributeType.Konstitution);
			break;
		case Körperkraft:
			fillAttributeValue((TextView) view.findViewById(R.id.attr_kk), AttributeType.Körperkraft);
			fillAttributeLabel((TextView) view.findViewById(R.id.attr_kk_label), AttributeType.Körperkraft);
			break;
		}
	}

	protected void fillAttributeValue(TextView tv, AttributeType type) {
		fillAttributeValue(tv, type, null);
	}

	protected void fillAttributeValue(TextView tv, AttributeType type, String prefix) {
		if (getHero() == null)
			return;
		Attribute attribute = getHero().getAttribute(type);

		if (attribute != null) {

			int modifier = getHero().getModificator(type);
			Util.setText(tv, attribute, modifier, prefix);
			tv.setTag(attribute);

			if (!tv.isLongClickable()) {

				if (type.probable()) {
					tv.setOnClickListener(getBaseActivity().getProbeListener());
				} else if (type.editable()) {
					tv.setOnClickListener(getBaseActivity().getEditListener());
				}

				if (type.editable())
					tv.setOnLongClickListener(getBaseActivity().getEditListener());
			}
		}
	}

	protected View findViewById(int id) {
		return getView().findViewById(id);
	}

	@Override
	public void onValueChanged(Value value) {

	}

	@Override
	public void onModifierAdded(Modificator value) {

	}

	@Override
	public void onModifierRemoved(Modificator value) {

	}

	@Override
	public void onModifierChanged(Modificator value) {

	}

	@Override
	public void onModifiersChanged(List<Modificator> values) {

	}

	@Override
	public void onPortraitChanged() {

	}

	@Override
	public void onItemAdded(Item item) {

	}

	@Override
	public void onItemRemoved(Item item) {

	}

	@Override
	public void onItemChanged(EquippedItem item) {

	}

	@Override
	public void onItemEquipped(EquippedItem item) {

	}

	@Override
	public void onItemUnequipped(EquippedItem item) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.view.listener.HeroChangedListener#onActiveSetChanged(int,
	 * int)
	 */
	@Override
	public void onActiveSetChanged(int newSet, int oldSet) {

	}

	private OnLongClickListener contextMenuListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Menu menu = new MenuBuilder(getActivity());
			Object info = onCreateIconContextMenu(menu, v, null);

			if (menu != null && menu.hasVisibleItems()) {
				IconContextMenu cm = new IconContextMenu(getActivity(), menu);
				cm.setInfo(info);
				cm.setOnIconContextItemSelectedListener(BaseFragment.this);

				onPrepareIconContextMenu(cm, v);
				cm.show();
				return true;
			}

			return false;
		}
	};

	public void registerForIconContextMenu(View v) {
		v.setOnLongClickListener(contextMenuListener);
	}

	public Object onCreateIconContextMenu(Menu menu, View v, ContextMenuInfo menuInfo) {
		return null;
	}

	public void onPrepareIconContextMenu(IconContextMenu menu, View v) {
	}

	public void onIconContextItemSelected(MenuItem item, Object info) {

	}
}
