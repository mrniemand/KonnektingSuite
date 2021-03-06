/*
 * Copyright (C) 2016 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of KONNEKTING Suite.
 *
 *   KONNEKTING Suite is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   KONNEKTING Suite is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with KONNEKTING DeviceConfig.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.konnekting.suite;

import de.konnekting.suite.events.EventBackgroundThread;
import de.root1.rooteventbus.RootEventBus;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public abstract class BackgroundTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(BackgroundTask.class);

    private final RootEventBus eventBus = RootEventBus.getDefault();

    private double progress;
    private double step;
    private final String action;
    private final long start;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final int i;

    public BackgroundTask(String action) {
        this(action, Thread.NORM_PRIORITY);
    }

    public BackgroundTask(String action, int priority) {
        i = COUNTER.incrementAndGet();
        this.start = System.currentTimeMillis();
        this.action = action;
        eventBus.post(new EventBackgroundThread(this));
        Thread t = new Thread(this, "BackgroundTask(" + i + ") '" + action + "'") {

            @Override
            public void run() {
                log.debug("Begin task(" + i + ") '" + action + "'");
                try {
                    super.run();
                } finally {
                    log.debug("Finished task(" + i + ") '" + action + "' in " + (System.currentTimeMillis() - start) + " ms.");
                }
            }

        };
        t.setPriority(priority);
        t.start();
    }

    @Override
    public abstract void run();

    public void setProgress(double progress) {
        this.progress = progress;
        eventBus.post(new EventBackgroundThread(this));
    }

    public double getProgress() {
        return progress;
    }

    public void setStepsToDo(int steps) {
        step = 1d / steps;
    }

    public void stepDone() {
        setProgress(progress + step);
    }

    public void setDone() {
        progress = 1;
        eventBus.post(new EventBackgroundThread(this));
    }

    public boolean isDone() {
        return progress == 1;
    }

    public String getAction() {
        return action;
    }

}
