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

import yuku.iconcontextmenu.IconContextMenu.IconContextMenuInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.activity.MainActivity;
import com.dsatab.common.Util;
import com.dsatab.data.Art;
import com.dsatab.data.BaseCombatTalent;
import com.dsatab.data.Hero;
import com.dsatab.data.Talent;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.ArtAdapter;
import com.dsatab.view.ArtInfoDialog;
import com.dsatab.view.FilterDialog;
import com.dsatab.view.FilterSettings;
import com.dsatab.view.FilterSettings.FilterType;
import com.dsatab.view.ListFilterSettings;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * 
 * 
 */
public class ArtFragment extends BaseFragment implements OnItemClickListener, HeroChangedListener {

	private ListView liturigeList;
	private LinearLayout talentsView;

	private ArtAdapter artAdapter;

	private ArtInfoDialog liturgieInfo;

	private ListFilterSettings filterSettings;

	private View empty;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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
		View root = configureContainerView(inflater.inflate(R.layout.sheet_art, container, false));
		liturigeList = (ListView) root.findViewById(R.id.art_list);
		talentsView = (LinearLayout) root.findViewById(R.id.art_talents);
		empty = root.findViewById(android.R.id.empty);
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		registerForContextMenu(liturigeList);
		liturigeList.setOnItemClickListener(this);

