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
package com.ibm.designer.extensibility.copyfile.openntf.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathUtil {
	private static IPath tempDir = null;
	private static IPath logsDir = null;

	synchronized public static IPath getImportTempDir() {
		if (tempDir == null) {
			tempDir = new Path(System.getenv("TEMP")); //$NON-NLS-1$
			if (!tempDir.toFile().exists()) {
				tempDir = new Path(System.getenv("TMP")); //$NON-NLS-1$
				if (!tempDir.toFile().exists()) {
					tempDir = new Path("C:\temp"); //$NON-NLS-1$
				}
			}
			tempDir = tempDir.append("OpenNTF"); //$NON-NLS-1$
		}
		if (!tempDir.toFile().exists()) {
			tempDir.toFile().mkdirs();
		}

		return tempDir;
	}

	synchronized public static IPath getImportLogsDir() {
		if (logsDir == null) {
			logsDir = getImportTempDir().append("logs"); //$NON-NLS-1$
		}
		if (!logsDir.toFile().exists()) {
			logsDir.toFile().mkdirs();
		}
		return logsDir;
	}
}
