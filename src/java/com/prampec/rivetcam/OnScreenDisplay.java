/*
 * File: OnScreenDisplay.java
 * Description:
 *    RivetCam is an open source photographic software, where you
 *    can capture still images, potentially use to create stop-motion videos.
 *    Documentation: https://doc.csipa.hu/?page_id=415
 *
 * Author: Balazs Kelemen
 * Contact: prampec+rivetcam@gmail.com
 * Copyright: 2017 Balazs Kelemen
 * Copying permission statement:
 *     This file is part of RivetCam.
 *     RivetCam is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prampec.rivetcam;

import java.util.*;

/**
 * Class to handle messages to be displayed on the screen.
 * Created by kelemenb on 6/19/17.
 */
public class OnScreenDisplay {
    public static final int QUEUE_LENGTH = 5;
    public static final int MESSAGE_ON_SCREEN_MS = 2200;

    protected HashMap<String, String> messages = new HashMap<>();
    protected LinkedList<String> keys = new LinkedList<>();
    private OsdEventListener listener;
    private Date lastMessageArrived = new Date(0);
    private Timer timer = new Timer();

    public OnScreenDisplay(OsdEventListener listener) {
        this.listener = listener;
    }

    public void add(String message) {
        add(UUID.randomUUID().toString(), message);
    }
    public synchronized void replace(String key, String message) {
        messages.remove(key);
        keys.remove(key);
        this.add(message);
    }
    public synchronized void add(String key, String message) {
        if (keys.contains(key)) {
            keys.remove(key);
        }
        keys.add(key);
        messages.put(key, message);
        if (keys.size() > QUEUE_LENGTH) {
            String keysRemoving = keys.removeFirst();
            messages.remove(keysRemoving);
        }
        lastMessageArrived = new Date();
        listener.newMessageArrived(messagesToDisplay());
//        timer.cancel();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                listener.noMessagesToDisplay();
            }
        }, MESSAGE_ON_SCREEN_MS);
    }

    public List<String> messagesToDisplay() {
        if(new Date(lastMessageArrived.getTime() + MESSAGE_ON_SCREEN_MS).before(new Date())) {
            return null;
        }
        List<String> m = new ArrayList<>(keys.size());
        for (String k : keys) {
            m.add(messages.get(k));
        }
        return m;
    }

    public void dispose() {
        timer.cancel();
    }

    public interface OsdEventListener {
        public void newMessageArrived(List<String> messages);
        public void noMessagesToDisplay();
    }


}
