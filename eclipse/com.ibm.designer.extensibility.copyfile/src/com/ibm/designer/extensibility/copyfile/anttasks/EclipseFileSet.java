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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class EclipseFileSet extends FileSet {

	/* cached DirectoryScanner instance for our own Project only */
	private EclipseDirectoryScanner directoryScanner = null;

	private boolean followSymlinks = true;

	public EclipseFileSet() {
	}

	public EclipseFileSet(FileSet fileset) {
		super(fileset);
	}

	/**
	 * Returns the directory scanner needed to access the files to process.
	 *
	 * @param p
	 *            the Project against which the DirectoryScanner should be
	 *            configured.
	 * @param eclipseProject
	 * @return a <code>EclipseDirectoryScanner</code> instance.
	 */
	public EclipseDirectoryScanner getDirectoryScanner(Project p,
			IProject eclipseProject, boolean debug) {
		if (isReference()) {
			return (EclipseDirectoryScanner) getRef(p).getDirectoryScanner(p);
		}

		EclipseDirectoryScanner ds = null;
		synchronized (this) {
			if (directoryScanner != null && p == getProject()) {
				ds = directoryScanner;
			} else {

				File dir = getDir(p);
				if (dir == null) {
					throw new BuildException(
							"No directory specified for fileset."); //$NON-NLS-1$
				}
				if (debug) {
					System.out
							.println("EclipseFileSet.getDirectoryScanner: dir = " //$NON-NLS-1$
									+ dir.toString());
				}

				IPath path = new Path(dir.toString().substring(2));
				IPath relativePath = null;
				int index = path.segmentCount() - 1;
				for (; index >= 0; index--) {
					if (path.segment(index).equals(eclipseProject.getName())) {
						relativePath = path.removeFirstSegments(index + 1);
						setDir(relativePath.toFile());
						break;
					}
				}
				if (index < 0) {
					throw new BuildException(
							"No directory specified for fileset."); //$NON-NLS-1$
				}

				if (debug) {
					System.out
							.println("EclipseFileSet.getDirectoryScanner: relativePath = " //$NON-NLS-1$
									+ relativePath.toString());
				}

				IResource resource = eclipseProject.getFile(relativePath);
				if ((!resource.exists() || !(resource instanceof IFolder))
						&& debug) {
					System.out.println("EclipseFileSet.getDirectoryScanner: " //$NON-NLS-1$
							+ relativePath
							+ " not found or is not a directory."); //$NON-NLS-1$
				}

				ds = new EclipseDirectoryScanner();
				setupDirectoryScanner(ds, p);
				ds.setFollowSymlinks(followSymlinks);

				directoryScanner = (p == getProject()) ? ds : directoryScanner;
			}
		}
		ds.scan(eclipseProject);
		return ds;
	}

}
