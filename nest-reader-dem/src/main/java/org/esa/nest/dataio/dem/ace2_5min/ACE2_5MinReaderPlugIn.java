package org.esa.nest.dataio.dem.ace2_5min;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.util.io.BeamFileFilter;
import org.esa.nest.dataio.ReaderUtils;

import java.io.File;
import java.util.Locale;


/**
 * A plugin for a product reader for ACE data.
 *
 */
public class ACE2_5MinReaderPlugIn implements ProductReaderPlugIn {

    public static final String FORMAT_NAME = "ACE2";
    public static final String[] FILE_EXTENSIONS = new String[]{ ".ACE2" };
    public static final Class[] INPUT_TYPES = new Class[]{
            String.class,
            File.class,
    };
    public static final String DESCRIPTION = "ACE2 Data Product Tile";

    public ACE2_5MinReaderPlugIn() {
    }

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     */
    public DecodeQualification getDecodeQualification(final Object input) {
        final File file = ReaderUtils.getFileFromInput(input);
        if (file != null) {
            final String fileName = file.getName().toLowerCase();
            final String[] extensions = getDefaultFileExtensions();
            for (final String extension : extensions) {
                if (fileName.endsWith(extension.toLowerCase()) && !fileName.equals(extension.toLowerCase())) {
                    return DecodeQualification.INTENDED;
                }
            }
        }
        return DecodeQualification.UNABLE;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p/>
     * <p> Intances of the classes returned in this array are valid objects for the <code>setInput</code> method of the
     * <code>ProductReader</code> interface (the method will not throw an <code>InvalidArgumentException</code> in this
     * case).
     *
     * @return an array containing valid input types, never <code>null</code>
     */
    public Class[] getInputTypes() {
        return INPUT_TYPES;
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    public ProductReader createReaderInstance() {
        return new ACE2_5MinReader(this);
    }

    public BeamFileFilter getProductFileFilter() {
        return new BeamFileFilter(FORMAT_NAME, FILE_EXTENSIONS, DESCRIPTION);
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    public String[] getFormatNames() {
        return new String[]{
                FORMAT_NAME
        };
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same lenhth as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    public String[] getDefaultFileExtensions() {
        return FILE_EXTENSIONS;
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to <code>null</code> the default locale is
     * used.
     * <p/>
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if <code>null</code> the default locale is used
     *
     * @return a textual description of this product reader/writer
     */
    public String getDescription(final Locale locale) {
        return DESCRIPTION;
    }
}