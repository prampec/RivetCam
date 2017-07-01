/*
 * File: FileManager.java
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

import java.io.File;

/**
 * Manages files and directories.
 *
 * Created by kelemenb on 6/20/17.
 */
public class FileManager {
    // -- TODO: introduce properties instead of using constants
    public final String directoryPrefix;
    public final int directoryIndexDigits;
    public final String filePrefix;
    public final String filePostfix;
    public final int fileIndexDigits;
    public final String baseDirectoryPath;
    public final boolean restartFileIndexWithNewDirectory;

    private File activeWorkingDirectory = null;
    private int nextFileIndex = 0;

    public FileManager(ConfigurationManager configurationManager) {
        directoryPrefix = configurationManager.directoryPrefix;
        directoryIndexDigits = configurationManager.directoryIndexDigits;
        filePrefix = configurationManager.filePrefix;
        filePostfix = configurationManager.filePostfix;
        fileIndexDigits = configurationManager.fileIndexDigits;
        baseDirectoryPath = configurationManager.baseDirectoryPath;
        restartFileIndexWithNewDirectory = configurationManager.restartFileIndexWithNewDirectory;
    }

    public String createNewWorkingDirectory()
    {
        int i = 1;
        activeWorkingDirectory = getDirectory(i);
        // TODO: might want to use File.list, to pick the biggest index.
        while(activeWorkingDirectory.exists()) {
            i += 1;
            activeWorkingDirectory = getDirectory(i);
        }
        if (!activeWorkingDirectory.mkdir()) {
            throw new IllegalStateException("Error creating working directory: " + activeWorkingDirectory.getAbsolutePath());
        }
        if (restartFileIndexWithNewDirectory) {
            nextFileIndex = 0;
        }
        return  activeWorkingDirectory.getName();
    }

    public File getNextFile() {
        File file = getFile(nextFileIndex);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException(
                        "File with name " + file.getAbsolutePath()
                                + " already exists, and cannot be removed.");
            }
        }
        nextFileIndex += 1;
        return file;
    }

    public String removeLast() {
        if (nextFileIndex > 0) {
            nextFileIndex -= 1;
            File file = getFile(nextFileIndex);
            if (!file.delete()) {
                throw new IllegalStateException(
                        "Cannot remove last file: " + file.getAbsolutePath());
            }
            return formatName(file);
        } else {
            return "";
        }
    }

    private File getDirectory(int i) {
        return new File(
                baseDirectoryPath,
                directoryPrefix + String.format("%0" + directoryIndexDigits + "d", i));
    }

    private File getFile(int i) {
        return new File(
                activeWorkingDirectory,
                filePrefix + String.format("%0" + fileIndexDigits + "d", i) + filePostfix);
    }

    public String formatName(File file) {
        return activeWorkingDirectory.getName() + "/" + file.getName();
    }

    public boolean hasBatch() {
        return activeWorkingDirectory != null;
    }
}
