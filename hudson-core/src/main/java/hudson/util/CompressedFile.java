/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 
 *    Kohsuke Kawaguchi
 *
 *
 *******************************************************************************/ 

package hudson.util;

import hudson.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Represents write-once read-many file that can be optiionally compressed to
 * save disk space. This is used for console output and other bulky data.
 *
 * <p> In this class, the data on the disk can be one of two states: <ol>
 * <li>Uncompressed, in which case the original data is available in the
 * specified file name. <li>Compressed, in which case the gzip-compressed data
 * is available in the specifiled file name + ".gz" extension. </ol>
 *
 * Once the file is written and completed, it can be compressed asynchronously
 * by {@link #compress()}.
 *
 * @author Kohsuke Kawaguchi
 */
public class CompressedFile {

    /**
     * Executor used for compression. Limited up to one thread since this should
     * be a fairly low-priority task.
     */
    private static final ExecutorService COMPRESSION_THREAD = new ThreadPoolExecutor(
            0, 1, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
            new ExceptionCatchingThreadFactory(new DaemonThreadFactory()));
    private static final Logger LOGGER = Logger.getLogger(CompressedFile.class.getName());
    /**
     * The name of the raw file.
     */
    private final File file;
    /**
     * The name of the compressed file.
     */
    private final File gz;

    public CompressedFile(File file) {
        this.file = file;
        this.gz = new File(file.getParentFile(), file.getName() + ".gz");
    }

    /**
     * Gets the OutputStream to write to the file.
     */
    public OutputStream write() throws FileNotFoundException {
        if (gz.exists()) {
            gz.delete();
        }
        return new FileOutputStream(file);
    }

    /**
     * Reads the contents of a file.
     */
    public InputStream read() throws IOException {
        if (file.exists()) {
            return new FileInputStream(file);
        }

        // check if the compressed file exists
        if (gz.exists()) {
            return new GZIPInputStream(new FileInputStream(gz));
        }
        // no such file
        throw new FileNotFoundException(file.getName());
    }

    /**
     * Loads the file content as a string.
     */
    public String loadAsString() throws IOException {
        long sizeGuess;
        if (file.exists()) {
            sizeGuess = file.length();
        } else if (gz.exists()) {
            sizeGuess = gz.length() * 2;
        } else {
            return "";
        }

        StringBuilder str = new StringBuilder((int) sizeGuess);

        Reader r = null;
        try {
            r = new InputStreamReader(read());
            char[] buf = new char[8192];
            int len;
            while ((len = r.read(buf, 0, buf.length)) > 0) {
                str.append(buf, 0, len);
            }
        } finally {
            IOUtils.closeQuietly(r);
        }
        return str.toString();
    }

    /**
     * Asynchronously schedules the compression of this file.
     *
     * <p> Once the file is compressed, the original will be removed and the
     * further reading will be done from the compressed stream.
     */
    public void compress() {
        COMPRESSION_THREAD.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream in = read();
                    OutputStream out = new GZIPOutputStream(new FileOutputStream(gz));
                    try {
                        Util.copyStream(in, out);
                    } finally {
                        in.close();
                        out.close();
                    }
                    // if the compressed file is created successfully, remove the original
                    file.delete();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to compress " + file, e);
                    // in case a processing is left in the middle
                    gz.delete();
                }
            }
        });
    }
}
