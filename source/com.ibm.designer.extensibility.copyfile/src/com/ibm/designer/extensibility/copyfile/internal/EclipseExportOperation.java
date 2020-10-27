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

package com.ibm.designer.extensibility.copyfile.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExporter;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.openntf.internal.MergeUtil;
import com.ibm.designer.extensibility.copyfile.resources.Constants;
import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
@SuppressWarnings("restriction")
public class EclipseExportOperation extends FileSystemExportOperation {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private String sourceClass = this.getClass().getName();

	private List<String> succeedList = new ArrayList<String>();
	private List<String> failedList = new ArrayList<String>();
	private List<String> updatedList = new ArrayList<String>();
	private List<String> olderList = new ArrayList<String>();

	private FileSystemExporter export = new FileSystemExporter();
	private IProgressMonitor monitor;
	@SuppressWarnings("unchecked")
	private List resourcesToExport;
	private IResource resource;
	private IPath destinationPath;

	private boolean createLog = true;
	private boolean overwriteOlder = true;

	private List<IStatus> errorTable = new ArrayList<IStatus>(1); // IStatus

	/**
	 * Create an instance of this class. Use this constructor if you wish to
	 * export specific resources with a common parent resource (affects
	 * container directory creation)
	 */
	public EclipseExportOperation(IResource res, List<Object> resources,
			String destinationPath, IOverwriteQuery overwriteImplementor) {
		super(res, resources, destinationPath, overwriteImplementor);
		this.resource = res;
		this.resourcesToExport = resources;
		this.destinationPath = new Path(destinationPath);
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "destinationPath = {0}", //$NON-NLS-1$
					destinationPath);
		}
	}

	/**
	 * Export the resources that were previously specified for export (or if a
	 * single resource was specified then export it recursively)
	 */
	public void run(IProgressMonitor progressMonitor)
			throws InterruptedException {
		this.monitor = progressMonitor;
		int totalWork = IProgressMonitor.UNKNOWN;
		try {
			if (resourcesToExport == null) {
				totalWork = countChildrenOf(resource);
			} else {
				totalWork = countSelectedResources();
			}
		} catch (CoreException e) {
			// Should not happen
			String sourceMethod = "run"; //$NON-NLS-1$
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			addError(e);
		}

		try {
			monitor.beginTask(Messages.FileExport_exportTask, totalWork);
			super.run(progressMonitor);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Export the passed file to the specified location
	 * 
	 * @param sourceFile
	 *            org.eclipse.core.resources.IFile
	 * @param location
	 *            org.eclipse.core.runtime.IPath
	 */
	protected void exportFile(IFile sourceFile, IPath location)
			throws InterruptedException {
		String sourceMethod = "exportFile"; //$NON-NLS-1$

		IPath fullPath = location.append(sourceFile.getName());
		monitor.subTask(sourceFile.getFullPath().toString());
		String properPathString = fullPath.toOSString();

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Exporting " //$NON-NLS-1$
					+ properPathString);
		}

		File targetFile = new File(properPathString);
		long resourceTimeStamp = sourceFile.getLocalTimeStamp();

		if (targetFile.exists()) {
			// Merge .classpath
			if (targetFile.getName().equals(Constants.FILE_CLASSPATH)) {
				InputStream baseStream = null;
				try {
					baseStream = sourceFile.getContents();
					new MergeUtil().mergeClassPath(baseStream, targetFile,
							targetFile);
					addFileToLogList(succeedList, properPathString);
					monitor.worked(1);
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest(properPathString
								+ "has been successfully merged."); //$NON-NLS-1$
					}
				} catch (Exception e) {
					addFileToLogList(failedList, properPathString);
					logger.logp(Level.SEVERE, sourceClass, sourceMethod,
							Messages.error_export_file + properPathString
									+ "\n" //$NON-NLS-1$
									+ e.getMessage(), e);
					addError(e);
				} finally {
					if (baseStream != null) {
						try {
							baseStream.close();
						} catch (IOException e) {
							logger.logp(Level.SEVERE, sourceClass,
									sourceMethod, e.getMessage(), e);
							addError(e);
						}
					}
				}
				return;
			}

			// Export the whole content of other files
			if (overwriteOlder) {
				long targetFileTimeStamp = targetFile.lastModified();
				if (targetFileTimeStamp == resourceTimeStamp) {
					addFileToLogList(updatedList, properPathString);
					monitor.worked(1);
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest(properPathString + "is up to date."); //$NON-NLS-1$
					}
					return;
				} else if (targetFileTimeStamp > resourceTimeStamp) {
					addFileToLogList(olderList, properPathString);
					monitor.worked(1);
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest(properPathString + "is newer."); //$NON-NLS-1$
					}
					return;
				}
			}
		}

		try {
			export.write(sourceFile, fullPath);
			targetFile.setLastModified(resourceTimeStamp);
			addFileToLogList(succeedList, properPathString);
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest(properPathString
						+ "has been successfully exported."); //$NON-NLS-1$
			}
		} catch (IOException e) {
			addFileToLogList(failedList, properPathString);
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					Messages.error_export_file + properPathString + "\n" //$NON-NLS-1$
							+ e.getMessage(), e);
			addError(e);
		} catch (CoreException e) {
			addFileToLogList(failedList, properPathString);
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					Messages.error_export_file + properPathString + "\n" //$NON-NLS-1$
							+ e.getMessage(), e);
			addError(e);
		}
		monitor.worked(1);
		ModalContext.checkCanceled(monitor);
	}

	private void addFileToLogList(List<String> logList, String resourcePath) {
		if (createLog) {
			logList.add(resourcePath);
		}
	}

	public void setCreateLog(boolean createExportLog) {
		this.createLog = createExportLog;
	}

	public void setOverwriteOlder(boolean overwriteOlder) {
		this.overwriteOlder = overwriteOlder;
	}

	/**
	 * Create export log file
	 */
	public boolean createLogFile(String location) {
		if (!createLog) {
			return true;
		}
		String sourceMethod = "createLogFile"; //$NON-NLS-1$

		LogUtil logUtil = new LogUtil();
		File logFile = null;
		if (location == null || location.equals("")) { //$NON-NLS-1$
			logFile = logUtil.createLogFile(destinationPath, "Export"); //$NON-NLS-1$
		} else {
			logFile = logUtil.createLogFile(new Path(location), "Export"); //$NON-NLS-1$
		}

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("createLogFile: " + logFile.getAbsolutePath()); //$NON-NLS-1$
		}

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(logFile);
			if (!logUtil.writeListToLogStream(fileOutputStream, failedList,
					Messages.log_export_failed)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							succeedList, Messages.log_export_succeed)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							olderList, Messages.log_export_older)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							updatedList, Messages.log_export_updated)) {
				logger.logp(Level.SEVERE, sourceClass, sourceMethod,
						Messages.error_create_logfile);
				return false;
			}
		} catch (FileNotFoundException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					Messages.error_create_logfile + "\n" + e.getMessage(), e); //$NON-NLS-1$
			addError(e);
			return false;
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
						.getMessage(), e);
				addError(e);
			}
		}
		succeedList.clear();
		failedList.clear();
		updatedList.clear();
		olderList.clear();

		return true;
	}

	/**
	 * Returns the status of the operation. If there were any errors, the result
	 * is a status object containing individual status objects for each error.
	 * If there were no errors, the result is a status object with error code
	 * <code>OK</code>.
	 * 
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		IStatus status = new MultiStatus(ImportExportPlugin.PLUGIN_ID,
				IStatus.OK, errors, Messages.FileExport_exportProblems, null);
		if (status.isOK()) {
			return super.getStatus();
		} else {
			return status;
		}
	}

	/**
	 * Add a new entry to the error table with the passed information
	 */
	protected void addError(Throwable e) {
		errorTable.add(new Status(IStatus.ERROR, ImportExportPlugin.PLUGIN_ID,
				0, e.getMessage(), e));
	}
}
