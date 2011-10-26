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
package org.esa.nest.util;

import org.esa.beam.visat.VisatApp;

/**
 * status bar Progress monitor
 */
public final class StatusProgressMonitor {
    private final VisatApp visatApp = VisatApp.getApp();
    private final float max;
    private final String msg;
    private int lastPct = 0;
    private boolean allowStdOut = true;

    public StatusProgressMonitor(final float max, final String msg) {
        this.max = max;
        this.msg = msg;
    }

    public void worked(final int i) {

        if(visatApp != null) {
            final int pct = (int)((i/max) * 100);
            if(pct >= lastPct + 1) {
                visatApp.setStatusBarMessage(msg+pct+'%');
                lastPct = pct;
            }
        } else if(allowStdOut) {
            final int pct = (int)((i/max) * 100);
            if(pct >= lastPct + 10) {
                if(lastPct==0) {
                    System.out.print(msg);
                }
                System.out.print(" "+pct+'%');
                lastPct = pct;
            }
        }
    }

    public void working() {
        if(visatApp != null) {
            visatApp.setStatusBarMessage(msg);
        }
    }

    public void done() {
        if(visatApp != null)
            visatApp.setStatusBarMessage("");
        else if(allowStdOut)
            System.out.println(" 100%");
    }

    public void setAllowStdOut(final boolean flag) {
        allowStdOut = flag;
    }

}