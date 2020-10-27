/*
 * © Copyright IBM Corp. 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.designer.extensibility.copyfile.anttasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class EclipseFileList extends FileList {

	public EclipseFileList(FileList filelist) {
		super(filelist);
	}

	/**
	 * Returns the list of files represented by this FileList.
	 * 
	 * @param p
	 *            the current project
	 * @param eclipseProject
	 *            the eclipse project
	 * @return the list of files represented by this FileList.
	 * @throws BuildException
	 */
	public List<IResource> getFileResources(Project p, IProject eclipseProject,
			boolean debug) {
		List<IResource> fileResources = new ArrayList<IResource>();
		File dir = getDir(p);
		String[] filenames = getFiles(p);

		if (dir == null || dir.equals("")) { //$NON-NLS-1$
			throw new BuildException("No directory specified for filelist."); //$NON-NLS-1$
		}
		if (debug) {
			System.out.println("EclipseFileList.getFileResources: dir = " //$NON-NLS-1$
					+ dir.getAbsolutePath());
		}

		if (filenames.length == 0) {
			throw new BuildException("No files specified for filelist."); //$NON-NLS-1$
		}

		IPath path = new Path(dir.getAbsolutePath().substring(2));
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
			throw new BuildException("No directory specified for filelist."); //$NON-NLS-1$
		}

		if (debug) {
			System.out
					.println("EclipseFileList.getFileResources: relativePath = " //$NON-NLS-1$
							+ relativePath.toString());
		}

		for (int i = 0; i < filenames.length; i++) {
			IPath filePath = relativePath.append(filenames[i]);
			IResource resource = eclipseProject.getFile(filePath);
			if (resource.exists() && resource instanceof IFile) {
				fileResources.add(resource);
				if (debug) {
					System.out
							.println("EclipseFileList.getFileResources: add file: " //$NON-NLS-1$
									+ filePath.toString());
				}
			} else {
				if (debug) {
					System.out.println("EclipseFileList.getFileResources: " //$NON-NLS-1$
							+ filePath.toString() + " does not exist."); //$NON-NLS-1$
				}
			}
		}

		return fileResources;
	}

}
