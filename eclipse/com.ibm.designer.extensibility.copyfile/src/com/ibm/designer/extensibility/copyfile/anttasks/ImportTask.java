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
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

import com.ibm.designer.extensibility.copyfile.internal.EclipseImportOperation;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class ImportTask extends Task {
	private String fromdir = null; // absolute full path, end with ".nsf"
	private String projectName = null;
	private IProject eclipseProject = null;
	private boolean overwriteolder = true;
	private boolean createlog = true;
	private boolean debug = false;

	private Vector<FileSet> filesets = new Vector<FileSet>();
	private Vector<FileList> filelists = new Vector<FileList>();

	private Vector<Object> filesToImport = new Vector<Object>();

	public ImportTask() {
		super();
	}

	public ImportTask(Vector<Object> filesToImport, String fromdir) {
		super();
		this.filesToImport = filesToImport;
		this.fromdir = fromdir;
	}

	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}

	public void addFileList(FileList filelist) {
		filelists.add(filelist);
	}

	protected void validate() {
		if (fromdir == null || fromdir.equals("")) { //$NON-NLS-1$
			throw new BuildException("fromdir not set"); //$NON-NLS-1$
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
	public void execute() throws BuildException {
		validate();
		executeFileSet();
		executeFileList();
		executeImport();
	}

	// The method adding files included in filelist to filesToImport
	public void executeFileList() {
		for (Iterator<FileList> itFLists = filelists.iterator(); itFLists
				.hasNext();) {
			FileList fl = (FileList) itFLists.next();
			File fileDir = fl.getDir(getProject());
			if (!fileDir.exists() || !fileDir.isDirectory()) {
				return;
			}
			if (debug) {
				System.out
						.println("ImportTask.executeFileList: dir = " + fileDir); //$NON-NLS-1$
			}
			String[] includedFiles = fl.getFiles(getProject());
			for (int i = 0; i < includedFiles.length; i++) {
				String filename = includedFiles[i];
				filesToImport.add(new File(fileDir, filename));
				if (debug) {
					System.out.println("ImportTask.executeFileList: add file " //$NON-NLS-1$
							+ new File(fileDir, filename).getAbsolutePath());
				}
			}
		}
	}

	// The method adding files included in fileset to filesToImport
	public void executeFileSet() {
		for (Iterator<FileSet> itFSets = filesets.iterator(); itFSets.hasNext();) {
			FileSet fs = (FileSet) itFSets.next();
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			File fileDir = ds.getBasedir();
			if (!fileDir.exists() || !fileDir.isDirectory()) {
				return;
			}
			if (debug) {
				System.out
						.println("ImportTask.executeFileSet: basedir = " + fileDir); //$NON-NLS-1$
			}

			String[] includedFiles = ds.getIncludedFiles();
			for (int i = 0; i < includedFiles.length; i++) {
				String filename = includedFiles[i];
				filesToImport.add(new File(fileDir, filename));
				if (debug) {
					System.out.println("ImportTask.executeFileSet: add file " //$NON-NLS-1$
							+ new File(fileDir, filename).getAbsolutePath());
				}
			}
		}
	}

	// The method executing import
	public void executeImport() {
		EclipseImportOperation importer = new EclipseImportOperation(new Path(
				"/" //$NON-NLS-1$
						+ projectName), new File(fromdir),
				FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite
					 * (java.lang.String)
					 */
					public String queryOverwrite(String pathString) {
						// Overwrite all files in the destination project
						return IOverwriteQuery.ALL;
					}
				}, filesToImport);

		importer.setCreateContainerStructure(true);
		importer.setOverwriteResources(true);

		importer.setCreateLog(createlog);
		importer.setOverwriteOlder(overwriteolder);

		try {
			importer.run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// do nothing if the thread is interrupted
		}
		if (createlog) {
			importer.createLogFile(new Path(fromdir).removeLastSegments(1)
					.toOSString());
		}
	}

	// The setter for the "fromdir" attribute
	public void setFromdir(String fromdir) {
		this.fromdir = fromdir;
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
