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

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

public class XMLReader {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private String sourceClass = this.getClass().getName();

	private List<String> folder_list = new ArrayList<String>();
	private List<String> file_list = new ArrayList<String>();
	private IProject eclipseProject = null;
	private File xmlFile = null;
	private boolean importUnitTest = false;

	public XMLReader(IProject eclipseProject, File xmlFile,
			boolean importUnitTest) {
		this.eclipseProject = eclipseProject;
		this.xmlFile = xmlFile;
		this.importUnitTest = importUnitTest;
	}

	public XMLReader(IProject eclipseProject, File xmlFile) {
		this(eclipseProject, xmlFile, false);
	}

	private boolean readXMLFile(File xmlFile) {
		String sourceMethod = "readXMLFile"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			logger.exiting(sourceClass, sourceMethod, false);
			return false;
		}

		Document doc = null;
		try {
			doc = db.parse(xmlFile);
		} catch (SAXException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			logger.exiting(sourceClass, sourceMethod, false);
			return false;
		} catch (IOException e) {
			logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					e.getMessage(), e);
			logger.exiting(sourceClass, sourceMethod, false);
			return false;
		}

		// parse xml
		Element root = doc.getDocumentElement();

		// Get core files
		NodeList folders = root
				.getElementsByTagName(OpenNTFConstants.default_importlist_folder_TagName);
		for (int i = 0; i < folders.getLength(); i++) {
			Element folder = (Element) folders.item(i);
			folder_list
					.add(folder
							.getAttribute(OpenNTFConstants.default_importlist_folder_AttributeName));
		}

		NodeList files = root
				.getElementsByTagName(OpenNTFConstants.default_importlist_file_TagName);
		for (int i = 0; i < files.getLength(); i++) {
			Element file = (Element) files.item(i);
			file_list
					.add(file
							.getAttribute(OpenNTFConstants.default_importlist_file_AttributeName));
		}

		// Get unit test files
		if (importUnitTest) {
			folders = root
					.getElementsByTagName(OpenNTFConstants.default_importlist_testFolder_TagName);
			for (int i = 0; i < folders.getLength(); i++) {
				Element folder = (Element) folders.item(i);
				folder_list
						.add(folder
								.getAttribute(OpenNTFConstants.default_importlist_testFolder_AttributeName));
			}
			files = root
					.getElementsByTagName(OpenNTFConstants.default_importlist_testFile_TagName);
			for (int i = 0; i < files.getLength(); i++) {
				Element file = (Element) files.item(i);
				file_list
						.add(file
								.getAttribute(OpenNTFConstants.default_importlist_testFile_AttributeName));
			}
		}

		logger.exiting(sourceClass, sourceMethod, true);
		return true;
	}

	/**
	 *
	 * @return
	 */
	public void getDefaultExportList() {
		String[] folders = OpenNTFConstants.DEFAULT_IMPORT_FOLDERS;
		String[] files = OpenNTFConstants.DEFAULT_IMPORT_FILES;
		for (String folder : folders) {
			folder_list.add(folder);
		}
		for (String file : files) {
			file_list.add(file);
		}
	}

	public Vector<Object> getExportList() {
		String sourceMethod = "getExportList"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		Vector<Object> filesToExport = new Vector<Object>();
		if (xmlFile == null || !xmlFile.exists() || !readXMLFile(xmlFile)) {
			getDefaultExportList();
		}

		for (String element : folder_list) {
			IPath path = new Path(element);
			IFolder folder = eclipseProject.getFolder(path);
			if (folder.exists()) {
				filesToExport.add(folder);
			} else {
				logger.logp(Level.WARNING, sourceClass, sourceMethod,
						"folder {0} does not exit", path.toString()); //$NON-NLS-1$
			}
		}

		for (String element : file_list) {
			IPath path = new Path(element);
			IResource file = eclipseProject.getFile(path);
			if (file.exists() && file instanceof IFile) {
				filesToExport.add(file);
			} else {
				logger.logp(Level.WARNING, sourceClass, sourceMethod,
						"file {0} does not exit", path.toString()); //$NON-NLS-1$
			}
		}

		logger.exiting(sourceClass, sourceMethod);
		return filesToExport;
	}

	public static void main(String[] args) {

	}
}
