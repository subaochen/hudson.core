/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.model.Hudson;
import hudson.model.Computer;
import hudson.util.TimeUnit2;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Channel;
import hudson.remoting.Callable;
import hudson.Extension;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Makes sure that connections to slaves are alive, and if they are not, cut them off.
 *
 * <p>
 * If we only rely on TCP retransmission time out for this, the time it takes to detect a bad connection
 * is in the order of 10s of minutes, so we take the matters to our own hands.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.325
 */
@Extension
public class ConnectionActivityMonitor extends AsyncPeriodicWork {
    public ConnectionActivityMonitor() {
        super("Connection Activity monitoring to slaves");
    }

    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        if (!enabled)   return;

        long now = System.currentTimeMillis();
        for (Computer c: Hudson.getInstance().getComputers()) {
            VirtualChannel ch = c.getChannel();
            if (ch instanceof Channel) {
                Channel channel = (Channel) ch;
                if (now-channel.getLastHeard() > TIME_TILL_PING) {
                    // haven't heard from this slave for a while.
                    Long lastPing = (Long)channel.getProperty(ConnectionActivityMonitor.class);

                    if (lastPing!=null && now-lastPing > TIMEOUT) {
                        LOGGER.info("Repeated ping attempts failed on "+c.getName()+". Disconnecting");
                        c.disconnect(OfflineCause.create(Messages._ConnectionActivityMonitor_OfflineCause()));
                    } else {
                        // send a ping. if we receive a reply, it will be reflected in the next getLastHeard() call.
                        channel.callAsync(PING_COMMAND);
                        if (lastPing==null)
                            channel.setProperty(ConnectionActivityMonitor.class,now);
                    }
                } else {
                    // we are receiving data nicely
                    channel.setProperty(ConnectionActivityMonitor.class,null);
                }
            }
        }
    }

    public long getRecurrencePeriod() {
        return enabled ? FREQUENCY : TimeUnit2.DAYS.toMillis(30);
    }

    /**
     * Time till initial ping
     */
    private static final long TIME_TILL_PING = Long.getLong(ConnectionActivityMonitor.class.getName()+".timeToPing",TimeUnit2.MINUTES.toMillis(3));

    private static final long FREQUENCY = Long.getLong(ConnectionActivityMonitor.class.getName()+".frequency",TimeUnit2.SECONDS.toMillis(10));

    /**
     * When do we abandon the effort and cut off?
     */
    private static final long TIMEOUT = Long.getLong(ConnectionActivityMonitor.class.getName()+".timeToPing",TimeUnit2.MINUTES.toMillis(4));


    // disabled by default until proven in the production
    public boolean enabled = Boolean.getBoolean(ConnectionActivityMonitor.class.getName()+".enabled");

    private static final PingCommand PING_COMMAND = new PingCommand();
    private static final class PingCommand implements Callable<Void,RuntimeException> {
        public Void call() throws RuntimeException {
            return null;
        }

        private static final long serialVersionUID = 1L;
    }

    private static final Logger LOGGER = Logger.getLogger(ConnectionActivityMonitor.class.getName());
}
