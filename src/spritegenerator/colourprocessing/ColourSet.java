package spritegenerator.colourprocessing;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColourSet {
    private Color baseColor;
    private Map<Integer, Integer> colorMap = new HashMap<>();

    public ColourSet(int R, int G, int B) {
        this.baseColor = new Color(R, G, B);
        initializeColorMap();
    }

    private void initializeColorMap() {
        for (int i = 0; i < 256; i++) {
            Color grayColor = new Color(i, i, i);
            Color mappedColor = blendColors(grayColor, baseColor, 0.8f);
            colorMap.put(grayColor.getRGB(), mappedColor.getRGB());
        }
    }

    private Color blendColors(Color c1, Color c2, float ratio) {
        float iRatio = 1.0f - ratio;
        int r = (int)(c1.getRed() * iRatio + c2.getRed() * ratio);
        int g = (int)(c1.getGreen() * iRatio + c2.getGreen() * ratio);
        int b = (int)(c1.getBlue() * iRatio + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    public int mapColor(int originalColor, boolean isArmorLayer) {
        Color color = new Color(originalColor);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float[] baseHsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

        float newHue = baseHsb[0];
        float newSaturation = Math.min(1f, hsb[1] + baseHsb[1] * 0.5f);
        float newBrightness = hsb[2] * 0.8f + baseHsb[2] * 0.2f;

        return Color.HSBtoRGB(newHue, newSaturation, newBrightness) & 0xFFFFFF;
    }
}