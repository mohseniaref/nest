package org.esa.nest.dataio;

import org.esa.beam.framework.dataio.DecodeQualification;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.dataio.ProductReaderPlugIn;
import org.esa.beam.util.io.BeamFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * The ReaderPlugIn for NetCDF products.
 *
 */
public class NetCDFReaderPlugIn implements ProductReaderPlugIn {

    protected String[] FORMAT_NAMES = NetcdfConstants.NETCDF_FORMAT_NAMES;
	protected String[] FORMAT_FILE_EXTENSIONS = NetcdfConstants.NETCDF_FORMAT_FILE_EXTENSIONS;
    protected String PLUGIN_DESCRIPTION = NetcdfConstants.NETCDF_PLUGIN_DESCRIPTION;
    protected Class[] VALID_INPUT_TYPES = new Class[]{File.class, String.class};

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     *
     * @param input any input object
     *
     * @return true if this product reader can decode the given input, otherwise false.
     */
    public DecodeQualification getDecodeQualification(final Object input) {
        final File file = getFileFromInput(input);
        if (file == null) {
            return DecodeQualification.UNABLE;
        }

        final File parentDir = file.getParentFile();
        if (file.isFile() && parentDir.isDirectory()) {
            final FilenameFilter filter = new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    return true;//name.contains(constants.getIndicationKey());
                }
            };
            final File[] files = parentDir.listFiles(filter);
            if (files != null) {
                return checkProductQualification(file);
            }
        }
        return DecodeQualification.UNABLE;
    }

    DecodeQualification checkProductQualification(final File file) {
        for(String ext : FORMAT_FILE_EXTENSIONS) {
            if(!ext.isEmpty() && file.getName().toLowerCase().endsWith(ext.toLowerCase()))
                return DecodeQualification.INTENDED;
        }

        return DecodeQualification.UNABLE;
    }

     public static File getFileFromInput(final Object input) {
        if (input instanceof String) {
            return new File((String) input);
        } else if (input instanceof File) {
            return (File) input;
        }
        return null;
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
        return VALID_INPUT_TYPES;
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    public ProductReader createReaderInstance() {
        return new NetCDFReader(this);
    }

    public BeamFileFilter getProductFileFilter() {
        return new FileFilter(FORMAT_NAMES[0], FORMAT_FILE_EXTENSIONS, PLUGIN_DESCRIPTION);
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    public String[] getFormatNames() {
        return FORMAT_NAMES;
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same length as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    public String[] getDefaultFileExtensions() {
        return FORMAT_FILE_EXTENSIONS;
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
        return PLUGIN_DESCRIPTION;
    }

    public static class FileFilter extends BeamFileFilter {

        public FileFilter(final String formatName, final String[] fileExts, final String description) {
            super(formatName, fileExts, description);
        }

        /**
         * Tests whether or not the given file is accepted by this filter. The default implementation returns
         * <code>true</code> if the given file is a directory or the path string ends with one of the registered extensions.
         * if no extension are defined, the method always returns <code>true</code>
         *
         * @param file the file to be or not be accepted.
         *
         * @return <code>true</code> if given file is accepted by this filter
         */
        public boolean accept(final File file) {
            if (super.accept(file)) {
                if (file.isDirectory() || checkExtension(file)) {
                    return true;
                }
            }
            return false;
        }

    }
}