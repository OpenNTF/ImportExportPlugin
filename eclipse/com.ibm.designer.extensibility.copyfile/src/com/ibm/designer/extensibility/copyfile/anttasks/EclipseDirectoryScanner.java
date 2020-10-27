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
package com.ibm.designer.extensibility.copyfile.anttasks;

import java.io.File;

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class EclipseDirectoryScanner extends DirectoryScanner {
	/**
	 * Scanning lock.
	 * 
	 * @since Ant 1.6.3
	 * @see DirectoryScanner
	 */
	private Object scanLock = new Object();

	/**
	 * Scanning flag.
	 * 
	 * @since Ant 1.6.3
	 * @see DirectoryScanner
	 */
	private boolean scanning = false;

	/**
	 * Exception thrown during scan.
	 * 
	 * @since Ant 1.6.3
	 * @see DirectoryScanner
	 */
	private IllegalStateException illegal = null;

	/**
	 * Whether a missing base directory is an error.
	 * 
	 * @since Ant 1.7.1
	 * @see DirectoryScanner
	 */
	protected boolean errorOnMissingDir = true;

	private IFolder includedFolder = null;

	/**
	 * Sole constructor.
	 */
	public EclipseDirectoryScanner() {
	}

	/**
	 * Scan for includedFolder.
	 * 
	 * @param eclipseProject
	 * @exception IllegalStateException
	 *                if the base directory was set incorrectly (i.e. if it
	 *                doesn't exist or isn't a directory).
	 */
	public void scan(IProject eclipseProject) throws IllegalStateException {
		synchronized (scanLock) {
			if (scanning) {
				while (scanning) {
					try {
						scanLock.wait();
					} catch (InterruptedException e) {
						continue;
					}
				}
				if (illegal != null) {
					throw illegal;
				}
				return;
			}
			scanning = true;
		}
		try {
			synchronized (this) {
				illegal = null;
				clearResults();

				IPath path = null;
				File dir = getBasedir();
				if (dir == null) {
					// if no basedir, nothing to do:
					return;
				} else {
					path = new Path(dir.toString());
					if (!eclipseProject.exists(path)) {
						if (errorOnMissingDir) {
							illegal = new IllegalStateException("basedir " //$NON-NLS-1$
									+ dir + " does not exist"); //$NON-NLS-1$
						} else {
							// Nothing to do if basedir does not exist
							return;
						}
					}
					if (!(eclipseProject.getFolder(path).exists())) {
						illegal = new IllegalStateException("basedir " + dir //$NON-NLS-1$
								+ " is not a directory"); //$NON-NLS-1$
					}
					if (illegal != null) {
						throw illegal;
					}
				}

				setIncludedFolder(eclipseProject.getFolder(path));
			}
		} finally {
			synchronized (scanLock) {
				scanning = false;
				scanLock.notifyAll();
			}
		}
	}

	public synchronized void setIncludedFolder(IFolder includedFolder) {
		this.includedFolder = includedFolder;
	}

	/**
	 * Return the folder, it is relative to the base directory.
	 * 
	 * @return IFolder
	 */
	public synchronized IFolder getIncludedFolder() {
		if (includedFolder == null) {
			throw new IllegalStateException("Must call scan() first"); //$NON-NLS-1$
		}
		return includedFolder;
	}

}
