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
import javax.imageio.ImageIO;

public class MainApp extends JFrame {
    private JColorChooser colorChooser;
    private JTextField materialNameField;
    private JButton generateButton;
    private JPanel previewPanel;
    private List<String> fileList;
    private JSpinner scaleSpinner;

    public MainApp() {
        setTitle("Sprite Generator");
        setSize(1000, 800);
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

        inputPanel.add(new JLabel("Preview Scale Factor:"));
        SpinnerNumberModel scaleModel = new SpinnerNumberModel(5, 1, 20, 1);
        scaleSpinner = new JSpinner(scaleModel);
        scaleSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updatePreview();
            }
        });
        inputPanel.add(scaleSpinner);

        panel.add(inputPanel, BorderLayout.SOUTH);

        previewPanel = new JPanel();
        previewPanel.setLayout(new GridLayout(0, 5, 10, 10));

        JScrollPane scrollPane = new JScrollPane(previewPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

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
            addPreviewImage(file);
        }
    }

    private void updatePreview() {
        previewPanel.removeAll();
        previewPanel.revalidate();
        previewPanel.repaint();

        for (String spritePath : fileList) {
            ClassLoader cl = MainApp.class.getClassLoader();
            InputStream inputStream = cl.getResourceAsStream(spritePath);
            //System.out.println(spritePath);
            if (inputStream != null) {
                // Reset the input stream for the Image constructor
                inputStream = cl.getResourceAsStream(spritePath);

                Image image = new Image(inputStream);
                image.createImage(getColorSet());
                int width = image.getWidth();
                int height = image.getHeight();
                BufferedImage previewImage = image.getBufferedImage();

                int scaleFactor = (int) scaleSpinner.getValue();
                BufferedImage scaledImage = new BufferedImage(width * scaleFactor, height * scaleFactor, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.drawImage(previewImage, 0, 0, width * scaleFactor, height * scaleFactor, null);
                g2d.dispose();

                ImageIcon icon = new ImageIcon(scaledImage);
                JLabel imageLabel = new JLabel(icon);
                previewPanel.add(imageLabel);
            }
        }

        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void addPreviewImage(String spritePath) {
        ClassLoader cl = MainApp.class.getClassLoader();
        InputStream inputStream = cl.getResourceAsStream(spritePath);

        if (inputStream != null) {
            // Reset the input stream for the Image constructor
            inputStream = cl.getResourceAsStream(spritePath);

            Image image = new Image(inputStream);
            System.out.println(spritePath);
            image.createImage(getColorSet());
            image.printImageInfo();
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage previewImage = image.getBufferedImage();

            int scaleFactor = 5;
            BufferedImage scaledImage = new BufferedImage(width * scaleFactor, height * scaleFactor, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(previewImage, 0, 0, width * scaleFactor, height * scaleFactor, null);
            g2d.dispose();

            ImageIcon icon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(icon);
            previewPanel.add(imageLabel);
        }
    }

    private ColourSet getColorSet() {
        Color selectedColor = colorChooser.getColor();
        ColourSet colourSet = new ColourSet(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
        return colourSet;
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

            // Reset the input stream for the Image constructor
            inputStream = cl.getResourceAsStream(file);

            String itemType = splitStringAtUnderscore(file)[1];
            Image image = new Image(inputStream);
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