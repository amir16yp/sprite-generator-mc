package spritegenerator.colourprocessing;

import java.awt.*;

public class ColourSet {
    public int[] customColoursR = new int[255];
    public int[] customColoursG = new int[255];
    public int[] customColoursB = new int[255];

    public ColourSet(int R, int G, int B) {

        int newR;
        int newG;
        int newB;

        for (int i = 0; i < 255; i++) {
            newR = (i + (R * 2)) / 2;
            newG = (i + (G * 2)) / 2;
            newB = (i + (B * 2)) / 2;

            newR = forceMin0AndMax255(newR);
            newG = forceMin0AndMax255(newG);
            newB = forceMin0AndMax255(newB);

            customColoursR[i] = newR;
            customColoursG[i] = newG;
            customColoursB[i] = newB;
        }

    }
    private static int forceMin0AndMax255(int val) {
        if (val > 255) val = 255;
        else if (val < 0) val = 0;
        return val;
    }

    public void resetColor(Color color) {
        this.customColoursR[color.getRed() - 1] = color.getRed();
        this.customColoursG[color.getGreen() - 1] = color.getGreen();
        this.customColoursB[color.getBlue() - 1] = color.getBlue();
    }
}
