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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.resources.Messages;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class UnZipJob extends WorkspaceJob {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private static String sourceClass = "com.ibm.designer.extensibility.copyfile.openntf.internal.UnZipJob"; //$NON-NLS-1$

	private IPath zipPath = null;
	private IPath destPath = null;
	private IPath nsfPath = null;
	private IPath xmlPath = null;
	private IProgressMonitor monitor;

	// when decompress .jar file

	public UnZipJob(String name, IPath zipPath, IPath destPath) {
		super(name);
		this.zipPath = zipPath;
		this.destPath = destPath;
	}

	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		this.monitor = monitor;
		boolean result = decompress();
		if (result) {
			return Status.OK_STATUS;
		} else {
			return Status.CANCEL_STATUS;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean decompress() {
		String sourceMethod = "decompress"; //$NON-NLS-1$
		boolean result = true;
		logger.entering(sourceClass, sourceMethod);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format("Decompress zip file %s to dir %s", //$NON-NLS-1$
					zipPath.toString(), destPath.toString()));
		}

		monitor.beginTask(zipPath.toFile().getName(), 10);
		byte b[] = new byte[1024];
		int length;
		ZipFile zipFile = null;
		OutputStream outputStream = null;
		InputStream inputStream = null;

		try {
			zipFile = new ZipFile(zipPath.toFile());
			Enumeration enumeration = zipFile.entries();
			ZipEntry zipEntry = null;

			while (enumeration.hasMoreElements()) {
				zipEntry = (ZipEntry) enumeration.nextElement();
				String name = zipEntry.getName();
				if (name.endsWith(".nsf")) { //$NON-NLS-1$
					nsfPath = destPath.append(name);
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("nsfPath = " + nsfPath.toString());//$NON-NLS-1$
					}
				} else if (name
						.equalsIgnoreCase(OpenNTFConstants.default_importlist_xmlfile)) {
					xmlPath = destPath.append(name);
					if (logger.isLoggable(Level.FINER)) {
						logger.finer("xmlPath = " + xmlPath.toString());//$NON-NLS-1$
					}
				}

				File targetFile = destPath.append(name).toFile();
				if (zipEntry.isDirectory()) {
					targetFile.mkdirs();
				} else {
					if (!targetFile.getParentFile().exists())
						targetFile.getParentFile().mkdirs();
					inputStream = zipFile.getInputStream(zipEntry);
					outputStream = new FileOutputStream(targetFile);
					while ((length = inputStream.read(b)) > 0)
						outputStream.write(b, 0, length);
				}
				monitor.worked(1);
			}
		} catch (IOException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					Messages.OpenNTFImport_error_unzip + "\n" + e.getMessage(), //$NON-NLS-1$
					e);
			result = false;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		monitor.done();
		logger.exiting(sourceClass, sourceMethod, result);
		return result;
	}

	public IPath getNsfPath() {
		return nsfPath;
	}

	public IPath getXmlPath() {
		return xmlPath;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
