/**
 * Copyright © 2009-2020 IBM Corp. and Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.designer.extensibility.copyfile.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class MyFileUtil {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private static String sourceClass = "com.ibm.designer.extensibility.copyfile.util.MyFileUtil"; //$NON-NLS-1$

	private List<Object> allFilesList = null;

	public MyFileUtil() {

	}

	private void readFolder(File dir) {
		String sourceMethod = "readFolder"; //$NON-NLS-1$
		if (dir.exists() && dir.isDirectory()) {
			File[] dirList = dir.listFiles();

			for (int i = 0; i < dirList.length; i++) {
				File file = dirList[i];
				if (file.isDirectory()) {
					readFolder(file);
				} else if (file.isFile()) {
					allFilesList.add(file);
				}
			}
		} else {
			logger.logp(Level.WARNING, sourceClass, sourceMethod,
					"{0} does not exist or it is not a directory.", dir); //$NON-NLS-1$
		}
	}

	public List<Object> getAllFilesList(File dir) {
		allFilesList = new ArrayList<Object>();
		readFolder(dir);
		return allFilesList;
	}

	public boolean delFolder(File dir) {
		boolean result = true;
		if (!dir.exists()) {
			return true;
		}

		File[] fileList = dir.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			if (file.isFile()) {
				result = file.delete();
			} else if (file.isDirectory()) {
				result = delFolder(file);
				result = file.delete();
			}
		}
		result = dir.delete();
		return result;
	}

	public boolean copyfile(File sourceFile, File destinationFile,
			boolean deleteIfExist) {
		String sourceMethod = "copyfile"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format(
					"source file = %s\ndestination file = %s", //$NON-NLS-1$
					sourceFile.getAbsolutePath(), destinationFile
							.getAbsolutePath()));
		}

		if (!sourceFile.exists()) {
			logger.logp(Level.WARNING, sourceClass, sourceMethod,
					"source file {0} does not exit", sourceFile); //$NON-NLS-1$
			logger.exiting(sourceMethod, sourceMethod, false);
			return false;
		}
		if (destinationFile.exists()) {
			if (deleteIfExist) {
				destinationFile.delete();
			} else {
				logger.logp(Level.WARNING, sourceClass, sourceMethod,
						"destination file {0} exits", destinationFile); //$NON-NLS-1$
				logger.exiting(sourceMethod, sourceMethod, false);
				return false;
			}
		}

		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			destinationFile.createNewFile();
			fin = new FileInputStream(sourceFile);
			fout = new FileOutputStream(destinationFile);
			byte[] bt = new byte[1024];
			while (fin.read(bt) != -1) {
				fout.write(bt, 0, bt.length);
			}
		} catch (IOException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			logger.exiting(sourceMethod, sourceMethod, false);
			return false;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
							.getMessage(), e);
				}
			}
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
							.getMessage(), e);
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod, true);
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MyFileUtil()
				.copyfile(
						new File(
								"C:\\Program Files\\IBM\\Lotus\\Notes\\Data\\workspace\\applications\\eclipse\\plugins\\com.ibm.designer.extensibility.copyfile_1.3.0\\files\\NOTICE"), //$NON-NLS-1$
						new File("d:\\nsf\\1"), true); //$NON-NLS-1$
	}

}
