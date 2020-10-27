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

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.IOverwriteQuery;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.internal.EclipseExportOperation;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class ExportJob extends WorkspaceJob {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private static String sourceClass = "com.ibm.designer.extensibility.copyfile.openntf.internal.ExportJob"; //$NON-NLS-1$

	private List<Object> filesToExport = null;
	private String todir = null;

	public ExportJob(String name, List<Object> filesToExport, String todir) {
		super(name);
		this.filesToExport = filesToExport;
		this.todir = todir;
	}

	@SuppressWarnings("restriction")
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		String sourceMethod = "runInWorkspace"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

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

		exporter.setCreateLog(false);
		exporter.setOverwriteOlder(false);

		try {
			exporter.run(monitor);
		} catch (InterruptedException e) {
			// do nothing if the thread is interrupted
		}

		logger.exiting(sourceClass, sourceMethod, exporter.getStatus());
		return exporter.getStatus();
	}

}
