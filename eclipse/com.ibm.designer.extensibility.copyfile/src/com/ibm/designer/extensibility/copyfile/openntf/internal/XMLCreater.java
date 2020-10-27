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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

public class XMLCreater {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private String sourceClass = this.getClass().getName();

	private File xmlFile = null;
	private String[] folders = null;
	private String[] files = null;
	private String[] testFolders = null;
	private String[] testFiles = null;

	public XMLCreater(File xmlFile, String[] folders, String[] files) {
		this(xmlFile, folders, files, null, null);
	}
	
	public XMLCreater(File xmlFile, String[] folders, String[] files, String[] testFolders, String[] testFiles) {
		this.xmlFile = xmlFile;
		this.folders = folders;
		this.files = files;
		this.testFolders = testFolders;
		this.testFiles = testFiles;
	}

	public XMLCreater(File xmlFile, ArrayList<String> folders,
			ArrayList<String> files) {
		this(xmlFile, folders, files, null, null);
	}

	public XMLCreater(File xmlFile, ArrayList<String> folders,
			ArrayList<String> files, ArrayList<String> testFolders,
			ArrayList<String> testFiles) {
		this.xmlFile = xmlFile;
		this.folders = getStringArrayFromList(folders);
		this.files = getStringArrayFromList(files);
		this.testFolders = getStringArrayFromList(testFolders);
		this.testFiles = getStringArrayFromList(testFiles);
	}
	
	private String[] getStringArrayFromList(ArrayList<String> list){
		if (list != null && list.size() > 0) {
			Object[] objArr = list.toArray();
			String[] strArr = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				strArr[i] = (String) objArr[i];
			}
		    return strArr;
		}
		return null;
	}

	public boolean createXmlFile(boolean deleteIfExist) {
		String sourceMethod = "createXmlFile"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		if (xmlFile.exists()) {
			if (deleteIfExist) {
				xmlFile.delete();
			} else {
				logger.logp(Level.WARNING, sourceClass, sourceMethod,
						"xml file {0} exists", xmlFile.getAbsolutePath()); //$NON-NLS-1$
				logger.exiting(sourceMethod, sourceMethod, true);
				return true;
			}
		}
		try {
			xmlFile.createNewFile();
		} catch (IOException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			logger.exiting(sourceClass, sourceMethod, false);
			return false;
		}

		StringBuffer fileContents = new StringBuffer("<ImportList>"); //$NON-NLS-1$

		if (folders != null && folders.length > 0) {
			fileContents.append("\r\n\t<ImportFolders>"); //$NON-NLS-1$
			for (int i = 0; i < folders.length; i++) {
				fileContents
						.append("\r\n\t\t<") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_folder_TagName)
						.append(" ") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_folder_AttributeName)
						.append("=\"") //$NON-NLS-1$
						.append(folders[i]).append("\"/>"); //$NON-NLS-1$
			}
			fileContents.append("\r\n\t</ImportFolders>"); //$NON-NLS-1$
		} else {
			fileContents
					.append(
							"\r\n\t<!--  Example to show you how to import folders") //$NON-NLS-1$
					.append("\r\n\t<ImportFolders>") //$NON-NLS-1$
					.append("\r\n\t\t<ImportFolder folder=\"XPages\" />") //$NON-NLS-1$
					.append(
							"\r\n\t\t<ImportFolder folder=\"CustomControls\" />") //$NON-NLS-1$
					.append("\r\n\t</ImportFolders>").append("\r\n\t-->"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (files != null && files.length > 0) {
			fileContents.append("\r\n\t<ImportFiles>"); //$NON-NLS-1$
			for (int i = 0; i < files.length; i++) {
				fileContents
						.append("\r\n\t\t<") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_file_TagName)
						.append(" ") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_file_AttributeName)
						.append("=\"") //$NON-NLS-1$
						.append(files[i]) //$NON-NLS-1$
						.append("\"/>"); //$NON-NLS-1$
			}
			fileContents.append("\r\n\t</ImportFiles>"); //$NON-NLS-1$
		} else {
			fileContents
					.append(
							"\r\n\t<!--  Example to show you how to import files") //$NON-NLS-1$
					.append("\r\n\t<ImportFiles>") //$NON-NLS-1$
					.append(
							"\r\n\t\t<ImportFile file=\"CustomControls/ccXXX.xsp\" />") //$NON-NLS-1$
					.append(
							"\r\n\t\t<ImportFile file=\"CustomControls/ccXXX.xsp-config\" />") //$NON-NLS-1$
					.append("\r\n\t</ImportFiles>").append("\r\n\t-->"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (testFolders != null && testFolders.length > 0) {
			fileContents.append("\r\n\t<TestFolders>"); //$NON-NLS-1$
			for (int i = 0; i < testFolders.length; i++) {
				fileContents
						.append("\r\n\t\t<") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_testFolder_TagName)
						.append(" ") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_testFolder_AttributeName)
						.append("=\"") //$NON-NLS-1$
						.append(testFolders[i]).append("\"/>"); //$NON-NLS-1$
			}
			fileContents.append("\r\n\t</TestFolders>"); //$NON-NLS-1$
		}
		
		if (testFiles != null && testFiles.length > 0) {
			fileContents.append("\r\n\t<TestFiles>"); //$NON-NLS-1$
			for (int i = 0; i < testFiles.length; i++) {
				fileContents
						.append("\r\n\t\t<") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_testFile_TagName)
						.append(" ") //$NON-NLS-1$
						.append(
								OpenNTFConstants.default_importlist_testFile_AttributeName)
						.append("=\"") //$NON-NLS-1$
						.append(testFiles[i]) //$NON-NLS-1$
						.append("\"/>"); //$NON-NLS-1$
			}
			fileContents.append("\r\n\t</TestFiles>"); //$NON-NLS-1$
		} 

		fileContents.append("\r\n</ImportList>"); //$NON-NLS-1$

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(xmlFile));
			out.write(fileContents.toString());
		} catch (IOException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			logger.exiting(sourceClass, sourceMethod, false);
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
							.getMessage(), e);
				}
			}
		}

		logger.exiting(sourceClass, sourceMethod, true);
		return true;
	}

	public static void main(String[] args) {
		new XMLCreater(new File("d:\\temp\\importlist_1.xml"), //$NON-NLS-1$
				OpenNTFConstants.DEFAULT_IMPORT_FOLDERS, OpenNTFConstants.DEFAULT_IMPORT_FILES)
				.createXmlFile(true);

		String corefiles[] = new String[2];
		corefiles[0] = "CustomControls/ccXXX.xsp"; //$NON-NLS-1$
		corefiles[1] = "CustomControls/ccXXX.xsp-config"; //$NON-NLS-1$
		
		String testfolders[] = new String[2];
		testfolders[0] = "XPages"; //$NON-NLS-1$
		testfolders[1] = "Views"; //$NON-NLS-1$
		
		String testfiles[] = new String[2];
		testfiles[0] = "CustomControls/testXXX.xsp"; //$NON-NLS-1$
		testfiles[1] = "CustomControls/testXXX.xsp-config"; //$NON-NLS-1$

		new XMLCreater(new File("d:\\temp\\importlist_2.xml"), //$NON-NLS-1$
				null, corefiles, testfolders, testfiles).createXmlFile(true);
	}
}
