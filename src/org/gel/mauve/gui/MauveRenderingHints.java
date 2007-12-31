package org.gel.mauve.gui;

import java.awt.RenderingHints;

public class MauveRenderingHints {
	public static RenderingHints.Key KEY_SIMILARITY_DENSITY = new SimilarityDensityKey (
			42);

	private static class SimilarityDensityKey extends RenderingHints.Key {
		protected SimilarityDensityKey (int privatekey) {
			super (privatekey);
		}

		public boolean isCompatibleValue (Object value) {
			return (value instanceof Double);
		}
	}

}
