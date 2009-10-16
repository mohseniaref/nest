package org.esa.nest.gpf;

import org.esa.nest.util.DialogUtils;
import org.esa.beam.framework.ui.AppContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

/**
 * User interface for SARSimTerrainCorrectionOp
 */
public class SARSimTerrainCorrectionOpUI extends RangeDopplerGeocodingOpUI {

    private final JTextField rmsThreshold = new JTextField("");
    private final JTextField warpPolynomialOrder = new JTextField("");
    private final JCheckBox openResidualsFileCheckBox = new JCheckBox("Show GCP Residuals");
    private boolean openResidualsFile = false;

    @Override
    public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {
        JComponent pane = super.CreateOpTab(operatorName, parameterMap, appContext);

        openResidualsFileCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    openResidualsFile = (e.getStateChange() == ItemEvent.SELECTED);
                }
        });
                
        return pane;
    }

    @Override
    public void initParameters() {
        super.initParameters();

        float threshold = (Float)paramMap.get("rmsThreshold");
        rmsThreshold.setText(String.valueOf(threshold));

        int order = (Integer)paramMap.get("warpPolynomialOrder");
        warpPolynomialOrder.setText(String.valueOf(order));

        openResidualsFile = (Boolean)paramMap.get("openResidualsFile");
        openResidualsFileCheckBox.getModel().setPressed(openResidualsFile);        
    }

    @Override
    public void updateParameters() {
        super.updateParameters();

        paramMap.put("rmsThreshold", Float.parseFloat(rmsThreshold.getText()));
        paramMap.put("warpPolynomialOrder", Integer.parseInt(warpPolynomialOrder.getText()));
        paramMap.put("openResidualsFile", openResidualsFile);        
    }

    @Override
    protected JComponent createPanel() {

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = DialogUtils.createGridBagConstraints();

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "RMS Threshold:", rmsThreshold);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "WARP Polynomial Order:", warpPolynomialOrder);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Digital Elevation Model:", demName);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, externalDEMFileLabel, externalDEMFile);
        gbc.gridx = 2;
        contentPane.add(externalDEMBrowseButton, gbc);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, externalDEMNoDataValueLabel, externalDEMNoDataValue);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "DEM Resampling Method:", demResamplingMethod);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Image Resampling Method:", imgResamplingMethod);
        gbc.gridy++;
        DialogUtils.addComponent(contentPane, gbc, "Pixel Spacing (m):", pixelSpacing);

        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(saveDEMCheckBox, gbc);
        gbc.gridy++;
        contentPane.add(saveLocalIncidenceAngleCheckBox, gbc);
        gbc.gridy++;
        contentPane.add(saveProjectedLocalIncidenceAngleCheckBox, gbc);
        gbc.gridy++;
        contentPane.add(saveSigmaNoughtCheckBox, gbc);
        gbc.gridx = 1;
        contentPane.add(incidenceAngleForSigma0, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(saveGammaNoughtCheckBox, gbc);
        gbc.gridx = 1;
        contentPane.add(incidenceAngleForGamma0, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(saveBetaNoughtCheckBox, gbc);
        gbc.gridy++;
        contentPane.add(openResidualsFileCheckBox, gbc);
        
        //DialogUtils.fillPanel(contentPane, gbc);

        return contentPane;
    }
}