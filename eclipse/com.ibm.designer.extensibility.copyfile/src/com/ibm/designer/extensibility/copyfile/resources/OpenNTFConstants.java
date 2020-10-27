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
package com.ibm.designer.extensibility.copyfile.resources;

public class OpenNTFConstants {
	public static final String TEMP_FOLDER = "temp"; //$NON-NLS-1$

	// xml file
	public static final String default_importlist_xmlfile = "importlist.xml"; //$NON-NLS-1$

	public static final String default_importlist_folder_TagName = "ImportFolder"; //$NON-NLS-1$
	public static final String default_importlist_folder_AttributeName = "folder"; //$NON-NLS-1$

	public static final String default_importlist_file_TagName = "ImportFile"; //$NON-NLS-1$
	public static final String default_importlist_file_AttributeName = "file"; //$NON-NLS-1$

	public static final String default_importlist_testFolder_TagName = "TestFolder"; //$NON-NLS-1$
	public static final String default_importlist_testFolder_AttributeName = "folder"; //$NON-NLS-1$

	public static final String default_importlist_testFile_TagName = "TestFile"; //$NON-NLS-1$
	public static final String default_importlist_testFile_AttributeName = "file"; //$NON-NLS-1$

	// NOTICE
	public static final String NOTICE_NAME = "NOTICE"; //$NON-NLS-1$
	public static final String NOTICE_LOCATION = "/files/NOTICE"; //$NON-NLS-1$

	// LICENSE
	public static final String LICENSE_NAME = "LICENSE"; //$NON-NLS-1$
	public static final String LICENSE_DIR = "/files"; //$NON-NLS-1$

	// other files
	public static final String FILE_SEPARATOR_WRITE = "\n"; //$NON-NLS-1$
	public static final String FILE_SEPARATOR_READ = "\r\n"; //$NON-NLS-1$

	// default import folders and files
	public static final String[] DEFAULT_IMPORT_FOLDERS = {
			Constants.FOLDER_XPAGES, Constants.FOLDER_CUSTOMCONTROLS,
			Constants.FOLDER_FORMS, Constants.FOLDER_VIEWS,
			Constants.FOLDER_FOLDERS, Constants.FOLDER_FRAMESETS,
			Constants.FOLDER_PAGES, Constants.FOLDER_SHAREDELEMENTS,
			Constants.FOLDER_RESOURCES, Constants.FOLDER_CODE,
			Constants.FOLDER_WEBCONTENT };

	public static final String[] DEFAULT_IMPORT_FILES = { Constants.FILE_CLASSPATH };

	// toolbar icons
	public static final String ICON_DIR = "icons/"; //$NON-NLS-1$
	public static final String IMPORT_OPENNTF_ICON = "openntf_import.png"; //$NON-NLS-1$
	public static final String EXPORT_OPENNTF_ICON = "openntf_export.png"; //$NON-NLS-1$
	public static final String IMPORT_RESOURCE_ICON = "filesystem_import.png"; //$NON-NLS-1$
	public static final String EXPORT_RESOURCE_ICON = "filesystem_export.png"; //$NON-NLS-1$
	public static final String TEMP_FOLDER_ICON = "folder_page.png"; //$NON-NLS-1$

	// Zip file URL prefix
	public static final String ZIP_FILE_URL_PREFIX = "http://www.openntf.org/Projects/pmt.nsf/downloadcounter?openagent&project="; //$NON-NLS-1$
}
