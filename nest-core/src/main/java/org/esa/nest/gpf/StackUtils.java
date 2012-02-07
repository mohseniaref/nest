/*
 * Copyright (C) 2011 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.nest.gpf;

import org.esa.beam.framework.datamodel.*;
import org.esa.beam.util.StringUtils;
import org.esa.nest.datamodel.AbstractMetadata;

/**
 * Helper methods for working with Stack products
 */
public final class StackUtils {

    public static boolean isCoregisteredStack(final Product product) {
        final MetadataElement absRoot = AbstractMetadata.getAbstractedMetadata(product);
        return absRoot != null && absRoot.getAttributeInt(AbstractMetadata.coregistered_stack, 0) == 1;
    }

    public static String getBandTimeStamp(final Product product) {
        final MetadataElement absRoot = AbstractMetadata.getAbstractedMetadata(product);
        if(absRoot != null) {
            String dateString = OperatorUtils.getAcquisitionDate(absRoot);
            if(!dateString.isEmpty())
                dateString = '_' + dateString;
            return StringUtils.createValidName(dateString, new char[]{'_', '.'}, '_');
        }
        return "";
    }

    public static void saveMasterProductBandNames(final Product targetProduct, final String[] masterProductBands) {
        final MetadataElement targetSlaveMetadataRoot = AbstractMetadata.getSlaveMetadata(targetProduct);
        final StringBuilder value = new StringBuilder(255);
        for(String name : masterProductBands) {
            value.append(name);
            value.append(' ');
        }

        targetSlaveMetadataRoot.setAttributeString(AbstractMetadata.MASTER_BANDS, value.toString().trim());
    }

    public static void saveSlaveProductBandNames(final Product targetProduct, final String slvProductName,
                                              final String[] bandNames) {
        final MetadataElement targetSlaveMetadataRoot = AbstractMetadata.getSlaveMetadata(targetProduct);
        final MetadataElement elem = targetSlaveMetadataRoot.getElement(slvProductName);
        StringBuilder value = new StringBuilder(255);
        for(String name : bandNames) {
            value.append(name);
            value.append(' ');
        }
        elem.setAttributeString(AbstractMetadata.SLAVE_BANDS, value.toString().trim());
    }

    public static String[] getMasterBandNames(final Product sourceProduct) {
        final MetadataElement slaveMetadataRoot = sourceProduct.getMetadataRoot().getElement(
                                                        AbstractMetadata.SLAVE_METADATA_ROOT);
        if(slaveMetadataRoot != null) {
            final String mstBandNames = slaveMetadataRoot.getAttributeString(AbstractMetadata.MASTER_BANDS, "");
            return StringUtils.stringToArray(mstBandNames, " ");

        }
        return new String[] {};
    }

    public static String[] getSlaveBandNames(final Product sourceProduct, final String slvProductName) {
       final MetadataElement slaveMetadataRoot = sourceProduct.getMetadataRoot().getElement(
                                                        AbstractMetadata.SLAVE_METADATA_ROOT);
        if(slaveMetadataRoot != null) {
            final MetadataElement elem = slaveMetadataRoot.getElement(slvProductName);
                final String slvBandNames = elem.getAttributeString(AbstractMetadata.SLAVE_BANDS, "");
                return StringUtils.stringToArray(slvBandNames, " ");
        }
        return new String[] {};
    }

    public static String[] getSlaveProductNames(final Product sourceProduct) {
        final MetadataElement slaveMetadataRoot = sourceProduct.getMetadataRoot().getElement(
                                                        AbstractMetadata.SLAVE_METADATA_ROOT);
        if(slaveMetadataRoot != null) {
            return slaveMetadataRoot.getElementNames();
        }
        return new String[]{};
    }

    public static String getSlaveProductName(final Product sourceProduct, final Band slvBand) {
        final MetadataElement slaveMetadataRoot = sourceProduct.getMetadataRoot().getElement(
                                                        AbstractMetadata.SLAVE_METADATA_ROOT);
        if(slaveMetadataRoot != null) {
            final String slvBandName = slvBand.getName();
            for(MetadataElement elem : slaveMetadataRoot.getElements()) {
                final String slvBandNames = elem.getAttributeString(AbstractMetadata.SLAVE_BANDS, "");
                if(slvBandNames.contains(slvBandName))
                    return elem.getName();
            }
        }
        return null;
    }

    public static String[] bandsToStringArray(final Band[] bands) {
        final String[] names = new String[bands.length];
        int i = 0;
        for (Band band : bands) {
            names[i++] = band.getName();
        }
        return names;
    }
}