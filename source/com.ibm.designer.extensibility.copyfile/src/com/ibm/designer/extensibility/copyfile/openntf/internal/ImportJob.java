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

package com.ibm.designer.extensibility.copyfile.openntf.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.internal.EclipseImportOperation;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class ImportJob extends WorkspaceJob {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private static String sourceClass = "com.ibm.designer.extensibility.copyfile.openntf.internal.ImportJob"; //$NON-NLS-1$

	private String projectName = null;
	private List<Object> filesToImport = null;
	private String fromdir = null;
	private boolean createLog = false;
	private boolean overwriteWithoutWarning = true; // overwrite all existing

	public ImportJob(String name, String projectName,
			List<Object> filesToImport, String fromdir, boolean createLog,
			boolean overwriteWithoutWarning) {
		super(name);
		this.projectName = projectName;
		this.filesToImport = filesToImport;
		this.fromdir = fromdir;
		this.createLog = createLog;
		this.overwriteWithoutWarning = overwriteWithoutWarning;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		String sourceMethod = "runInWorkspace"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		EclipseImportOperation importer = new EclipseImportOperation(new Path(
				"/" //$NON-NLS-1$
						+ projectName), new File(fromdir),
				FileSystemStructureProvider.INSTANCE, new OverwriteQuery(),
				filesToImport);

		importer.setCreateContainerStructure(true);
		importer.setOverwriteResources(false);
		importer.setOverwriteOlder(false);
		importer.setOverwriteWithoutWarning(overwriteWithoutWarning);
		importer.setCreateLog(createLog);

		try {
			importer.run(monitor);
		} catch (InvocationTargetException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
		} catch (InterruptedException e) {
			// do nothing if the thread is interrupted
		}

		if (createLog) {
			importer.createLogFile(PathUtil.getImportLogsDir().toOSString());
		}

		logger.exiting(sourceClass, sourceMethod, importer.getStatus());
		return importer.getStatus();
	}
}
