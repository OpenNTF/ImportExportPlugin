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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.ibm.designer.domino.ide.resources.extensions.DesignerException;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ui.commons.extensions.DesignerResource;
import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.internal.MyFileUtil;
import com.ibm.designer.extensibility.copyfile.openntf.beans.Control;
import com.ibm.designer.extensibility.copyfile.resources.Messages;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

public class ImportThread implements Runnable {

	private static final Logger logger = ImportExportPlugin.getLogger();
	private static IPath tempPath = null;
	private static int count;
	private static boolean importUnitTest;
	private static boolean createLogFile;
	private static boolean overwriteWithoutWarning;
	private static boolean removeTempFile;
	private static String destinationProject;
	public static ArrayList<String> succeed_control_list = new ArrayList<String>();
	public static ArrayList<String> fail_control_list = new ArrayList<String>();

	private Control ctl = null;

	private IPath zipPath = null;
	private IPath nsfPath = null;
	private IPath destUnzipPath = null;
	private String sourceProjectName = null;
	private IPath xmlPath = null;
	private IPath downloadFilePath = null;
	private File sourceDir = null;
	private MyFileUtil myFileUtil = new MyFileUtil();

	public ImportThread(Control ctl) {
		this.ctl = ctl;
	}

	public void run() {
		tempPath = PathUtil.getImportTempDir();

		// Download .zip files from OpenNTF website
		String fileName = ctl.getZipFile().substring(
				ctl.getZipFile().lastIndexOf("=") + 1); //$NON-NLS-1$
		boolean result = downloadFromOpenNTF(ctl.getZipFile(), tempPath,
				fileName);
		zipPath = downloadFilePath;

		// Decompress the .zip file
		if (result) {
			result = decompressZipFile();
		}

		// Export resources from the downloaded .nsf to local disk
		if (result) {
			result = exportResourcesFromControlToLocal();
		}

		// Import resources from local disk to Designer Application
		if (result) {
			result = importResourcesFromLocaltoApplication();
		}

		// Remove temporary files
		removeTempFiles();

		// Notify the user when all controls are imported
		if (determineImportAllCompletion(result)) {
			showDialog();

			// Clean up
			succeed_control_list.clear();
			fail_control_list.clear();
		}
	}

