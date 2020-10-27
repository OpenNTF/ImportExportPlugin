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

import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.dialogs.IOverwriteQuery;

import com.ibm.designer.extensibility.copyfile.internal.EclipseExportOperation;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class ExportTask extends Task {
	private String todir = null;
	private String projectName = null;
	private IProject eclipseProject = null;
	private boolean overwriteolder = true;
	private boolean createlog = true;
	private boolean debug = false;

	private Vector<FileSet> filesets = new Vector<FileSet>();
	private Vector<FileList> filelists = new Vector<FileList>();

	private Vector<Object> filesToExport = new Vector<Object>();

	public ExportTask() {
	}

	public ExportTask(Vector<Object> filesToExport, String todir) {
		this.filesToExport = filesToExport;
		this.todir = todir;
	}

	public void addFileset(EclipseFileSet fileset) {
		filesets.add(fileset);
	}

	public void addFileList(FileList filelist) {
		filelists.add(filelist);
	}

	protected void validate() {
		if (todir == null || todir.equals("")) { //$NON-NLS-1$
			throw new BuildException("todir not set"); //$NON-NLS-1$
		}
		if (projectName == null || projectName.equals("")) { //$NON-NLS-1$
			throw new BuildException("project not set"); //$NON-NLS-1$
		}
		if (eclipseProject == null) {
			throw new BuildException(
					"project does not exist or can not be open"); //$NON-NLS-1$
		}
		if (filesets.size() < 1 && filelists.size() < 1) {
			throw new BuildException("fileset and filelist not set"); //$NON-NLS-1$
		}
	}

	// The method executing the task
	@Override
	public void execute() throws BuildException {
		validate();
		executeFileSet();
		executeFileList();
		executeExport();
	}

	// The method adding files included in filelist to filesToExport
	public void executeFileList() {
		for (FileList filelist2 : filelists) {
FileList filelist = filelist2;
EclipseFileList fl = new EclipseFileList(filelist);
List<IResource> includedFiles = fl.getFileResources(getProject(),
			eclipseProject, debug);
filesToExport.addAll(includedFiles);
}
	}

	// The method adding folder included in fileset to filesToExport
	public void executeFileSet() {
		for (FileSet fileset2 : filesets) {
			FileSet fileset = fileset2;
			EclipseFileSet fs = new EclipseFileSet(fileset);
			EclipseDirectoryScanner ds = fs
					.getDirectoryScanner(getProject(), eclipseProject, debug);
			IFolder folder = ds.getIncludedFolder();
			if (debug) {
				System.out.println("ExportTask.executeFileSet: basedir = " //$NON-NLS-1$
						+ ds.getBasedir());
				System.out.println("ExportTask.executeFileSet: add folder = " //$NON-NLS-1$
						+ folder.toString());
			}
			filesToExport.add(folder);
		}
	}

	// The method executing export
	@SuppressWarnings("restriction")
	public void executeExport() {
		EclipseExportOperation exporter = new EclipseExportOperation(null,
				filesToExport, todir, new IOverwriteQuery() {
					/*
					 * (non-Javadoc)
					 *
					 * @see
					 * org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite
					 * (java.lang.String)
					 */
					@Override
					public String queryOverwrite(String pathString) {
						// Overwrite all files in the destination project
						return IOverwriteQuery.ALL;
					}
				});

		exporter.setCreateLeadupStructure(true);
		exporter.setOverwriteFiles(true);

		exporter.setCreateLog(createlog);
		exporter.setOverwriteOlder(overwriteolder);

		try {
			exporter.run(new NullProgressMonitor());
		} catch (InterruptedException e) {
			// do nothing if the thread is interrupted
		}
		if (createlog) {
			exporter.createLogFile(todir);
		}
	}

	// The setter for the "todir" attribute
	public void setTodir(String todir) {
		this.todir = todir;
	}

	// The setter for the "project" attribute
	public void setProject(String project) {
		this.projectName = project;
		eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(
				project);
		if (!eclipseProject.isOpen()) {
			try {
				eclipseProject.open(new NullProgressMonitor());
			} catch (CoreException e) {
				return;
			}
		}
	}

	// The setter for the "overwriteolder" attribute
	public void setOverwriteolder(String overwriteolder) {
		if (overwriteolder.equalsIgnoreCase("yes") //$NON-NLS-1$
				|| overwriteolder.equalsIgnoreCase("y")) { //$NON-NLS-1$
			this.overwriteolder = true;
		} else if (overwriteolder.equalsIgnoreCase("no") //$NON-NLS-1$
				|| overwriteolder.equalsIgnoreCase("n")) { //$NON-NLS-1$
			this.overwriteolder = false;
		}
	}

	// The setter for the "createlog" attribute
	public void setCreatelog(String createlog) {
		if (createlog.equalsIgnoreCase("yes") //$NON-NLS-1$
				|| createlog.equalsIgnoreCase("y")) { //$NON-NLS-1$
			this.createlog = true;
		} else if (createlog.equalsIgnoreCase("no") //$NON-NLS-1$
				|| createlog.equalsIgnoreCase("n")) { //$NON-NLS-1$
			this.createlog = false;
		}
	}

	// The setter for the "debug" attribute
	public void setDebug(String debug) {
		if (debug.equalsIgnoreCase("yes") //$NON-NLS-1$
				|| debug.equalsIgnoreCase("y")) { //$NON-NLS-1$
			this.debug = true;
		} else if (debug.equalsIgnoreCase("no") //$NON-NLS-1$
				|| debug.equalsIgnoreCase("n")) { //$NON-NLS-1$
			this.debug = false;
		}
	}

}
