package spritegenerator;

import spritegenerator.colourprocessing.ColourSet;
import spritegenerator.imageprocessing.Image;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MainApp extends JFrame {
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 800;
    private static final String TITLE = "Sprite Generator";
    private static final String INPUT_PATH = "spritegenerator/Input";
    private static final String PNG_EXTENSION = ".png";
    
    private final JColorChooser colorChooser;
    private final JTextField materialNameField;
    private final JPanel previewPanel;
    private final List<String> fileList;
    private final JSpinner scaleSpinner;
    
    public MainApp() {
        setTitle(TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        colorChooser = new JColorChooser();
        materialNameField = new JTextField(20);
        previewPanel = new JPanel(new GridLayout(0, 5, 10, 10));
        fileList = new ArrayList<>();
        scaleSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        
        initUI();
        loadSpriteList();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        setupColorChooser();
        mainPanel.add(colorChooser, BorderLayout.WEST);
        
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        JScrollPane scrollPane = new JScrollPane(previewPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }

    private void setupColorChooser() {
        for (AbstractColorChooserPanel panel : colorChooser.getChooserPanels()) {
            if (!panel.getDisplayName().equals("RGB")) {
                colorChooser.removeChooserPanel(panel);
            }
        }
        
        colorChooser.getSelectionModel().addChangeListener(e -> {
            Color newColor = colorChooser.getColor();
            if (newColor.getAlpha() != 255) {
                colorChooser.setColor(new Color(newColor.getRGB()));
            }
            updatePreview();
        });
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout());
        
        inputPanel.add(new JLabel("Material Name:"));
        inputPanel.add(materialNameField);
        
        JButton generateButton = new JButton("Generate Sprites");
        generateButton.addActionListener(e -> generateSprites());
        inputPanel.add(generateButton);
        
        inputPanel.add(new JLabel("Preview Scale Factor:"));
        scaleSpinner.addChangeListener(e -> updatePreview());
        inputPanel.add(scaleSpinner);
        
        return inputPanel;
    }

    private void loadSpriteList() {
        try (JarFile jar = new JarFile(getJarPath())) {
            jar.stream()
               .map(JarEntry::getName)
               .filter(name -> name.startsWith(INPUT_PATH) && name.endsWith(PNG_EXTENSION))
               .forEach(name -> {
                   fileList.add(name);
                   addPreviewImage(name);
               });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getJarPath() {
        return new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    private void updatePreview() {
        previewPanel.removeAll();
        fileList.forEach(this::addPreviewImage);
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void addPreviewImage(String spritePath) {
        try (InputStream inputStream = getResourceAsStream(spritePath)) {
            if (inputStream != null) {
                Image image = new Image(inputStream);
                image.createImage(getColorSet());
                
                BufferedImage scaledImage = createScaledImage(image);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                previewPanel.add(imageLabel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getResourceAsStream(String path) {
        return MainApp.class.getClassLoader().getResourceAsStream(path);
    }

    private BufferedImage createScaledImage(Image image) {
        int scaleFactor = (int) scaleSpinner.getValue();
        int width = image.getWidth() * scaleFactor;
        int height = image.getHeight() * scaleFactor;
        
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(image.getBufferedImage(), 0, 0, width, height, null);
        g2d.dispose();
        
        return scaledImage;
    }

    private ColourSet getColorSet() {
        Color selectedColor = colorChooser.getColor();
        return new ColourSet(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue());
    }

    private void generateSprites() {
        String materialName = materialNameField.getText().toLowerCase();
        if (colorChooser.getColor() == null || materialName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a color and enter a material name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Path outputPath = Paths.get(materialName);
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create output directory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        fileList.forEach(file -> generateSprite(file, materialName, outputPath));

        JOptionPane.showMessageDialog(this, "Sprites generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateSprite(String file, String materialName, Path outputPath) {
        try (InputStream inputStream = getResourceAsStream(file)) {
            if (inputStream != null) {
                String fileName = Paths.get(file).getFileName().toString();
                String newFileName = fileName.replace("iron", materialName);
                
                Image image = new Image(inputStream);
                image.createImage(getColorSet());
                image.writeToNewFile(outputPath.resolve(newFileName).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate sprite: " + file, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }
}
