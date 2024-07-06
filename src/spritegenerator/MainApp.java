package spritegenerator;

import spritegenerator.colourprocessing.ColourSet;
import spritegenerator.imageprocessing.Image;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MainApp extends JFrame {
    private JColorChooser colorChooser;
    private JTextField materialNameField;
    private JButton generateButton;
    private JComboBox<String> spriteComboBox;
    private JLabel previewLabel;
    private List<String> fileList;

    public MainApp() {
        setTitle("Sprite Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadSpriteList();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());
    
        colorChooser = new JColorChooser();
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : panels) {
            if (!accp.getDisplayName().equals("RGB")) {
                colorChooser.removeChooserPanel(accp);
            }
        }
    
        // Remove the alpha channel from the color selection model
        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Color newColor = colorChooser.getColor();
                if (newColor.getAlpha() != 255) {
                    colorChooser.setColor(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
                }
                updatePreview();
            }
        });
    
        panel.add(colorChooser, BorderLayout.WEST);
    
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Material Name:"));
        materialNameField = new JTextField(20);
        inputPanel.add(materialNameField);
    
        generateButton = new JButton("Generate Sprites");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSprites();
            }
        });
        inputPanel.add(generateButton);
    
        panel.add(inputPanel, BorderLayout.SOUTH);
    
        spriteComboBox = new JComboBox<>();
        spriteComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePreview();
            }
        });
        JScrollPane listScrollPane = new JScrollPane(spriteComboBox);
        panel.add(listScrollPane, BorderLayout.CENTER);
    
        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(256, 256));
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(previewLabel, BorderLayout.NORTH);
    
        add(panel);
    }

    private void loadSpriteList() {
        fileList = new ArrayList<>();
        final String path = "spritegenerator/Input";
        try {
            final JarFile jar = new JarFile(new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && name.endsWith("png")) {
                    fileList.add(name);
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String file : fileList) {
            spriteComboBox.addItem(file);
        }
    }

    private ColourSet getColorSet()
    {
        Color selectedColor = colorChooser.getColor();
        ColourSet colourSet = new ColourSet(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
    
        // Colours that will not change between all sprites
        colourSet.resetColor(new Color(73, 54, 21));
        colourSet.resetColor(new Color(104, 78, 30));
        colourSet.resetColor(new Color(137, 103, 39));
        colourSet.resetColor(new Color(40, 30, 11));
        colourSet.resetColor(new Color(116, 116, 117));
        colourSet.resetColor(new Color(143, 143, 144));
        colourSet.resetColor(new Color(127, 127, 128));
        colourSet.resetColor(new Color(104, 104, 105));
        return colourSet;
    }

    private void updatePreview() {
    
    
        String selectedSprite = (String) spriteComboBox.getSelectedItem();
        if (selectedSprite != null) {
            ClassLoader cl = MainApp.class.getClassLoader();
            InputStream inputStream = cl.getResourceAsStream(selectedSprite);
    
            if (inputStream != null) {
                Image image = new Image(16, 16, inputStream);
                image.createImage(getColorSet());
    
                // Assuming Image class has a method to get the image as a BufferedImage
                BufferedImage previewImage = image.getBufferedImage();
    
                // Scale the image to a larger size for better visibility
                int scaleFactor = 5; // Increase this value to make the image larger
                BufferedImage scaledImage = new BufferedImage(16 * scaleFactor, 16 * scaleFactor, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.drawImage(previewImage, 0, 0, 16 * scaleFactor, 16 * scaleFactor, null);
                g2d.dispose();
    
                ImageIcon icon = new ImageIcon(scaledImage);
                previewLabel.setIcon(icon);
            }
        }
    }

    private void generateSprites() {
        Color selectedColor = colorChooser.getColor();
        String materialName = materialNameField.getText();
    
        if (selectedColor == null || materialName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a color and enter a material name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String outputDir = materialName;
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            try {
                Files.createDirectories(outputPath);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to create output directory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    
        for (String file : fileList) {
            ClassLoader cl = MainApp.class.getClassLoader();
            InputStream inputStream = cl.getResourceAsStream(file);
    
            if (inputStream == null) {
                JOptionPane.showMessageDialog(this, "Failed to load sprite file: " + file, "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
    
            String itemType = splitStringAtUnderscore(file)[1];
            Image image = new Image(16, 16, inputStream);
            image.createImage(getColorSet());
            image.writeToNewFile(outputDir + "/" + materialName + "_" + itemType + ".png");
        }
    
        JOptionPane.showMessageDialog(this, "Sprites generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    static private String[] splitStringAtUnderscore(String string) {
        return string.split("_");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainApp ui = new MainApp();
                ui.setVisible(true);
            }
        });
    }
}