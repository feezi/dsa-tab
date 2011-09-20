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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.dsatab.R;
import com.dsatab.common.Util;
import com.dsatab.data.Hero;
import com.dsatab.data.Liturgie;
import com.dsatab.data.Talent;
import com.dsatab.data.Value;
import com.dsatab.data.adapter.LiturigeAdapter;
import com.dsatab.view.LiturgieInfoDialog;
import com.dsatab.view.listener.HeroChangedListener;

/**
 * 
 * 
 */
public class LiturgieFragment extends BaseFragment implements OnItemClickListener, HeroChangedListener {

	private static final String PREF_KEY_SHOW_FAVORITE = "SHOW_FAVORITE_LITURGIE";
	private static final String PREF_KEY_SHOW_NORMAL = "SHOW_NORMAL_LITURGIE";
	private static final String PREF_KEY_SHOW_UNUSED = "SHOW_UNUSED_LITURGIE";

	private ListView liturigeList;

	private View talentView;
	private LiturigeAdapter liturigeAdapter;

	private LiturgieInfoDialog LiturgieInfo;

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
		return inflater.inflate(R.layout.sheet_liturige, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		liturigeList = (ListView) findViewById(R.id.liturgie_list);
		registerForContextMenu(liturigeList);
		liturigeList.setOnItemClickListener(this);

		talentView = findViewById(R.id.liturgie_talent);

		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * @param probe
	 */
	private void showInfo(Liturgie probe) {
		if (LiturgieInfo == null) {
			LiturgieInfo = new LiturgieInfoDialog(getBaseActivity());
		}
		LiturgieInfo.setLiturgie(probe);
		LiturgieInfo.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onHeroLoaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroLoaded(Hero hero) {
		loadHeroLiturgien(hero);

		fillLiturgieKenntnis(hero);

		if (hero.getLiturgies().isEmpty()) {
			findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
			liturigeList.setVisibility(View.GONE);
			talentView.setVisibility(View.GONE);
		} else {
			findViewById(android.R.id.empty).setVisibility(View.GONE);
			liturigeList.setVisibility(View.VISIBLE);
			talentView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 
	 */
	private void fillLiturgieKenntnis(Hero hero) {

		Talent talent = hero.getLiturgieKenntnis();

		if (talent != null) {
			talentView.setVisibility(View.VISIBLE);
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

			String be = talent.getBe();

			if (TextUtils.isEmpty(be)) {
				Util.setVisibility(text2, false, text1);
			} else {
				Util.setVisibility(text2, true, text1);
				text2.setText(be);
			}
			text3.setText(talent.getProbe());

			int modifier = hero.getModificator(talent);
			Util.setText(text4, talent.getValue(), modifier, null);

			Util.setVisibility(text5, false, text1);
			talentView.setTag(talent);
			talentView.setOnClickListener(getBaseActivity().getProbeListener());
			talentView.setOnLongClickListener(getBaseActivity().getEditListener());
		} else {
			talentView.setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.fragment.BaseFragment#onHeroUnloaded(com.dsatab.data.Hero)
	 */
	@Override
	public void onHeroUnloaded(Hero hero) {

	}

	public void onValueChanged(Value value) {
		if (value == null) {
			return;
		}

		if (value instanceof Liturgie) {
			liturigeAdapter.notifyDataSetChanged();
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
		Liturgie spell = liturigeAdapter.getItem(position);
		if (spell != null) {
			getBaseActivity().checkProbe(spell);
		}
	}

	private void loadHeroLiturgien(Hero hero2) {

		SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

		liturigeAdapter = new LiturigeAdapter(getBaseActivity(), getHero().getLiturgies(), pref.getBoolean(
				PREF_KEY_SHOW_FAVORITE, true), pref.getBoolean(PREF_KEY_SHOW_NORMAL, true), pref.getBoolean(
				PREF_KEY_SHOW_UNUSED, false));

		liturigeList.setAdapter(liturigeAdapter);
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
				inflater.inflate(R.menu.liturgie_popupmenu, menu);

				Liturgie liturgie = liturigeAdapter.getItem(position);

				menu.setHeaderTitle(liturgie.getName());
				menu.findItem(R.id.option_unmark).setVisible(liturgie.isFavorite() || liturgie.isUnused());
				menu.findItem(R.id.option_mark_favorite).setVisible(!liturgie.isFavorite());
				menu.findItem(R.id.option_mark_unused).setVisible(!liturgie.isUnused());
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

		if (item.getMenuInfo() instanceof AdapterContextMenuInfo) {
			int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

			Liturgie liturige = null;
			if (position >= 0) {
				liturige = liturigeAdapter.getItem(position);

				switch (item.getItemId()) {
				case R.id.option_mark_favorite:
					liturige.setFavorite(true);
					liturigeAdapter.refilter();
					liturigeAdapter.notifyDataSetChanged();
					return true;
				case R.id.option_mark_unused:
					liturige.setUnused(true);
					liturigeAdapter.refilter();
					liturigeAdapter.notifyDataSetChanged();
					return true;
				case R.id.option_unmark:
					liturige.setFavorite(false);
					liturige.setUnused(false);
					liturigeAdapter.refilter();
					liturigeAdapter.notifyDataSetChanged();
					return true;
				case R.id.option_view_details:
					showInfo(liturige);
					break;
				}
			}
		}

		return super.onContextItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onCreateOptionsMenu(android.view
	 * .Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.liturgie_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dsatab.activity.BaseMenuActivity#onOptionsItemSelected(android.view
	 * .MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.option_filter_liturgie) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle("Liturgien filtern");
			builder.setIcon(android.R.drawable.ic_menu_view);
			View content = LayoutInflater.from(getActivity()).inflate(R.layout.popup_filter, null);

			final CheckBox fav = (CheckBox) content.findViewById(R.id.cb_show_favorites);
			final CheckBox normal = (CheckBox) content.findViewById(R.id.cb_show_normal);
			final CheckBox unused = (CheckBox) content.findViewById(R.id.cb_show_unused);

			SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);

			fav.setChecked(pref.getBoolean(PREF_KEY_SHOW_FAVORITE, true));
			normal.setChecked(pref.getBoolean(PREF_KEY_SHOW_NORMAL, true));
			unused.setChecked(pref.getBoolean(PREF_KEY_SHOW_UNUSED, false));

			builder.setView(content);

			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {

						SharedPreferences pref = getActivity().getPreferences(Activity.MODE_PRIVATE);
						Editor edit = pref.edit();

						edit.putBoolean(PREF_KEY_SHOW_FAVORITE, fav.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_NORMAL, normal.isChecked());
						edit.putBoolean(PREF_KEY_SHOW_UNUSED, unused.isChecked());

						edit.commit();

						liturigeAdapter.filter(fav.isChecked(), normal.isChecked(), unused.isChecked());
					} else if (which == DialogInterface.BUTTON_NEUTRAL) {
						// do nothing
					}

				}
			};

			builder.setPositiveButton(R.string.label_ok, clickListener);
			builder.setNegativeButton(R.string.label_cancel, clickListener);

			builder.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

}
