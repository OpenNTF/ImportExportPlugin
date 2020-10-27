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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;

public class ZipJob extends WorkspaceJob {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private static String sourceClass = "com.ibm.designer.extensibility.copyfile.openntf.internal.ZipJob"; //$NON-NLS-1$

	private File sourceDir = null;
	private File zipFile = null;

	public ZipJob(String name, File sourceDir, File zipFile) {
		super(name);
		this.sourceDir = sourceDir;
		this.zipFile = zipFile;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		zipDirectory(sourceDir, zipFile);
		return Status.OK_STATUS;
	}

	public static void addToZipDirectory(ZipOutputStream out, File sourceDir,
			int relativeCount) throws IOException {
		String sourceMethod = "addToZipDirectory"; //$NON-NLS-1$
		if (sourceDir.isDirectory()) {
			String[] entries = sourceDir.list();
			for (String entry : entries) {
				File f = new File(sourceDir, entry);
				addToZipDirectory(out, f, relativeCount);
			}
		} else {
			byte[] buffer = new byte[4096]; // Create a buffer for copying
			int bytesRead;
			FileInputStream finStream = null;
			try {
				finStream = new FileInputStream(sourceDir);
				ZipEntry entry = new ZipEntry(new Path(sourceDir
						.getAbsolutePath()).removeFirstSegments(relativeCount)
						.toString().substring(2)); // Make a ZipEntry
				out.putNextEntry(entry); // Store entry
				while ((bytesRead = finStream.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead); // Stream to read file
				}
			} finally {
				if (finStream != null) {
					try {
						finStream.close();
					} catch (IOException e) {
						logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
								.getMessage(), e);
					}
				}
			}
		}
	}

	public static boolean zipDirectory(File sourceDir, File zipFile) {
		String sourceMethod = "zipDirectory"; //$NON-NLS-1$
		boolean result = true;
		logger.entering(sourceClass, sourceMethod);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format(
					"Compress dir %s to zip file %s", //$NON-NLS-1$
					sourceDir.getAbsolutePath(), zipFile.getAbsolutePath()));
		}

		if (!sourceDir.isDirectory()) {
			logger.logp(Level.WARNING, sourceClass, sourceMethod,
					"Not a directory: " + sourceDir); //$NON-NLS-1$
			result = false;
		} else {

			String[] entries = sourceDir.list();
			ZipOutputStream zipOutStream = null;
			int relativeCount = new Path(sourceDir.getAbsolutePath())
					.segmentCount();

			try {
				zipOutStream = new ZipOutputStream(
						new FileOutputStream(zipFile));
				for (String entry : entries) {
					File f = new File(sourceDir, entry);
					addToZipDirectory(zipOutStream, f, relativeCount);
				}

			} catch (FileNotFoundException e) {
				logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
						.getMessage(), e);
				result = false;
			} catch (IOException e) {
				logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
						.getMessage(), e);
				result = false;
			} finally {
				if (zipOutStream != null) {
					try {
						zipOutStream.close();
					} catch (IOException e) {
						logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
								.getMessage(), e);
					}
				}
			}
		}
		logger.exiting(sourceClass, sourceMethod, result);
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		zipDirectory(new File("d:\\nsf\\test"), new File("d:\\nsf\\test.zip")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
