package com.dsatab.view.listener;

import java.util.List;

import com.dsatab.data.Value;
import com.dsatab.data.items.EquippedItem;
import com.dsatab.data.items.Item;
import com.dsatab.data.modifier.Modificator;

public interface HeroChangedListener {

	void onValueChanged(Value value);

	void onModifierAdded(Modificator value);

	void onModifierRemoved(Modificator value);

	void onModifierChanged(Modificator value);

	void onModifiersChanged(List<Modificator> values);

	void onActiveSetChanged(int newSet, int oldSet);

	void onItemAdded(Item item);

	void onItemRemoved(Item item);

	void onItemChanged(EquippedItem item);

	void onItemEquipped(EquippedItem item);

	void onItemUnequipped(EquippedItem item);

	void onPortraitChanged();
}
