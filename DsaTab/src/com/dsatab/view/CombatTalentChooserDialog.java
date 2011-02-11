package com.dsatab.view;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dsatab.R;
import com.dsatab.activity.DSATabApplication;
import com.dsatab.data.CombatTalent;
import com.dsatab.data.items.Weapon;

public class CombatTalentChooserDialog extends Dialog implements AdapterView.OnItemClickListener {

	private ArrayAdapter<CombatTalent> talentAdapter = null;

	private ListView categoryList;

	private Weapon selectedItem = null;

	private List<CombatTalent> combatTalents;

	public CombatTalentChooserDialog(Context context) {
		super(context, R.style.EditDialog);

		init();
	}

	@Override
	protected void onStart() {
		super.onStart();

		talentAdapter = new ArrayAdapter<CombatTalent>(getContext(), android.R.layout.simple_spinner_item, combatTalents);
		categoryList.setAdapter(talentAdapter);
	}

	public void show(Weapon weapon, List<CombatTalent> combatTalents) {
		this.selectedItem = weapon;

		if (combatTalents == null) {
			combatTalents = DSATabApplication.getInstance().getHero().getAvailableCombatTalents(selectedItem);
		}
		this.combatTalents = combatTalents;

		if (combatTalents.size() == 1) {
			choose(combatTalents.get(0));
			return;
		} else if (combatTalents.isEmpty()) {
			Toast.makeText(getContext(), "Es wurde kein verwendbares Talent gefunden.", Toast.LENGTH_LONG).show();
			return;
		} else {
			show();
		}
	}

	private void init() {

		setTitle("Wähle ein Talent...");

		setCanceledOnTouchOutside(true);

		RelativeLayout popupcontent = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.popup_talent_chooser, null, false);
		addContentView(popupcontent, new LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, (int) (getContext().getResources()
				.getDisplayMetrics().widthPixels * 0.80)));

		categoryList = (ListView) popupcontent.findViewById(R.id.popup_item_category_list);
		categoryList.setOnItemClickListener(this);
	}

	private void choose(CombatTalent talent) {
		DSATabApplication.getInstance().getHero().addItem(getContext(), selectedItem, talent);
		dismiss();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		choose((CombatTalent) talentAdapter.getItem(position));
	}
}