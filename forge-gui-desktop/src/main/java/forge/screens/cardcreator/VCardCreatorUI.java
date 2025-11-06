package forge.screens.cardcreator;

import forge.Singletons;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.FScreen;
import forge.gui.framework.IVDoc;
import forge.toolbox.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Card Creator main view - provides UI for creating custom cards.
 *
 * <br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VCardCreatorUI implements IVDoc<CCardCreatorUI> {
    SINGLETON_INSTANCE;

    private DragCell parentCell;
    private final DragTab tab = new DragTab("Card Creator");

    // Title
    private final FLabel lblTitle = new FLabel.Builder()
            .text("Card Creator").fontAlign(SwingConstants.CENTER)
            .opaque(true).fontSize(18).build();

    // Form panels
    private final JPanel pnlMain = new JPanel();
    private final FScrollPane scrMain = new FScrollPane(pnlMain, false);

    // Basic Information
    private final FTextField txtCardName = new FTextField.Builder().build();
    private final FTextField txtManaCost = new FTextField.Builder().build();
    private final FComboBox<String> cmbCardType = new FComboBox<>(new String[]{
        "Creature", "Instant", "Sorcery", "Enchantment", "Artifact",
        "Planeswalker", "Land", "Tribal"
    });
    private final FTextField txtSubtypes = new FTextField.Builder().build();
    private final FCheckBox chkLegendary = new FCheckBox("Legendary");

    // Set Information
    private final FComboBox<String> cmbRarity = new FComboBox<>(new String[]{
        "Common", "Uncommon", "Rare", "Mythic Rare", "Special"
    });
    private final FTextField txtSet = new FTextField.Builder().text("Edge of Eternities").build();

    // Card Text
    private final FTextArea txtOracleText = new FTextArea();
    private final FScrollPane scrOracleText = new FScrollPane(txtOracleText, false);
    private final FTextArea txtFlavorText = new FTextArea();
    private final FScrollPane scrFlavorText = new FScrollPane(txtFlavorText, false);
    private final FTextArea txtKeywords = new FTextArea();
    private final FScrollPane scrKeywords = new FScrollPane(txtKeywords, false);

    // Attributes (conditional)
    private final FTextField txtPower = new FTextField.Builder().build();
    private final FTextField txtToughness = new FTextField.Builder().build();
    private final FTextField txtLoyalty = new FTextField.Builder().build();

    // Image
    private final FButton btnChooseImage = new FButton("Choose Image...");
    private final FLabel lblImagePreview = new FLabel.Builder().build();
    private File selectedImageFile;

    // Advanced section
    private final FLabel lblAdvancedHeader = new FLabel.Builder()
            .text("▶ Advanced: Custom Abilities (Optional)")
            .fontSize(14).fontStyle(Font.BOLD).build();
    private final JPanel pnlWarning = new JPanel();
    private final FTextArea txtCustomScript = new FTextArea();
    private final FScrollPane scrCustomScript = new FScrollPane(txtCustomScript, false);
    private boolean advancedExpanded = false;

    // Buttons
    private final FButton btnCreate = new FButton("Create Card");
    private final FButton btnClear = new FButton("Clear Form");
    private final FButton btnCancel = new FButton("Cancel");

    VCardCreatorUI() {
        setupUI();
    }

    private void setupUI() {
        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        pnlMain.setLayout(new MigLayout("insets 10, gap 5, wrap 2"));
        pnlMain.setOpaque(false);

        // Setup text areas
        txtOracleText.setRows(4);
        txtFlavorText.setRows(2);
        txtKeywords.setRows(3);
        txtCustomScript.setRows(6);

        // Setup image preview
        lblImagePreview.setPreferredSize(new Dimension(223, 310));
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Setup warning panel
        pnlWarning.setLayout(new MigLayout("insets 5, gap 5, wrap"));
        pnlWarning.setBackground(new Color(255, 245, 200));
        pnlWarning.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
        FLabel lblWarningText = new FLabel.Builder()
                .text("<html><b>⚠ WARNING:</b> Custom scripts may break your card!<br>" +
                      "Enter only if you know what you're doing!<br><br>" +
                      "📖 <a href='https://github.com/Card-Forge/forge/blob/ec814fc706f49a4c05c60d437a5cad10a59a36cf/docs/Creating-a-Custom-Card.md'>Read the Custom Card Scripting Guide</a></html>")
                .fontSize(11).build();
        pnlWarning.add(lblWarningText, "w 100%");
        pnlWarning.setVisible(false);
        scrCustomScript.setVisible(false);

        // Event handlers
        cmbCardType.addItemListener(this::onCardTypeChanged);
        lblAdvancedHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleAdvancedSection();
            }
        });
        lblAdvancedHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnChooseImage.addActionListener(e -> chooseImage());
        btnCreate.addActionListener(e -> CCardCreatorUI.SINGLETON_INSTANCE.createCard());
        btnClear.addActionListener(e -> clearForm());
        btnCancel.addActionListener(e -> Singletons.getControl().setCurrentScreen(FScreen.HOME_SCREEN));

        // Initial state
        updateConditionalFields();
    }

    private void onCardTypeChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            updateConditionalFields();
        }
    }

    private void updateConditionalFields() {
        String type = (String) cmbCardType.getSelectedItem();
        boolean isCreature = "Creature".equals(type) || "Tribal".equals(type);
        boolean isPlaneswalker = "Planeswalker".equals(type);

        txtPower.setEnabled(isCreature);
        txtToughness.setEnabled(isCreature);
        txtLoyalty.setEnabled(isPlaneswalker);

        if (!isCreature) {
            txtPower.setText("");
            txtToughness.setText("");
        }
        if (!isPlaneswalker) {
            txtLoyalty.setText("");
        }
    }

    private void toggleAdvancedSection() {
        advancedExpanded = !advancedExpanded;
        lblAdvancedHeader.setText((advancedExpanded ? "▼" : "▶") + " Advanced: Custom Abilities (Optional)");
        pnlWarning.setVisible(advancedExpanded);
        scrCustomScript.setVisible(advancedExpanded);
        if (parentCell != null) {
            parentCell.getBody().revalidate();
            parentCell.getBody().repaint();
        }
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)$");
            }
            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.png)";
            }
        });

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            try {
                ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(223, 310, Image.SCALE_SMOOTH);
                lblImagePreview.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                FOptionPane.showMessageDialog("Error loading image: " + e.getMessage(), "Error", FOptionPane.ERROR_ICON);
            }
        }
    }

    private void clearForm() {
        txtCardName.setText("");
        txtManaCost.setText("");
        cmbCardType.setSelectedIndex(0);
        txtSubtypes.setText("");
        chkLegendary.setSelected(false);
        cmbRarity.setSelectedIndex(0);
        txtSet.setText("Edge of Eternities");
        txtOracleText.setText("");
        txtFlavorText.setText("");
        txtKeywords.setText("");
        txtPower.setText("");
        txtToughness.setText("");
        txtLoyalty.setText("");
        txtCustomScript.setText("");
        lblImagePreview.setIcon(null);
        selectedImageFile = null;
        updateConditionalFields();
    }

    @Override
    public void populate() {
        JPanel body = parentCell.getBody();
        body.setLayout(new MigLayout("insets 0, gap 0, wrap"));
        body.add(lblTitle, "w 100%, h 40px!");
        body.add(scrMain, "w 100%, h 100%-40px!");

        // Populate form
        pnlMain.removeAll();

        // Basic Information
        pnlMain.add(new FLabel.Builder().text("═══ Basic Information ═══").fontSize(14).fontStyle(Font.BOLD).build(), "spanx 2, gaptop 10");
        pnlMain.add(new FLabel.Builder().text("Card Name*:").build());
        pnlMain.add(txtCardName, "w 100%, wrap");
        pnlMain.add(new FLabel.Builder().text("Mana Cost*:").build());
        pnlMain.add(txtManaCost, "w 100%, wrap");
        pnlMain.add(new FLabel.Builder().text("").fontSize(10).build());
        pnlMain.add(new FLabel.Builder().text("Example: G, 1 U, 2 R R, X U U").fontSize(10).build(), "wrap");

        pnlMain.add(new FLabel.Builder().text("Card Type*:").build());
        pnlMain.add(cmbCardType, "w 100%, wrap");
        pnlMain.add(new FLabel.Builder().text("Subtypes:").build());
        pnlMain.add(txtSubtypes, "w 100%, wrap");
        pnlMain.add(new FLabel.Builder().text("").build());
        pnlMain.add(new FLabel.Builder().text("Space-separated (e.g., Elf Druid)").fontSize(10).build(), "wrap");
        pnlMain.add(new FLabel.Builder().text("").build());
        pnlMain.add(chkLegendary, "wrap");

        // Set Information
        pnlMain.add(new FLabel.Builder().text("Rarity*:").build());
        pnlMain.add(cmbRarity, "w 100%, wrap");
        pnlMain.add(new FLabel.Builder().text("Set:").build());
        pnlMain.add(txtSet, "w 100%, wrap");

        // Card Text
        pnlMain.add(new FLabel.Builder().text("═══ Card Text ═══").fontSize(14).fontStyle(Font.BOLD).build(), "spanx 2, gaptop 15");
        pnlMain.add(new FLabel.Builder().text("Oracle Text*:").build(), "aligny top");
        pnlMain.add(scrOracleText, "w 100%, h 80px!, wrap");
        pnlMain.add(new FLabel.Builder().text("Flavor Text:").build(), "aligny top");
        pnlMain.add(scrFlavorText, "w 100%, h 50px!, wrap");
        pnlMain.add(new FLabel.Builder().text("Keywords:").build(), "aligny top");
        pnlMain.add(scrKeywords, "w 100%, h 60px!, wrap");
        pnlMain.add(new FLabel.Builder().text("").build());
        pnlMain.add(new FLabel.Builder().text("One per line: Flying, Haste, etc.").fontSize(10).build(), "wrap");

        // Attributes
        pnlMain.add(new FLabel.Builder().text("═══ Attributes ═══").fontSize(14).fontStyle(Font.BOLD).build(), "spanx 2, gaptop 15");
        pnlMain.add(new FLabel.Builder().text("Power:").build());
        pnlMain.add(txtPower, "w 60px, split 3");
        pnlMain.add(new FLabel.Builder().text("Toughness:").build(), "gapleft 20");
        pnlMain.add(txtToughness, "w 60px, wrap");
        pnlMain.add(new FLabel.Builder().text("").build());
        pnlMain.add(new FLabel.Builder().text("(Creatures only)").fontSize(10).build(), "wrap");
        pnlMain.add(new FLabel.Builder().text("Loyalty:").build());
        pnlMain.add(txtLoyalty, "w 60px, wrap");
        pnlMain.add(new FLabel.Builder().text("").build());
        pnlMain.add(new FLabel.Builder().text("(Planeswalkers only)").fontSize(10).build(), "wrap");

        // Image
        pnlMain.add(new FLabel.Builder().text("═══ Card Image ═══").fontSize(14).fontStyle(Font.BOLD).build(), "spanx 2, gaptop 15");
        pnlMain.add(new FLabel.Builder().text("Card Image:").build(), "aligny top");
        JPanel pnlImage = new JPanel(new MigLayout("insets 0, gap 5"));
        pnlImage.setOpaque(false);
        pnlImage.add(btnChooseImage, "wrap");
        pnlImage.add(new FLabel.Builder().text("Supported: .jpg, .png").fontSize(10).build(), "wrap");
        pnlImage.add(lblImagePreview, "gaptop 10");
        pnlMain.add(pnlImage, "wrap");

        // Advanced
        pnlMain.add(new FLabel.Builder().text("═══════════════════").fontSize(14).build(), "spanx 2, gaptop 15");
        pnlMain.add(lblAdvancedHeader, "spanx 2");
        pnlMain.add(pnlWarning, "spanx 2, w 100%, hidemode 3");
        pnlMain.add(new FLabel.Builder().text("Custom Script:").build(), "aligny top, hidemode 3");
        pnlMain.add(scrCustomScript, "w 100%, h 100px!, wrap, hidemode 3");
        pnlMain.add(new FLabel.Builder().text("").build());
        pnlMain.add(new FLabel.Builder().text("If empty, uses basic script").fontSize(10).build(), "wrap, hidemode 3");

        // Buttons
        pnlMain.add(new FLabel.Builder().text("").build(), "gaptop 20");
        JPanel pnlButtons = new JPanel(new MigLayout("insets 0, gap 10"));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnCreate);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnCancel);
        pnlMain.add(pnlButtons, "gaptop 20");

        body.revalidate();
        body.repaint();
    }

    @Override
    public EDocID getDocumentID() {
        return EDocID.CARD_CREATOR;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CCardCreatorUI getLayoutControl() {
        return CCardCreatorUI.SINGLETON_INSTANCE;
    }

    @Override
    public void setParentCell(DragCell cell) {
        this.parentCell = cell;
    }

    @Override
    public DragCell getParentCell() {
        return parentCell;
    }

    // Getters for controller
    public String getCardName() { return txtCardName.getText().trim(); }
    public String getManaCost() { return txtManaCost.getText().trim(); }
    public String getCardType() { return (String) cmbCardType.getSelectedItem(); }
    public String getSubtypes() { return txtSubtypes.getText().trim(); }
    public boolean isLegendary() { return chkLegendary.isSelected(); }
    public String getRarity() { return (String) cmbRarity.getSelectedItem(); }
    public String getSet() { return txtSet.getText().trim(); }
    public String getOracleText() { return txtOracleText.getText().trim(); }
    public String getFlavorText() { return txtFlavorText.getText().trim(); }
    public String getKeywords() { return txtKeywords.getText().trim(); }
    public String getPower() { return txtPower.getText().trim(); }
    public String getToughness() { return txtToughness.getText().trim(); }
    public String getLoyalty() { return txtLoyalty.getText().trim(); }
    public String getCustomScript() { return txtCustomScript.getText().trim(); }
    public File getSelectedImageFile() { return selectedImageFile; }

    public void showSuccess(String message) {
        FOptionPane.showMessageDialog(message, "Success", FOptionPane.INFORMATION_ICON);
        clearForm();
    }

    public void showError(String message) {
        FOptionPane.showMessageDialog(message, "Error", FOptionPane.ERROR_ICON);
    }
}
