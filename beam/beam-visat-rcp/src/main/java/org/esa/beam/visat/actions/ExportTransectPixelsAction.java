package org.esa.beam.visat.actions;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.framework.datamodel.TransectProfileData;
import org.esa.beam.framework.draw.Figure;
import org.esa.beam.framework.ui.SelectExportMethodDialog;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.framework.ui.command.ExecCommand;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.util.io.FileUtils;
import org.esa.beam.visat.VisatApp;

import javax.swing.SwingWorker;
import java.awt.Dialog;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExportTransectPixelsAction extends ExecCommand {

    private static final String DLG_TITLE = "Export Transect Pixels";
    private static final String ERR_MSG_BASE = "Transect pixels cannot be exported:\n";

    /**
     * Invoked when a command action is performed.
     *
     * @param event the command event
     */
    @Override
    public void actionPerformed(CommandEvent event) {
        exportTransectPixels();
    }

    /**
     * Called when a command should update its state.
     * <p/>
     * <p> This method can contain some code which analyzes the underlying element and makes a decision whether
     * this item or group should be made visible/invisible or enabled/disabled etc.
     *
     * @param event the command event
     */
    @Override
    public void updateState(CommandEvent event) {
        ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
        boolean enabled = view != null && view.getCurrentShapeFigure() != null;
        setEnabled(enabled);
    }

    private void exportTransectPixels() {

        // Get current VISAT view showing a product's band
        final ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
        if (view == null) {
            return;
        }
        // Get the displayed raster data node (band or tie-point grid)
        final RasterDataNode raster = view.getRaster();
        // Get the transect of the displayed raster data node
        final Figure transect = view.getCurrentShapeFigure();
        if (transect == null) {
            VisatApp.getApp().showErrorDialog(DLG_TITLE,
                                              ERR_MSG_BASE + "There is no transect defined in the selected band.");  /*I18N*/
            return;
        }

        final TransectProfileData transectProfileData;
        try {
            transectProfileData = TransectProfileData.create(raster, transect.getShape());
        } catch (IOException e) {
            VisatApp.getApp().showErrorDialog(DLG_TITLE,
                                              ERR_MSG_BASE + "An I/O error occured:\n" + e.getMessage());   /*I18N*/
            return;
        }

        // Compute total number of transect pixels
        final int numTransectPixels = getNumTransectPixels(raster.getProduct(), transectProfileData);

        String numPixelsText;
        if (numTransectPixels == 1) {
            numPixelsText = "One transect pixel will be exported.\n"; /*I18N*/
        } else {
            numPixelsText = numTransectPixels + " transect pixels will be exported.\n"; /*I18N*/
        }
        // Get export method from user
        final String questionText = "How do you want to export the pixel values?\n"; /*I18N*/
        final int method = SelectExportMethodDialog.run(VisatApp.getApp().getMainFrame(), getWindowTitle(),
                                                        questionText + numPixelsText, getHelpId());

        final PrintWriter out;
        final StringBuffer clipboardText;
        final int initialBufferSize = 256000;
        if (method == SelectExportMethodDialog.EXPORT_TO_CLIPBOARD) {
            // Write into string buffer
            final StringWriter stringWriter = new StringWriter(initialBufferSize);
            out = new PrintWriter(stringWriter);
            clipboardText = stringWriter.getBuffer();
        } else if (method == SelectExportMethodDialog.EXPORT_TO_FILE) {
            // Write into file, get file from user
            final File file = promptForFile(VisatApp.getApp(), createDefaultFileName(raster));
            if (file == null) {
                return; // Cancel
            }
            final FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(file);
            } catch (IOException e) {
                VisatApp.getApp().showErrorDialog(DLG_TITLE,
                                                  ERR_MSG_BASE + "Failed to create file '" + file + "':\n" + e.getMessage()); /*I18N*/
                return; // Error
            }
            out = new PrintWriter(new BufferedWriter(fileWriter, initialBufferSize));
            clipboardText = null;
        } else {
            return; // Cancel
        }

        final SwingWorker<Exception, Object> swingWorker = new SwingWorker<Exception, Object>() {

            @Override
            protected Exception doInBackground() throws Exception {
                Exception returnValue = null;
                ProgressMonitor pm = new DialogProgressMonitor(VisatApp.getApp().getMainFrame(), DLG_TITLE,
                                                               Dialog.ModalityType.APPLICATION_MODAL);
                try {
                    boolean success = exportTransectPixels(out, raster.getProduct(), transectProfileData,
                                                           numTransectPixels,
                                                           pm);
                    if (success && clipboardText != null) {
                        SystemUtils.copyToClipboard(clipboardText.toString());
                        clipboardText.setLength(0);
                    }
                } catch (Exception e) {
                    returnValue = e;
                } finally {
                    out.close();
                }
                return returnValue;
            }

            @Override
            public void done() {
                // clear status bar
                VisatApp.getApp().clearStatusBarMessage();
                // show default-cursor
                UIUtils.setRootFrameDefaultCursor(VisatApp.getApp().getMainFrame());
                // On error, show error message
                Exception exception;
                try {
                    exception = get();
                } catch (Exception e) {
                    exception = e;
                }
                if (exception != null) {
                    VisatApp.getApp().showErrorDialog(DLG_TITLE,
                                                      ERR_MSG_BASE + exception.getMessage());
                }
            }
        };

        // show wait-cursor
        UIUtils.setRootFrameWaitCursor(VisatApp.getApp().getMainFrame());
        // show message in status bar
        VisatApp.getApp().setStatusBarMessage("Exporting ROI pixels..."); /*I18N*/

        // Start separate worker thread.
        swingWorker.execute();
    }

    private static String createDefaultFileName(final RasterDataNode raster) {
        return FileUtils.getFilenameWithoutExtension(raster.getProduct().getName()) + "_TRANSECT.txt";
    }

    private static String getWindowTitle() {
        return VisatApp.getApp().getAppName() + " - " + DLG_TITLE;
    }

    /**
     * Opens a modal file chooser dialog that prompts the user to select the output file name.
     *
     * @param visatApp the VISAT application
     * @return the selected file, <code>null</code> means "Cancel"
     */
    private static File promptForFile(final VisatApp visatApp, String defaultFileName) {
        return visatApp.showFileSaveDialog(DLG_TITLE,
                                           false,
                                           null,
                                           ".txt",
                                           defaultFileName,
                                           "exportTransectPixels.lastDir");
    }

    /**
     * Writes all pixel values of the given product within the given ROI to the specified out.
     *
     * @param out     the data output writer
     * @param product the product providing the pixel values
     * @return <code>true</code> for success, <code>false</code> if export has been terminated (by user)
     */
    private static boolean exportTransectPixels(final PrintWriter out,
                                                final Product product,
                                                final TransectProfileData transectProfileData,
                                                final int numTransectPixels,
                                                ProgressMonitor pm) throws IOException {

        final Band[] bands = product.getBands();
        final GeoCoding geoCoding = product.getGeoCoding();

        writeHeaderLine(out, geoCoding, bands);
        final Point2D[] pixelPositions = transectProfileData.getPixelPositions();

        pm.beginTask("Writing pixel data...", numTransectPixels);
        try {
            for (Point2D pixelPosition : pixelPositions) {
                int x = (int) Math.floor(pixelPosition.getX());
                int y = (int) Math.floor(pixelPosition.getY());
                if (x >= 0 && x < product.getSceneRasterWidth()
                        && y >= 0 && y < product.getSceneRasterHeight()) {
                    writeDataLine(out, geoCoding, bands, x, y);
                    pm.worked(1);
                    if (pm.isCanceled()) {
                        return false;
                    }
                }
            }
        } finally {
            pm.done();
        }

        return true;
    }

    private static int getNumTransectPixels(final Product product,
                                            final TransectProfileData transectProfileData) {

        final Point2D[] pixelPositions = transectProfileData.getPixelPositions();
        int numTransectPixels = 0;
        for (Point2D pixelPosition : pixelPositions) {
            int x = (int) Math.floor(pixelPosition.getX());
            int y = (int) Math.floor(pixelPosition.getY());
            if (x >= 0 && x < product.getSceneRasterWidth()
                    && y >= 0 && y < product.getSceneRasterHeight()) {
                numTransectPixels++;
            }
        }
        return numTransectPixels;
    }

    private static void writeHeaderLine(final PrintWriter out,
                                        final GeoCoding geoCoding,
                                        final Band[] bands) {
        out.print("Pixel-X");
        out.print("\t");
        out.print("Pixel-Y");
        out.print("\t");
        if (geoCoding != null) {
            out.print("Longitude");
            out.print("\t");
            out.print("Latitude");
            out.print("\t");
        }
        for (int i = 0; i < bands.length; i++) {
            final Band band = bands[i];
            out.print(band.getName());
            if (i < bands.length - 1) {
                out.print("\t");
            }
        }
        out.print("\n");
    }

    /**
     * Writes a data line of the dataset to be exported for the given pixel position.
     *
     * @param out       the data output writer
     * @param geoCoding the product's geo-coding
     * @param bands     the array of bands that provide pixel values
     * @param x         the current pixel's X coordinate
     * @param y         the current pixel's Y coordinate
     */
    private static void writeDataLine(final PrintWriter out,
                                      final GeoCoding geoCoding,
                                      final Band[] bands,
                                      int x,
                                      int y) {
        final PixelPos pixelPos = new PixelPos(x + 0.5f, y + 0.5f);

        out.print(String.valueOf(pixelPos.x));
        out.print("\t");
        out.print(String.valueOf(pixelPos.y));
        out.print("\t");
        if (geoCoding != null) {
            final GeoPos geoPos = geoCoding.getGeoPos(pixelPos, null);
            out.print(String.valueOf(geoPos.lon));
            out.print("\t");
            out.print(String.valueOf(geoPos.lat));
            out.print("\t");
        }
        for (int i = 0; i < bands.length; i++) {
            final Band band = bands[i];
            final String pixelString = band.getPixelString(x, y);
            out.print(pixelString);
            if (i < bands.length - 1) {
                out.print("\t");
            }
        }
        out.print("\n");
    }

}