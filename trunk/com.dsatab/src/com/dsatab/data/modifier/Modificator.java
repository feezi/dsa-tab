package com.dsatab.data.modifier;

import com.dsatab.data.Modifier;
import com.dsatab.data.Probe;
import com.dsatab.data.enums.AttributeType;

public interface Modificator {

	public Modifier getModifier(Probe type);

	public Modifier getModifier(AttributeType type);

	public String getModificatorName();

	public String getModificatorInfo();

	public boolean affects(Probe probe);

}
