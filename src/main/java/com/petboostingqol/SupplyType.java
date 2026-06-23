package com.petboostingqol;

public enum SupplyType
{
	SANFEW_SERUM          ("Sanfew serum(4)",              10925),
	SUPER_RESTORE         ("Super restore(4)",              3024),
	SUPER_COMBAT          ("Super combat potion(4)",       12695),
	DIVINE_SUPER_COMBAT   ("Divine super combat potion(4)", 23685),
	ANGLERFISH            ("Anglerfish",                   13441);

	final String displayName;
	final int    itemId;

	SupplyType(String displayName, int itemId)
	{
		this.displayName = displayName;
		this.itemId      = itemId;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
