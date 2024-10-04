package spritegenerator.colourprocessing;

import java.awt.*;

public class ColourSet {
    private Color baseColor;
    private float[] baseHSB;

    public ColourSet(int R, int G, int B) {
        this.baseColor = new Color(R, G, B);
        this.baseHSB = Color.RGBtoHSB(R, G, B, null);
    }

    public int mapColor(int originalColor) {
        Color color = new Color(originalColor);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        float newHue = baseHSB[0];
        float newSaturation = adjustSaturation(hsb[1]);
        float newBrightness = adjustBrightness(hsb[2]);

        return Color.HSBtoRGB(newHue, newSaturation, newBrightness) & 0xFFFFFF;
    }

    private float adjustSaturation(float originalSaturation) {
        // Preserve the base color's saturation but allow for slight variations
        float variation = (originalSaturation - 0.5f) * 0.2f;
        return Math.max(0f, Math.min(1f, baseHSB[1] + variation));
    }

    private float adjustBrightness(float originalBrightness) {
        // Allow for brightness variation while maintaining the overall tone
        float variation = (originalBrightness - 0.5f) * 0.4f;
        return Math.max(0f, Math.min(1f, baseHSB[2] + variation));
    }
}