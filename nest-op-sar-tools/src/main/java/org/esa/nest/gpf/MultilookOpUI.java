package org.esa.nest.gpf;

import org.esa.beam.framework.gpf.ui.BaseOperatorUI;
import org.esa.beam.framework.gpf.ui.UIValidation;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.GridBagUtils;
import org.esa.nest.util.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;

/**
 * User interface for GCPSelectionOp
 */
public class MultilookOpUI extends BaseOperatorUI {

    private final JList bandList = new JList();

    private final JTextField nRgLooks = new JTextField("");
    private final JTextField nAzLooks = new JTextField("");
    private final JTextField meanGRSqaurePixel = new JTextField("");

    private final JRadioButton grSquarePixel = new JRadioButton("GR Square Pixel");
    private final JRadioButton independentLooks = new JRadioButton("Independent Looks");

    @Override
    public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {

        initializeOperatorUI(operatorName, parameterMap);
        final JComponent panel = createPanel();
        initParameters();

        return new JScrollPane(panel);
    }

    @Override
    public void initParameters() {

        OperatorUIUtils.initBandList(bandList, getBandNames());

        nRgLooks.setText(String.valueOf(paramMap.get("nRgLooks")));
        
        setAzimuthLooks();
    }

    private void setAzimuthLooks() {
        if(sourceProducts != null && sourceProducts.length > 0) {
            try {
                if (grSquarePixel.isSelected()) {
                    final int nRgLooksVal = Integer.parseInt(nRgLooks.getText());
                    final MultilookOp.DerivedParams param = new MultilookOp.DerivedParams();
                    MultilookOp.getDerivedParameters(sourceProducts[0], nRgLooksVal, param);
                    final int azimuthLooks = param.nAzLooks;
                    nAzLooks.setText(String.valueOf(azimuthLooks));

                    final float meanSqaurePixel = param.meanGRSqaurePixel;
                    meanGRSqaurePixel.setText(String.valueOf(meanSqaurePixel));
                } else { // independent looks
                    meanGRSqaurePixel.setText("");
                }
            } catch (Exception e) {
                meanGRSqaurePixel.setText("");
            }
        }
    }

    @Override
    public UIValidation validateParameters() {

        return new UIValidation(UIValidation.State.OK, "");
    }

    @Override
    public void updateParameters() {

        OperatorUIUtils.updateBandList(bandList, paramMap);

        final String nRgLooksStr = nRgLooks.getText();
        final String nAzLooksStr = nAzLooks.getText();
        if(nRgLooksStr != null && !nRgLooksStr.isEmpty())
            paramMap.put("nRgLooks", Integer.parseInt(nRgLooksStr));
        if(nAzLooksStr != null && !nAzLooksStr.isEmpty())
            paramMap.put("nAzLooks", Integer.parseInt(nAzLooksStr));
    }

    private JComponent createPanel() {

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = GridBagUtils.createDefaultConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.top = 1;
        gbc.insets.bottom = 1;
        gbc.insets.right = 1;
        gbc.insets.left = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPane.add(new JLabel("Source Bands:"), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        contentPane.add(new JScrollPane(bandList), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(grSquarePixel, gbc);

        gbc.gridx = 1;
        contentPane.add(independentLooks, gbc);

        grSquarePixel.setSelected(true);
        grSquarePixel.setActionCommand("GR Square Pixel:");
        independentLooks.setActionCommand("Independent Looks:");
        ButtonGroup group = new ButtonGroup();
    	group.add(grSquarePixel);
	    group.add(independentLooks);
        RadioListener myListener = new RadioListener();
        grSquarePixel.addActionListener(myListener);
        independentLooks.addActionListener(myListener);

        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Number of Range Looks:", nRgLooks);

        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Number of Azimuth Looks:", nAzLooks);

        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Mean GR Square Pixel:", meanGRSqaurePixel);

        nAzLooks.setEditable(false);
        meanGRSqaurePixel.setEditable(false);

        nRgLooks.addFocusListener(new FocusListener() {

            public void focusGained(final FocusEvent e) {
            }
            public void focusLost(final FocusEvent e) {
                setAzimuthLooks();
            }
        });

        nRgLooks.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                setAzimuthLooks();
            }
        });

        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Note:",
                new JLabel("Currently, detection for complex data is done without resampling"));

        DialogUtils.fillPanel(contentPane, gbc);

        return contentPane;
    }

    private class RadioListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            meanGRSqaurePixel.setText("");
            meanGRSqaurePixel.setEditable(false);
            if (e.getActionCommand().contains("GR Square Pixel:")) {
                nAzLooks.setText("");
                nAzLooks.setEditable(false);
                setAzimuthLooks();
            } else { // independent looks
                nAzLooks.setEditable(true);
            }
        }
    }

}