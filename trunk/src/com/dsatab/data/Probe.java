package com.dsatab.data;

public interface Probe {

	public enum ProbeType {
		ThreeOfThree, TwoOfThree, One
	};

	public ProbeType getProbeType();

	public String getName();

	public Integer getValue();

	public String getBe();

	public String getProbe();

	public Integer getProbeValue(int i);

	/**
	 * Returns the value that can be used to counter negative dice rolls
	 * (talent, spell value)
	 * 
	 * @return Integer
	 */
	public Integer getProbeBonus();

	/**
	 * Returns the probe modification positive values means the probe is more
	 * difficult, negative values simplifies the probe
	 * 
	 * @return
	 */
	public Integer getErschwernis();

}
