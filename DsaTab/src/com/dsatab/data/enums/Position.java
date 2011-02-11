package com.dsatab.data.enums;

import java.util.Arrays;

public enum Position {

	// KO KS KG Hl SL OaL UaL SR OaR UaR Br Ba Be OS US
	// order must be the same as in ruestung.txt
	Head_Up("Kopf Oben"), Head_Side("Kopf seitl."), Head_Face("Gesicht"), Neck("Hals"),

	LeftShoulder("Linke Schulter"), LeftUpperArm("Linker Oberarm"), LeftArm("Linker Unterarm"),

	RightShoulder("Rechte Schulter"), RightUpperArm("Rechter Oberarm"), RightArm("Rechter Unterarm"),

	Chest("Brust"), Stomach("Bauch"), Pelvis("Becken"),

	UpperLeg("Oberschenkel"), LowerLeg("Unterschenkel"),

	// no rs at the moment

	Head("Kopf"), LeftHand("Linke Hand"), RightHand("Rechte Hand"), ;

	private String name;

	private Position(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Position[] WOUND_POSITIONS = { Position.Head, Position.Stomach, Position.LeftArm, Position.RightArm, Position.UpperLeg, Position.LowerLeg };

	public static Position[] ARMOR_POSITIONS = { Position.Head_Up, Position.Head_Side, Position.Head_Face, Position.Neck, Position.Stomach,
			Position.LeftShoulder, Position.LeftUpperArm, Position.LeftArm, Position.RightShoulder, Position.RightUpperArm, Position.RightArm, Position.Pelvis,
			Position.UpperLeg, Position.LowerLeg };
	static {
		Arrays.sort(ARMOR_POSITIONS);
	}

	public static final Position[] messer_dolch_stich = new Position[21];
	static {
		messer_dolch_stich[1] = Position.UpperLeg;
		messer_dolch_stich[2] = Position.UpperLeg;
		messer_dolch_stich[3] = Position.RightShoulder;
		messer_dolch_stich[4] = Position.RightArm;
		messer_dolch_stich[5] = Position.RightArm;
		messer_dolch_stich[6] = Position.RightHand;
		messer_dolch_stich[7] = Position.RightHand;
		messer_dolch_stich[8] = Position.RightUpperArm;
		messer_dolch_stich[9] = Position.LeftArm;
		messer_dolch_stich[10] = Position.LeftArm;
		messer_dolch_stich[11] = Position.LeftHand;
		messer_dolch_stich[12] = Position.Stomach;
		messer_dolch_stich[13] = Position.Stomach;
		messer_dolch_stich[14] = Position.Chest;
		messer_dolch_stich[15] = Position.Chest;
		messer_dolch_stich[16] = Position.Pelvis;
		messer_dolch_stich[17] = Position.LowerLeg;
		messer_dolch_stich[18] = Position.Head_Side;
		messer_dolch_stich[19] = Position.Head_Face;
		messer_dolch_stich[20] = Position.Head_Face;
	}

	public static final Position[] hieb_ketten = new Position[21];
	static {
		hieb_ketten[1] = Position.Pelvis;
		hieb_ketten[2] = Position.UpperLeg;
		hieb_ketten[3] = Position.RightHand;
		hieb_ketten[4] = Position.RightArm;
		hieb_ketten[5] = Position.RightArm;
		hieb_ketten[6] = Position.LeftArm;
		hieb_ketten[7] = Position.LeftArm;
		hieb_ketten[8] = Position.RightUpperArm;
		hieb_ketten[9] = Position.RightUpperArm;
		hieb_ketten[10] = Position.LeftUpperArm;
		hieb_ketten[11] = Position.LeftUpperArm;
		hieb_ketten[12] = Position.Chest;
		hieb_ketten[13] = Position.LeftShoulder;
		hieb_ketten[14] = Position.RightShoulder;
		hieb_ketten[15] = Position.RightShoulder;
		hieb_ketten[16] = Position.Stomach;
		hieb_ketten[17] = Position.Head_Up;
		hieb_ketten[18] = Position.Head_Up;
		hieb_ketten[19] = Position.LowerLeg;
		hieb_ketten[20] = Position.Head_Side;
	}

	public static final Position[] schwert_saebel = new Position[21];
	static {
		schwert_saebel[1] = Position.RightHand;
		schwert_saebel[2] = Position.RightUpperArm;
		schwert_saebel[3] = Position.RightArm;
		schwert_saebel[4] = Position.RightArm;
		schwert_saebel[5] = Position.UpperLeg;
		schwert_saebel[6] = Position.UpperLeg;
		schwert_saebel[7] = Position.Pelvis;
		schwert_saebel[8] = Position.Pelvis;
		schwert_saebel[9] = Position.Stomach;
		schwert_saebel[10] = Position.Stomach;
		schwert_saebel[11] = Position.LowerLeg;
		schwert_saebel[12] = Position.LeftShoulder;
		schwert_saebel[13] = Position.RightShoulder;
		schwert_saebel[14] = Position.LeftUpperArm;
		schwert_saebel[15] = Position.LeftArm;
		schwert_saebel[16] = Position.Chest;
		schwert_saebel[17] = Position.Chest;
		schwert_saebel[18] = Position.Head_Face;
		schwert_saebel[19] = Position.Head_Up;
		schwert_saebel[20] = Position.Head_Side;
	}

	public static final Position[] stangen_zweih_stich = new Position[21];
	static {
		stangen_zweih_stich[1] = Position.LeftArm;
		stangen_zweih_stich[2] = Position.LeftUpperArm;
		stangen_zweih_stich[3] = Position.LeftUpperArm;
		stangen_zweih_stich[4] = Position.RightArm;
		stangen_zweih_stich[5] = Position.RightUpperArm;
		stangen_zweih_stich[6] = Position.UpperLeg;
		stangen_zweih_stich[7] = Position.UpperLeg;
		stangen_zweih_stich[8] = Position.RightShoulder;
		stangen_zweih_stich[9] = Position.LowerLeg;
		stangen_zweih_stich[10] = Position.Stomach;
		stangen_zweih_stich[11] = Position.Stomach;
		stangen_zweih_stich[12] = Position.Stomach;
		stangen_zweih_stich[13] = Position.RightHand;
		stangen_zweih_stich[14] = Position.Chest;
		stangen_zweih_stich[15] = Position.Chest;
		stangen_zweih_stich[16] = Position.Chest;
		stangen_zweih_stich[17] = Position.Neck;
		stangen_zweih_stich[18] = Position.Pelvis;
		stangen_zweih_stich[19] = Position.Head_Face;
		stangen_zweih_stich[20] = Position.Head_Face;
	}

	public static final Position[] stangen_zweih_hieb = new Position[21];
	static {
		stangen_zweih_hieb[1] = Position.UpperLeg;
		stangen_zweih_hieb[2] = Position.UpperLeg;
		stangen_zweih_hieb[3] = Position.LowerLeg;
		stangen_zweih_hieb[4] = Position.Stomach;
		stangen_zweih_hieb[5] = Position.Pelvis;
		stangen_zweih_hieb[6] = Position.RightArm;
		stangen_zweih_hieb[7] = Position.RightArm;
		stangen_zweih_hieb[8] = Position.RightUpperArm;
		stangen_zweih_hieb[9] = Position.LeftArm;
		stangen_zweih_hieb[10] = Position.RightHand;
		stangen_zweih_hieb[11] = Position.LeftUpperArm;
		stangen_zweih_hieb[12] = Position.LeftShoulder;
		stangen_zweih_hieb[13] = Position.RightShoulder;
		stangen_zweih_hieb[14] = Position.RightShoulder;
		stangen_zweih_hieb[15] = Position.Chest;
		stangen_zweih_hieb[16] = Position.Chest;
		stangen_zweih_hieb[17] = Position.Head_Up;
		stangen_zweih_hieb[18] = Position.Head_Up;
		stangen_zweih_hieb[19] = Position.Head_Side;
		stangen_zweih_hieb[20] = Position.Head_Side;
	}

	public static final Position[] box_rauf_hruru = new Position[21];
	static {
		box_rauf_hruru[1] = Position.Chest;
		box_rauf_hruru[2] = Position.Chest;
		box_rauf_hruru[3] = Position.LeftUpperArm;
		box_rauf_hruru[4] = Position.RightUpperArm;
		box_rauf_hruru[5] = Position.LeftArm;
		box_rauf_hruru[6] = Position.LeftArm;
		box_rauf_hruru[7] = Position.RightArm;
		box_rauf_hruru[8] = Position.RightArm;
		box_rauf_hruru[9] = Position.RightHand;
		box_rauf_hruru[10] = Position.Head_Face;
		box_rauf_hruru[11] = Position.Head_Face;
		box_rauf_hruru[12] = Position.Head_Face;
		box_rauf_hruru[13] = Position.Stomach;
		box_rauf_hruru[14] = Position.Stomach;
		box_rauf_hruru[15] = Position.Pelvis;
		box_rauf_hruru[16] = Position.LeftShoulder;
		box_rauf_hruru[17] = Position.RightShoulder;
		box_rauf_hruru[18] = Position.Head_Side;
		box_rauf_hruru[19] = Position.Head_Side;
		box_rauf_hruru[20] = Position.Head_Side;
	}

	public static final Position[] fern_wurf = new Position[21];
	static {
		fern_wurf[1] = Position.LeftShoulder;
		fern_wurf[2] = Position.RightShoulder;
		fern_wurf[3] = Position.Pelvis;
		fern_wurf[4] = Position.Pelvis;
		fern_wurf[5] = Position.LeftUpperArm;
		fern_wurf[6] = Position.RightUpperArm;
		fern_wurf[7] = Position.LeftArm;
		fern_wurf[8] = Position.RightArm;
		fern_wurf[9] = Position.Chest;
		fern_wurf[10] = Position.Chest;
		fern_wurf[11] = Position.Chest;
		fern_wurf[12] = Position.Neck;
		fern_wurf[13] = Position.Stomach;
		fern_wurf[14] = Position.Stomach;
		fern_wurf[15] = Position.RightHand;
		fern_wurf[16] = Position.LowerLeg;
		fern_wurf[17] = Position.UpperLeg;
		fern_wurf[18] = Position.UpperLeg;
		fern_wurf[19] = Position.Head;
		fern_wurf[20] = Position.Head;
	}
}