		filterSettings = new ListFilterSettings(preferences.getBoolean(FilterDialog.PREF_KEY_ART_FAVORITE, true),
				preferences.getBoolean(FilterDialog.PREF_KEY_ART_NORMAL, true), preferences.getBoolean(
						FilterDialog.PREF_KEY_ART_UNUSED, false), preferences.getBoolean(
						FilterDialog.PREF_KEY_ART_MODIFIERS, true));

		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * @param probe
	 */
	private void showInfo(Art probe) {
		if (liturgieInfo == null) {
			liturgieInfo = new ArtInfoDialog(getBaseActivity());
		}
		liturgieInfo.setArt(probe);
		liturgieInfo.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		loadHeroArts(hero);

		fillLiturgieKenntnis(hero);

		if (hero.getArts().isEmpty()) {
			empty.setVisibility(View.VISIBLE);
			liturigeList.setVisibility(View.GONE);
			talentsView.setVisibility(View.GONE);
		} else {
			empty.setVisibility(View.GONE);
			liturigeList.setVisibility(View.VISIBLE);
			talentsView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 
	 */
	private void fillLiturgieKenntnis(Hero hero) {

		List<Talent> talents = hero.getArtTalents();

		// remove talentViews that are no longer needed

		LayoutInflater inflater = getActivity().getLayoutInflater();

		int count = 0;
		for (Talent talent : talents) {
			if (!filterSettings.isVisible(talent))
				continue;

			View talentView = talentsView.getChildAt(count);
			if (talentView == null) {
				talentView = inflater.inflate(R.layout.talent_list_item, talentsView, false);
				talentsView.addView(talentView);
			}
			// name
			TextView text1 = (TextView) talentView.findViewById(R.id.talent_list_item_text1);
			// be
			TextView text2 = (TextView) talentView.findViewById(R.id.talent_list_item_text2);
			// probe
			TextView text3 = (TextView) talentView.findViewById(R.id.talent_list_item_text3);
			// value / at
			TextView text4 = (TextView) talentView.findViewById(R.id.talent_list_item_text4);
			// pa
			TextView text5 = (TextView) talentView.findViewById(R.id.talent_list_item_text5);

			text1.setText(talent.getName());

			String be = talent.getProbeInfo().getBe();

			if (TextUtils.isEmpty(be)) {
				Util.setVisibility(text2, false, text1);
			} else {
				Util.setVisibility(text2, true, text1);
				text2.setText(be);
			}
			text3.setText(talent.getProbeInfo().getAttributesString());

			int modifier = hero.getModifier(talent);
			Util.setText(text4, talent.getValue(), modifier, null);

			Util.setVisibility(text5, false, text1);
			talentView.setTag(talent);
			talentView.setOnClickListener(getBaseActivity().getProbeListener());
			registerForIconContextMenu(talentView);

			Util.applyRowStyle(talent, talentView, count);

			count++;

		}

		int maxCount = talentsView.getChildCount();
		for (int i = maxCount - 1; i >= count; i--) {
			talentsView.removeViewAt(i);
		}

		if (talents.isEmpty()) {
			talentsView.setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.
	 * actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (menu.findItem(R.id.option_filter) == null) {
			com.actionbarsherlock.view.MenuItem item = menu.add(Menu.NONE, R.id.option_filter, Menu.NONE, "Filtern");
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setIcon(R.drawable.ic_menu_filter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onIconContextItemSelected(android.view
	 * .MenuItem, java.lang.Object)
	 */
	@Override
	public void onIconContextItemSelected(MenuItem item, Object info) {
		if (info instanceof Talent) {

			Talent talent = (Talent) info;
			BaseCombatTalent combatTalent = getHero().getCombatTalent(talent.getName());
			switch (item.getItemId()) {

			case R.id.option_edit_talent:
				if (combatTalent != null)
					MainActivity.showEditPopup(getActivity(), combatTalent);
				else
					MainActivity.showEditPopup(getActivity(), talent);
				break;

			case R.id.option_mark_favorite_talent:
				talent.setFavorite(true);
				fillLiturgieKenntnis(getHero());
				break;
			case R.id.option_mark_unused_talent:
				talent.setUnused(true);
				fillLiturgieKenntnis(getHero());
				break;
			case R.id.option_unmark_talent:
				talent.setFavorite(false);
				talent.setUnused(false);
				fillLiturgieKenntnis(getHero());
				break;
			}

		}
		super.onIconContextItemSelected(item, info);
	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Art) {
			artAdapter.refilter();
			artAdapter.notifyDataSetChanged();
		}

		if (value instanceof Talent) {
			fillLiturgieKenntnis(getHero());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Art spell = artAdapter.getItem(position);
		if (spell != null) {
			getBaseActivity().checkProbe(spell);
		}
	}

	private void loadHeroArts(Hero hero) {
		artAdapter = new ArtAdapter(getBaseActivity(), hero.getArts().values(), filterSettings);
		liturigeList.setAdapter(artAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onCreateIconContextMenu(android.view
	 * .Menu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public Object onCreateIconContextMenu(Menu menu, View v, IconContextMenuInfo menuInfo) {

		if (v.getTag() instanceof Talent) {
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.talent_popupmenu, menu);
			Talent talent = (Talent) v.getTag();

			if (menu instanceof ContextMenu) {
				((ContextMenu) menu).setHeaderTitle(talent.getName());
			}
			menu.findItem(R.id.option_unmark_talent).setVisible(talent.isFavorite() || talent.isUnused());
			menu.findItem(R.id.option_mark_favorite_talent).setVisible(!talent.isFavorite());
			menu.findItem(R.id.option_mark_unused_talent).setVisible(!talent.isUnused());
			menu.findItem(R.id.option_view_talent).setVisible(false);
			menuInfo.setTitle(talent.getName());
			return talent;
		}
		return super.onCreateIconContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		if (v == liturigeList) {

			int position = ((AdapterContextMenuInfo) menuInfo).position;

			if (position >= 0) {
				MenuInflater inflater = new MenuInflater(getActivity());
				inflater.inflate(R.menu.art_popupmenu, menu);

				Art liturgie = artAdapter.getItem(position);
				if (menu instanceof ContextMenu) {
					((ContextMenu) menu).setHeaderTitle(liturgie.getName());
				}
				menu.findItem(R.id.option_unmark_art).setVisible(liturgie.isFavorite() || liturgie.isUnused());
				menu.findItem(R.id.option_mark_favorite_art).setVisible(!liturgie.isFavorite());
				menu.findItem(R.id.option_mark_unused_art).setVisible(!liturgie.isUnused());
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getGroupId() == R.id.group_art && item.getMenuInfo() instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo menuInfo = ((AdapterContextMenuInfo) item.getMenuInfo());
			int position = menuInfo.position;

			View child = menuInfo.targetView;

			switch (item.getItemId()) {
			case R.id.option_mark_favorite_art: {
				Art art = getArt(child, position);
				if (art != null) {
					art.setFavorite(true);
					artAdapter.notifyDataSetChanged();
				}
				return true;
			}
			case R.id.option_mark_unused_art: {
				Art art = getArt(child, position);
				if (art != null) {
					art.setUnused(true);
					artAdapter.notifyDataSetChanged();
				}
				return true;
			}
			case R.id.option_unmark_art: {
				Art art = getArt(child, position);
				if (art != null) {
					art.setFavorite(false);
					art.setUnused(false);
					artAdapter.notifyDataSetChanged();
				}
				return true;
			}
			case R.id.option_view_art: {
				Art art = getArt(child, position);
				if (art != null) {
					showInfo(art);
				}
				return true;
			}
			}

		}

		return super.onContextItemSelected(item);
	}

	private Art getArt(View child, int position) {
		Art art = null;
		if (child != null && child.getTag() instanceof Art) {
			art = (Art) child.getTag();
		}

		if (artAdapter != null && art == null && position >= 0 && position < artAdapter.getCount()) {
			art = artAdapter.getItem(position);
		}
		return art;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dsatab.fragment.BaseFragment#onFilterChanged(com.dsatab.view.
	 * FilterSettings.FilterType, com.dsatab.view.FilterSettings)
	 */
	@Override
	public void onFilterChanged(FilterType type, FilterSettings settings) {
		if (type == FilterType.Art && settings instanceof ListFilterSettings) {
			artAdapter.filter((ListFilterSettings) settings);
		}
	}
}
