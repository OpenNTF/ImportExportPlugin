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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.openntf.internal.MergeUtil;
import com.ibm.designer.extensibility.copyfile.resources.Constants;
import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class EclipseImportOperation extends ImportOperation {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private String sourceClass = this.getClass().getName();

	// create log
	private List<String> succeedList = new ArrayList<String>();
	private List<String> failedList = new ArrayList<String>();
	private List<String> notoverwriteList = new ArrayList<String>();
	private List<String> updatedList = new ArrayList<String>();
	private List<String> olderList = new ArrayList<String>();
	private List<String> newcreatedList = new ArrayList<String>();
	private boolean userCancel = false;

	// options value
	private boolean createLog = true;
	private boolean overwriteOlder = true;
	private boolean overwriteWithoutWarning = true;

	protected IOverwriteQuery overwriteCallback;

	// The constants for the overwrite 3 state
	private static final int OVERWRITE_NOT_SET = 0;
	private static final int OVERWRITE_NONE = 1;
	private static final int OVERWRITE_ALL = 2;
	private int overwriteState = OVERWRITE_NOT_SET;

	private String dbName = null;
	private IProgressMonitor monitor;
	private IPath sourcePath;
	private IPath destinatePath;
	private IImportStructureProvider provider;
	@SuppressWarnings("unchecked")
	private List selectedFiles;

	private List<IStatus> errorTable = new ArrayList<IStatus>(1); // IStatus

	public EclipseImportOperation(IPath containerPath, Object source,
			IImportStructureProvider provider,
			IOverwriteQuery overwriteImplementor, List<Object> filesToImport) {
		super(containerPath, source, provider, overwriteImplementor,
				filesToImport);
		this.provider = provider;
		this.destinatePath = containerPath;
		this.sourcePath = new Path(provider.getFullPath(source));
		this.selectedFiles = filesToImport;
		this.overwriteCallback = overwriteImplementor;
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "destinatePath = {0}, sourcePath = {1}", //$NON-NLS-1$
					new Object[] { destinatePath, sourcePath });
		}
		this.dbName = destinatePath.segment(0);
	}

	/*
	 * (non-Javadoc) Method declared on WorkbenchModifyOperation. Imports the
	 * specified file system objects from the file system.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void execute(IProgressMonitor progressMonitor) {
		this.monitor = progressMonitor;
		if (selectedFiles == null) {
		} else {
			int creationCount = selectedFiles.size();
			monitor.beginTask(Messages.FileImport_importTask, creationCount);
			try {
				importFileSystemObjects(selectedFiles);
			} catch (InterruptedException e) {
				// do nothing
			}
			monitor.done();
		}
	}

	/**
	 * Imports the specified file system objects into the workspace. If the
	 * import fails, adds a status object to the list to be returned by
	 * <code>getStatus</code>.
	 *
	 * @param filesToImport
	 *            the list of file system objects to import (element type:
	 *            <code>Object</code>)
	 * @throws InterruptedException
	 */
	private void importFileSystemObjects(List<Object> filesToImport)
			throws InterruptedException {
		IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(dbName);

		if (!eclipseProject.isOpen()) {
			try {
				eclipseProject.open(monitor);
			} catch (CoreException e) {
				logger.log(Level.SEVERE, Messages.error_open_eclipse_project);
				addError(e);
				return;
			}
		}

		Iterator<Object> filesEnum = filesToImport.iterator();
		while (filesEnum.hasNext()) {
			Object fileSystemObject = filesEnum.next();

			if (provider.isFolder(fileSystemObject)) {
				monitor.worked(1);
				continue;
			}
			importFile(fileSystemObject, eclipseProject);
			if (userCancel) {
				break;
			}
		}
	}

	/**
	 * Imports the specified file system object into the workspace. If the
	 * import fails, adds a status object to the list to be returned by
	 * <code>getResult</code>.
	 *
	 * @param fileObject
	 *            the file system object to be imported
	 * @param eclipseProject
	 *            target project
	 * @throws InterruptedException
	 */
	private void importFile(Object fileObject, IProject eclipseProject)
			throws InterruptedException {
		String sourceMethod = "importFile"; //$NON-NLS-1$

		String fileObjectPath = provider.getFullPath(fileObject);

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Importing " //$NON-NLS-1$
					+ fileObjectPath);
		}

		File sourceFile = new File(fileObjectPath);

		IPath relativePath = new Path(fileObjectPath.substring(2))
				.removeFirstSegments(sourcePath.segmentCount());
		relativePath = destinatePath.append(relativePath)
				.removeFirstSegments(1);
		IFile targetFile = eclipseProject.getFile(relativePath);

		if (relativePath.toString().equals(Constants.FILE_CLASSPATH)) {
			// Merge .classpath
			InputStream baseStream = null;
			InputStream resultStream = null;
			try {
				baseStream = targetFile.getContents();
				File resultFile = new MergeUtil().mergeClassPath(baseStream,
						sourceFile, null);
				resultStream = new FileInputStream(resultFile);
				targetFile.setContents(resultStream, true, true, monitor);
				resultFile.delete();
				addFileToLogList(succeedList, fileObjectPath);
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest(relativePath.toString()
							+ " has been successfully merged."); //$NON-NLS-1$
				}
			} catch (Exception e) {
				addFileToLogList(failedList, fileObjectPath);
				logger.logp(Level.SEVERE, sourceClass, sourceMethod,
						Messages.error_merge_designer_resource
								+ relativePath.toString()
								+ "\n" + e.getMessage(), //$NON-NLS-1$
						e);
				addError(e);
			} finally {
				if (baseStream != null) {
					try {
						baseStream.close();
					} catch (IOException e) {
						logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
								.getMessage(), e);
						addError(e);
					}
				}
				if (resultStream != null) {
					try {
						resultStream.close();
					} catch (IOException e) {
						logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
								.getMessage(), e);
						addError(e);
					}
				}
			}
		} else {
			// Import the whole content of other files
			long sourceFileTimeStamp = sourceFile.lastModified();
			InputStream istream = null;
			try {
				istream = new FileInputStream(sourceFile);
				if (!targetFile.exists()) {
					try {
						for (int i = relativePath.segmentCount() - 1; i > 0; i--) {
							IPath tempPath = relativePath.removeLastSegments(i);
							IFolder folder = eclipseProject.getFolder(tempPath);
							if (!folder.exists()) {
								folder.create(true, true, monitor);
							}
						}
						targetFile.create(istream, true, monitor);
						targetFile.setLocalTimeStamp(sourceFileTimeStamp);
					} catch (CoreException e) {
						addFileToLogList(failedList, fileObjectPath);
						monitor.worked(1);
						logger.logp(Level.SEVERE, sourceClass, sourceMethod,
								Messages.error_create_designer_resource
										+ relativePath.toString() + "\n" //$NON-NLS-1$
										+ e.getMessage(), e);
						addError(e);
						return;
					}
					addFileToLogList(newcreatedList, fileObjectPath);
				} else {
					// overwriteWithoutWarning is set to false by default for
					// import
					// from OpenNTF, while it is set to true by default for
					// import
					// from local file system
					if (!overwriteWithoutWarning) {
						if (overwriteState == OVERWRITE_NONE) {
							addFileToLogList(notoverwriteList, fileObjectPath);
							monitor.worked(1);
							if (logger.isLoggable(Level.FINEST)) {
								logger.finest("Not overwrite " //$NON-NLS-1$
										+ relativePath.toString());
							}
							return;
						} else if (overwriteState == OVERWRITE_NOT_SET) {
							if (!queryOverwrite(relativePath)) {
								addFileToLogList(notoverwriteList,
										fileObjectPath);
								monitor.worked(1);
								if (logger.isLoggable(Level.FINEST)) {
									logger.finest("Not overwrite " //$NON-NLS-1$
											+ relativePath.toString());
								}
								return;
							}
						}
					}
					// overwriteOlder is set to false for import from OpenNTF
					if (overwriteOlder) {
						long targetFileTimeStamp = targetFile
								.getLocalTimeStamp();

						if (targetFileTimeStamp == sourceFileTimeStamp) {
							addFileToLogList(updatedList, fileObjectPath);
							monitor.worked(1);
							if (logger.isLoggable(Level.FINEST)) {
								logger.finest(relativePath.toString()
										+ " is up to date."); //$NON-NLS-1$
							}
							return;
						} else if (targetFileTimeStamp > sourceFileTimeStamp) {
							addFileToLogList(olderList, fileObjectPath);
							monitor.worked(1);
							if (logger.isLoggable(Level.FINEST)) {
								logger.finest(relativePath.toString()
										+ " is newer."); //$NON-NLS-1$
							}
							return;
						}
					}
					targetFile.setContents(istream, true, true, monitor);
					targetFile.setLocalTimeStamp(sourceFileTimeStamp);
					addFileToLogList(succeedList, fileObjectPath);
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest(relativePath.toString()
								+ " has been successfully imported."); //$NON-NLS-1$
					}
				}
			} catch (FileNotFoundException e) {
				addFileToLogList(failedList, fileObjectPath);
				logger.logp(Level.SEVERE, sourceClass, sourceMethod,
						Messages.error_create_designer_resource
								+ relativePath.toString()
								+ "\n" + e.getMessage(), //$NON-NLS-1$
						e);
				addError(e);
			} catch (CoreException e) {
				addFileToLogList(failedList, fileObjectPath);
				logger.logp(Level.SEVERE, sourceClass, sourceMethod,
						Messages.error_save_designer_resource
								+ relativePath.toString()
								+ "\n" + e.getMessage(), //$NON-NLS-1$
						e);
				addError(e);
			} catch (OperationCanceledException e) {
				// User stop importing
				userCancel = true;

			} catch (Exception e) {
				// Catch any exceptions that Designer failed to handle with
				addFileToLogList(failedList, fileObjectPath);
				logger.logp(Level.SEVERE, sourceClass, sourceMethod,
						Messages.error_create_designer_resource
								+ relativePath.toString()
								+ "\n" + e.getMessage(), //$NON-NLS-1$
						e);
				addError(e);
			} finally {
				try {
					if (istream != null) {
						istream.close();
					}
				} catch (IOException e) {
					logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
							.getMessage(), e);
					addError(e);
				}
			}
		}
		monitor.worked(1);
		ModalContext.checkCanceled(monitor);
	}

	/**
	 * Queries the user whether the resource with the specified path should be
	 * overwritten by a file system object that is being imported.
	 *
	 * @param resourcePath
	 *            the workspace path of the resource that needs to be
	 *            overwritten
	 * @return <code>true</code> to overwrite, <code>false</code> to not
	 *         overwrite
	 * @exception OperationCanceledException
	 *                if canceled
	 */
	boolean queryOverwrite(IPath resourcePath)
			throws OperationCanceledException {
		String overwriteAnswer = overwriteCallback.queryOverwrite(resourcePath
				.makeRelative().toString());

		if (overwriteAnswer.equals(IOverwriteQuery.CANCEL)) {
			throw new OperationCanceledException("cancel"); //$NON-NLS-1$
		}

		if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
			return false;
		}

		if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
			this.overwriteState = OVERWRITE_NONE;
			return false;
		}

		if (overwriteAnswer.equals(IOverwriteQuery.ALL)) {
			this.overwriteState = OVERWRITE_ALL;
		}

		return true;
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

	public void setOverwriteWithoutWarning(boolean overwriteWithoutWarning) {
		this.overwriteWithoutWarning = overwriteWithoutWarning;
	}

	/**
	 * Create import log file
	 */
	public boolean createLogFile(String location) {
		if (!createLog) {
			return true;
		}
		String sourceMethod = "createLogFile"; //$NON-NLS-1$

		LogUtil logUtil = new LogUtil();
		File logFile = null;
		if (location == null || location.equals("")) { //$NON-NLS-1$
			logFile = logUtil.createLogFile(sourcePath.removeLastSegments(1),
					"Import"); //$NON-NLS-1$
		} else {
			logFile = logUtil.createLogFile(new Path(location), "Import"); //$NON-NLS-1$
		}

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("createLogFile: " + logFile.getAbsolutePath()); //$NON-NLS-1$
		}

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(logFile);
			if (!logUtil.writeListToLogStream(fileOutputStream, failedList,
					Messages.log_import_failed)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							newcreatedList, Messages.log_import_new_created)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							succeedList, Messages.log_import_succeed)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							olderList, Messages.log_import_older)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							updatedList, Messages.log_import_updated)
					|| !logUtil.writeListToLogStream(fileOutputStream,
							notoverwriteList, Messages.log_import_notoverwrite)
					|| (userCancel && !logUtil.writeStringToLogStream(
							fileOutputStream, Messages.log_import_usercanecled))) {
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
		newcreatedList.clear();

		return true;
	}

	/**
	 * Returns the status of the import operation. If there were any errors, the
	 * result is a status object containing individual status objects for each
	 * error. If there were no errors, the result is a status object with error
	 * code <code>OK</code>.
	 *
	 * @return the status
	 */
	@Override
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		IStatus status = new MultiStatus(ImportExportPlugin.PLUGIN_ID,
				IStatus.OK, errors, Messages.FileImport_importProblems, null);
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
