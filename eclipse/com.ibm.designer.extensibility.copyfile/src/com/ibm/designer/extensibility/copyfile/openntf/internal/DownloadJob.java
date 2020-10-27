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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;

public class DownloadJob extends WorkspaceJob {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private static String sourceClass = "com.ibm.designer.extensibility.copyfile.openntf.internal.DownloadJob"; //$NON-NLS-1$
	private URL url = null;
	private IPath downloadDir = null;
	private IPath downloadFilePath = null;
	private String fileName = null;
	private IProgressMonitor monitor;

	public DownloadJob(String name, URL url, IPath downloadDir, String fileName) {
		super(name);
		this.url = url;
		this.downloadDir = downloadDir;
		this.fileName = fileName;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		this.monitor = monitor;
		boolean result = downloadFromURL(url);
		if (result) {
			return Status.OK_STATUS;
		} else {
			return Status.CANCEL_STATUS;
		}
	}

	public boolean downloadFromURL(URL url) {
		String sourceMethod = "downloadFromURL"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);
		boolean result = true;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format("Download %s from URL %s to dir %s", //$NON-NLS-1$
					fileName, url.toString(), downloadDir));
		}
		downloadFilePath = downloadDir.append(fileName);
		if (downloadFilePath.toFile().exists()) {
			downloadFilePath.toFile().delete();
		}

		InputStream inStream = null;
		FileOutputStream foutStream = null;
		try {
			monitor.beginTask(url.toString(), 1000);
			inStream = url.openStream();
			foutStream = new FileOutputStream(downloadFilePath.toFile());

			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = inStream.read(buffer)) != -1) {
				foutStream.write(buffer, 0, byteread);
				monitor.worked(1);
			}
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			result = false;
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			result = false;
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			if (foutStream != null) {
				try {
					foutStream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
		monitor.done();
		logger.exiting(sourceClass, sourceMethod, result);
		return result;
	}

	public IPath getDownloadFilePath() {
		return downloadFilePath;
	}

}
