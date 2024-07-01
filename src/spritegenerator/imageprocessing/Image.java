package spritegenerator.imageprocessing;

import spritegenerator.colourprocessing.ColourSet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Image {
    private final int height;
    private final int width;
    private BufferedImage image;

    public Image(int height, int width, InputStream stream) {
        this.height = height;
        this.width = width;
        readFromFile(stream);
    }

    private void readFromFile(InputStream stream) {
        BufferedImage img = null;
        try {
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            img = ImageIO.read(stream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        image = img;
    }

    public void writeToNewFile(String path) {
        try {
            File file = new File(path);

            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getBufferedImage()
    {
        return image;
    }

    public void drawPixel(int x, int y, Color color) {
        Graphics graphics = image.getGraphics();

        graphics.setColor(color);

        graphics.drawLine(x, y, x, y);
    }

    public void createImage(ColourSet colour) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                Color pixel = new Color(image.getRGB(i, j));
                int newR = 0, newG = 0, newB = 0;

                if (pixel.getRed() != 0 && pixel.getGreen() != 0 && pixel.getBlue() != 0) {
                    for (int k = 0; k < 255; k++) {
                        if (pixel.getRed() == k + 1) newR = colour.customColoursR[k];
                        if (pixel.getGreen() == k + 1) newG = colour.customColoursG[k];
                        if (pixel.getBlue() == k + 1) newB = colour.customColoursB[k];
                    }

                    Color newColour = new Color(newR, newG, newB);

                    drawPixel(i, j, newColour);
                }
            }
        }
    }
}