	private boolean downloadFromOpenNTF(String urlStr, IPath downloadDir,
			String fileName) {
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.getMessage() + " url = " + urlStr, e); //$NON-NLS-1$
			return false;
		}
		DownloadJob downloadJob = new DownloadJob(
				"DownloadJob", url, downloadDir, fileName); //$NON-NLS-1$
		downloadJob.schedule();
		try {
			downloadJob.join();
		} catch (InterruptedException e) {
			// do nothing if the thread is interrupted
			return false;
		}
		downloadFilePath = downloadJob.getDownloadFilePath();
		return (downloadJob.getResult() == Status.OK_STATUS);
	}

	private boolean decompressZipFile() {
		// Unzip the .zip file
		destUnzipPath = zipPath.removeFileExtension();
		if (destUnzipPath.toFile().exists()) {
			myFileUtil.delFolder(destUnzipPath.toFile());
		}
		UnZipJob unZipJob = new UnZipJob("UnZipJob", zipPath, destUnzipPath); //$NON-NLS-1$
		unZipJob.schedule();
		try {
			unZipJob.join();
		} catch (InterruptedException e) {
			// do nothing if the thread is interrupted
			return false;
		}

		nsfPath = unZipJob.getNsfPath();
		xmlPath = unZipJob.getXmlPath();

		if (nsfPath == null) {
			logger.log(Level.WARNING, Messages.OpenNTFImport_error_noNsf,
					zipPath);
			return false;
		}
		return (unZipJob.getResult() == Status.OK_STATUS);
	}

	private boolean exportResourcesFromControlToLocal() {
		DesignerProject designerProject = null;
		// Open the nsf file in workspace
		try {
			designerProject = DesignerResource.openDesignerProject("Local", nsfPath //$NON-NLS-1$
					.toOSString());
		} catch (DesignerException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
		IProject eclipseProject = designerProject.getProject();
		sourceProjectName = eclipseProject.getName();

		// Get xml file
		File xmlFile = null;
		if (xmlPath != null) {
			xmlFile = xmlPath.toFile();
		} else {
			if (ctl.getHasImportlist()) {
				String url = ctl.getImportListUrl();
				downloadFromOpenNTF(url, destUnzipPath,
						OpenNTFConstants.default_importlist_xmlfile);
				xmlFile = downloadFilePath.toFile();
			}
		}

		// Export resources from nsf database to the temp folder
		sourceDir = new File(tempPath.toFile(), sourceProjectName);
		if (sourceDir.exists()) {
			myFileUtil.delFolder(sourceDir);
		}
		Vector<Object> filesToExport = new XMLReader(eclipseProject, xmlFile,
				importUnitTest).getExportList();
		if (filesToExport != null && filesToExport.size() > 0) {
			ExportJob exportJob = new ExportJob("ExportJob", filesToExport, //$NON-NLS-1$
					tempPath.toOSString());
			exportJob.schedule();
			try {
				exportJob.join();
			} catch (InterruptedException e) {
				// do nothing if the thread is interrupted
				return false;
			}
			try {
				eclipseProject.delete(false, true, new NullProgressMonitor());
			} catch (CoreException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			if (!exportJob.getResult().isOK()) {
				return false;
			}
		} else {
			logger.log(Level.WARNING,
					Messages.OpenNTFImport_warn_noResource_inNsf, nsfPath);
			return false;
		}
		
		if (!sourceDir.exists() || !sourceDir.isDirectory()) {
			logger.log(Level.WARNING, Messages.OpenNTFImport_error_export,
					nsfPath);
			return false;
		}

		return true;
	}

	private boolean importResourcesFromLocaltoApplication() {
		List<Object> filesToImport = null;

		filesToImport = myFileUtil.getAllFilesList(sourceDir);

		if (filesToImport != null && filesToImport.size() > 0) {
			ImportJob importJob = new ImportJob(
					"Import " + sourceProjectName, //$NON-NLS-1$
					destinationProject, filesToImport, sourceDir
							.getAbsolutePath(), createLogFile,
					overwriteWithoutWarning);
			importJob.schedule();
			try {
				importJob.join();
			} catch (InterruptedException e) {
				// do nothing if the thread is interrupted
				return false;
			}
			if (!importJob.getResult().isOK()) {
				return false;
			}
		} else {
			logger.log(Level.WARNING,
					Messages.OpenNTFImport_warn_noResource_inTempDir, nsfPath);
			return false;
		}
		return true;
	}

	private void removeTempFiles() {
		if (removeTempFile) {
			// Do not remove the extracted files, users have to read the LICENSE
			// and NOTICE file.
			// Remove the .zip file
			if (zipPath != null) {
				zipPath.toFile().delete();
			}
			// remove the exported files
			if (sourceDir != null) {
				myFileUtil.delFolder(sourceDir);
			}
		}
	}

	synchronized private boolean determineImportAllCompletion(boolean success) {
		if (success) {
			succeed_control_list.add(ctl.getName());
		} else {
			fail_control_list.add(ctl.getName());
		}
		count--;
		if (count == 0) {
			return true;
		} else {
			return false;
		}
	}

	private void showDialog() {
		// run in syncExec because ImportThread is not running in the UI thread.
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				StringBuffer msgBuf = new StringBuffer();
				if (succeed_control_list.size() > 0) {
					msgBuf.append(NLS
							.bind(Messages.OpenNTFImport_Success_msg,
									succeed_control_list.toString(),
									destinationProject));
				}
				if (fail_control_list.size() > 0) {
					msgBuf.append(NLS.bind(Messages.OpenNTFImport_Problems_msg,
							fail_control_list.toString()));
				}
				msgBuf
						.append("\n\n").append(Messages.OpenNTFImport_License_msg); //$NON-NLS-1$

				ImportFinishMessageDialog dialog = new ImportFinishMessageDialog(
						null, Messages.OpenNTFImport_Finish_title, null, msgBuf
								.toString(), MessageDialog.INFORMATION,
						new String[] { Messages.OpenNTFImport_TempDir_button,
								IDialogConstants.OK_LABEL }, 1, 0);
				// ok is the default
				dialog.open();
			}
		});
	}

	public static void setCount(int count) {
		ImportThread.count = count;
	}

	public static void setDestinationProject(String destinationProject) {
		ImportThread.destinationProject = destinationProject;
	}

	public static void setCreateLogFile(boolean createLogFile) {
		ImportThread.createLogFile = createLogFile;
	}

	public static void setOverwriteWithoutWarning(
			boolean overwriteWithoutWarning) {
		ImportThread.overwriteWithoutWarning = overwriteWithoutWarning;
	}

	public static void setRemoveTempFile(boolean removeTempFile) {
		ImportThread.removeTempFile = removeTempFile;
	}

	public static void setImportUnitTest(boolean importUnitTest) {
		ImportThread.importUnitTest = importUnitTest;
	}

}
