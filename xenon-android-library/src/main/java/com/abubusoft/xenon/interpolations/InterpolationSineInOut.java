package com.abubusoft.xenon.interpolations;

import com.abubusoft.xenon.math.XenonMath;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 *
 * @author Gil
 * @author Nicolas Gramlich
 * @since 16:52:11 - 26.07.2010
 */
public class InterpolationSineInOut implements Interpolation {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static InterpolationSineInOut INSTANCE;

	// ===========================================================
	// Constructors
	// ===========================================================

	private InterpolationSineInOut() {

	}

	public static InterpolationSineInOut getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new InterpolationSineInOut();
		}
		return INSTANCE;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public float getPercentage(final float pSecondsElapsed, final float pDuration) {
		final float percentage = pSecondsElapsed / pDuration;

		return (float) (-0.5f * (Math.cos(percentage * XenonMath.PI) - 1));
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
