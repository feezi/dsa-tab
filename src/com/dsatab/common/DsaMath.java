package com.dsatab.common;

import android.content.SharedPreferences;

import com.dsatab.DSATabApplication;
import com.dsatab.activity.DsaPreferenceActivity;

public class DsaMath {

	public static double getProbePercentage(int[] e, int t) {

		double v = 0;

		// see Wege des Meisters page 170
		if (t <= 0) {
			v = Math.min(20, e[1] + t);
			for (int i = 2; i <= 3; i++) {
				v *= Math.min(20, e[i] + t);
			}
		} else {

			v = Math.min(20, e[1]);
			for (int i = 2; i <= 3; i++) {
				v *= Math.min(20, e[i]);
			}

			// E i=1 - 3
			for (int i = 1; i <= 3; i++) {

				int ti = Math.min(20 - e[i], t);

				// E n=1 - Ti
				for (int n = 1; n <= ti; n++) {
					v += (Math.min(20, e[(i % 3) + 1] - n) * Math.min(20, e[((i + 1) % 3) + 1] - n));
				}

			}

		}

		return v / 8000;
	}

	public static double testEigen(int e1, int taw) {
		double result;
		SharedPreferences preferences = DSATabApplication.getPreferences();

		if (preferences.getBoolean(DsaPreferenceActivity.KEY_HOUSE_RULES, false) == false) {
			result = Math.min(1.0, (e1 + taw) / 20.0);

		} else {

			e1 = e1 + taw;

			// negative values don't work and an one is always successful.
			if (e1 < 0)
				e1 = 0;

			int a = Math.min(20, e1) * Math.min(20, e1) * (20 - Math.min(20, e1));
			int d = Math.min(20, e1) * Math.min(20, e1) * Math.min(20, e1);

			result = (3 * a + d) / 8000.0;

		}

		// an 1 is always successful no matter how small the taw
		if (result < 0.05)
			result = 0.05;

		return result;
	}

	public static double testTalent(int e1, int e2, int e3, int taw) {

		int success, restTaP;
		if (taw < 0)
			return testTalent(e1 + taw, e2 + taw, e3 + taw, 0);

		success = 0;
		for (int w1 = 1; w1 <= 20; w1++) {
			for (int w2 = 1; w2 <= 20; w2++) {
				for (int w3 = 1; w3 <= 20; w3++) {
					if (meisterhaft(w1, w2, w3)) {
						success++;
					} else {
						if (patzer(w1, w2, w3)) {

						} else {
							// schauen, ob die Rest-TaP nicht unter 0 fallen
							restTaP = taw - Math.max(0, w1 - e1) - Math.max(0, w2 - e2) - Math.max(0, w3 - e3);
							if (restTaP >= 0) {
								// hat gereicht
								success++;
							}
						}
					}
				}
			}
		}
		return (1d / 8000d * (success));
	}

	private static boolean meisterhaft(int w1, int w2, int w3) {
		return (w1 == 1) && (w2 == 1) || (w2 == 1) && (w3 == 1) || (w1 == 1) && (w3 == 1);
	}

	private static boolean patzer(int w1, int w2, int w3) {
		return (w1 == 20) && (w2 == 20) || (w2 == 20) && (w3 == 20) || (w1 == 20) && (w3 == 20);
	}

}
