package org.intermine.util;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.lang.ref.WeakReference;
import java.io.Writer;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class provides a "shutdown" service to other classes. It sets up a single
 * shutdown hook, which iterates through a series of objects that are registered with it,
 * shutting them down in turn. This class currently supports java.io.Writer,
 * java.io.OutputStream, and ObjectStoreWriterInterMineImpl. It allows objects to be
 * wrapped in a WeakReference, so that the shutdown hook does not preclude those objects
 * from being garbage collected.
 *
 * @author Matthew Wakeling
 */
public class ShutdownHook extends Thread
{
    private static final Logger LOG = Logger.getLogger(ShutdownHook.class);
    private static Set objects = new HashSet();
    private static ShutdownHook instance;

    static {
        instance = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(instance);
    }

    /**
     * Creates an instance.
     */
    private ShutdownHook() {
        objects = new HashSet();
    }

    /**
     * Registers an object with the shutdown hook.
     *
     * @param object the object
     */
    public static synchronized void registerObject(Object object) {
        instance.objects.add(object);
    }

    /**
     * Performs the shutdown.
     */
    private static synchronized void shutdown() {
        Iterator iter = objects.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            try {
                if (o instanceof WeakReference) {
                    o = ((WeakReference) o).get();
                }
                if (o instanceof Writer) {
                    ((Writer) o).flush();
                    ((Writer) o).close();
                } else if (o instanceof OutputStream) {
                    ((OutputStream) o).flush();
                    ((OutputStream) o).close();
                } else if (o instanceof Shutdownable) {
                    ((Shutdownable) o).shutdown();
                } else if (o != null) {
                    LOG.error("Do not know how to shut down " + o);
                }
            } catch (Exception e) {
                LOG.error("Exception while shutting down " + o + ": " + e);
            }
        }
    }

    /**
     * @see Thread#run
     */
    public void run() {
        shutdown();
    }
}
