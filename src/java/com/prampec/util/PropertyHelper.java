/*
 * File: PropertyHelper.java
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

package com.prampec.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Helper methods to process property files.
 * Created by kelemenb on 6/24/17.
 */
public class PropertyHelper {
    public static <T> List<T> readList(
            Properties properties,
            String subject,
            PropertyReader<T> propertyReader) {
        String idsString = properties.getProperty(subject);
        if (idsString == null) {
            return Collections.emptyList();
        }
        String[] ids = idsString.split("\\w*,\\w*");
        List<T> result = new ArrayList<>(ids.length);
        for (String id : ids) {
            String prefix = subject + "." + id + ".";
            T item = propertyReader.readItem(properties, prefix, id);
            result.add(item);
        }
        return result;
    }

    public interface PropertyReader<T> {
        T readItem(Properties properties, String prefix, String id);
    }
}
