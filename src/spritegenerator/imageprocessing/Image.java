package spritegenerator.imageprocessing;

import spritegenerator.colourprocessing.ColourSet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Image {
    private int height;
    private int width;
    private BufferedImage image;
    private boolean isArmorLayer;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Image(InputStream stream) {
        readFromFile(stream);
    }

    private void readFromFile(InputStream stream) {
        try {
            image = ImageIO.read(stream);
            if (image == null) {
                throw new IOException("Failed to read image from input stream");
            }
            width = image.getWidth();
            height = image.getHeight();
            isArmorLayer = (width == 64 && height == 32);

            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = newImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = newImage;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToNewFile(String path) {
        try {
            File file = new File(path);
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createImage(ColourSet colourSet) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha > 0) {
                    Color originalColor = new Color(rgb, true);
                    if (shouldTransformColor(originalColor)) {
                        int newRgb = colourSet.mapColor(rgb & 0x00FFFFFF);
                        image.setRGB(x, y, (alpha << 24) | (newRgb & 0x00FFFFFF));
                    }
                }
            }
        }
    }

    private boolean shouldTransformColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // Check if it's a shade of gray (iron color)
        boolean isGray = Math.abs(r - g) < 20 && Math.abs(g - b) < 20 && Math.abs(r - b) < 20;

        // Check if it's not a wooden color (brown)
        boolean isNotWood = !(r > g && g > b && r > 100 && g > 50 && b < 50);

        return isGray || (isNotWood && r > 100 && g > 100 && b > 100);
    }

    public BufferedImage getBufferedImage() {
        return image;
    }

    public void printImageInfo() {
        System.out.println("Image dimensions: " + width + "x" + height);
        System.out.println("Color model: " + image.getColorModel().getClass().getSimpleName());
        System.out.println("Color space: " + image.getColorModel().getColorSpace().getType());
        System.out.println("Pixel size: " + image.getColorModel().getPixelSize() + " bits");
        System.out.println("Is armor layer: " + isArmorLayer);
    }
}