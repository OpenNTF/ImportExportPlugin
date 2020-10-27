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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class LogUtil {
	private static final Logger logger = ImportExportPlugin.getLogger();

	/*
	 * Create export/import log file
	 */
	public File createLogFile(IPath path, String copyType) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy_MM_dd@HH_mm_ss"); //$NON-NLS-1$
		StringBuffer fileName = new StringBuffer().append(copyType).append("_") //$NON-NLS-1$
				.append(sDateFormat.format(new Date())).append(".log"); //$NON-NLS-1$
		File file = new File(path.toFile(), fileName.toString());
		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e) {
			logger.log(Level.SEVERE, Messages.error_create_logfile
					+ "\n" + e.getMessage(), e); //$NON-NLS-1$
		}
		return file;
	}

	public boolean writeListToLogStream(FileOutputStream fileOutputStream,
			List<String> logList, String str) {
		int num = logList.size();
		if (num == 0) {
			return true;
		}
		try {
			fileOutputStream.write(NLS.bind(str, num).getBytes());
			for (int i = 0; i < num; i++) {
				fileOutputStream.write("\n".getBytes()); //$NON-NLS-1$
				fileOutputStream.write(logList.get(i).getBytes());
			}
			fileOutputStream.write("\n\n".getBytes()); //$NON-NLS-1$
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		return true;
	}

	public boolean writeStringToLogStream(FileOutputStream fileOutputStream,
			String str) {
		try {
			fileOutputStream.write("\n".getBytes()); //$NON-NLS-1$
			fileOutputStream.write(str.getBytes());
			fileOutputStream.write("\n\n".getBytes()); //$NON-NLS-1$
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		return true;
	}

}
