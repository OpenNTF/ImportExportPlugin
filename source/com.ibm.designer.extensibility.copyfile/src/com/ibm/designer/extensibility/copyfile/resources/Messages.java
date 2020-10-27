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

package com.ibm.designer.extensibility.copyfile.resources;

import org.eclipse.osgi.util.NLS;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.ibm.designer.extensibility.copyfile.resources.messages"; //$NON-NLS-1$

	// toolbar icons
	public static String OpenNTF_Import_Menu_Title;
	public static String OpenNTF_Export_Menu_Title;
	public static String FileResource_Import_Menu_Title;
	public static String FileResource_Export_Menu_Title;

	// export wizard page
	public static String FileExport_WindowTitle;
	public static String FileExport_PageTitle;
	public static String FileExport_description;

	// export wizard options
	public static String FileExport_overwriteAllExisting;
	public static String FileExport_overwriteOlderExisting;
	public static String FileExport_createLog;

	// import wizard page
	public static String FileImport_WindowTitle;
	public static String FileImport_PageTitle;
	public static String FileImport_description;
	public static String FileImport_error_hierarchy;
	public static String FileImport_error_folder_name;
	public static String FileImport_error_noOpenProjects;

	// import wizard options
	public static String FileImport_overwriteAllExisting;
	public static String FileImport_overwriteOlderExisting;
	public static String FileImport_createLog;

	// import openntf reusable control wizard page
	public static String OpenNTFImport_WindowTitle;
	public static String OpenNTFImport_PageTitle;
	public static String OpenNTFImport_description;
	public static String OpenNTFImport_select_controls;
	public static String OpenNTFImport_button_browse;
	public static String OpenNTFImport_label_intoDir;
	public static String OpenNTFImport_selectAppTitle;
	public static String OpenNTFImport_selectAppLabel;

	// import openntf dialog messages
	public static String OpenNTFImport_Start_title;
	public static String OpenNTFImport_Start_msg;
	public static String OpenNTFImport_Finish_title;
	public static String OpenNTFImport_Success_msg;
	public static String OpenNTFImport_Problems_msg;
	public static String OpenNTFImport_License_msg;
	public static String OpenNTFImport_TempDir_button;

	// import openntf reusable control wizard options
	public static String OpenNTFImport_importUnitTests;
	public static String OpenNTFImport_overwriteAllExisting;
	public static String OpenNTFImport_removetempfiles;
	public static String OpenNTFImport_createLog;

	// export openntf reusable control wizard page
	public static String OpenNTFExport_WindowTitle;
	public static String OpenNTFExport_PageTitle;
	public static String OpenNTFExport_description;
	public static String OpenNTFExport_label_select_core_files;
	public static String OpenNTFExport_label_select_test_files;
	public static String OpenNTFExport_button_select_all;
	public static String OpenNTFExport_button_deselect_all;
	public static String OpenNTFExport_label_to_dir;
	public static String OpenNTFExport_license_Apache;
	public static String OpenNTFExport_license_GNU_GPL3;
	public static String OpenNTFExport_license_GNU_LGPL3;
	public static String OpenNTFExport_license_GNU_Affero_GPL3;
	public static String OpenNTFExport_notice_default;
	public static String OpenNTFExport_otherfile;
	public static String OpenNTFExport_label_zip_file_name;
	public static String OpenNTFExport_label_select_license;
	public static String OpenNTFExport_button_edit_notice;
	public static String OpenNTFExport_button_browse;

	// export openntf dialog messages
	public static String OpenNTFExport_Success_title;
	public static String OpenNTFExport_Success_msg;
	public static String OpenNTFExport_Problems_title;
	public static String OpenNTFExport_Problems_msg;
	public static String OpenNTFExport_error_target_exist_as_file;
	public static String OpenNTFExport_query_create_target_dir;
	public static String OpenNTFExport_error_create_target_dir;
	public static String OpenNTFExport_dialog_create_target_dir_msg;
	public static String OpenNTFExport_dialog_create_target_dir_title;

	// warning messages on the import openntf resuable control wizard page
	public static String OpenNTFImport_error_getControlFromOpenNTF;
	public static String OpenNTFImport_error_noSource;
	public static String OpenNTFImport_warn_specify_application;

	// warning messages on the export openntf openntf resuable control wizard
	// page
	public static String OpenNTFExport_warn_specify_zip_name;
	public static String OpenNTFExport_warn_failed_copy_nsf;
	public static String OpenNTFExport_warn_select_multi_application;
	public static String OpenNTFExport_warn_zip_name_equal_project_name;

	// openntf logging error messages
	public static String OpenNTFImport_error_unzip;
	public static String OpenNTFImport_error_open_nsf;
	public static String OpenNTFImport_error_read_xml;
	public static String OpenNTFImport_error_remove_app;
	public static String OpenNTFImport_error_noNsf;
	public static String OpenNTFImport_error_export;

	// openntf logging warning messages
	public static String OpenNTFExport_warn_failed_create_xml;
	public static String OpenNTFImport_warn_noResource_inNsf;
	public static String OpenNTFImport_warn_noResource_inTempDir;

	// dialog messages
	public static String FileImport_importProblems;
	public static String FileExport_exportProblems;
	public static String FileImport_importProblems_msg;
	public static String FileExport_exportProblems_msg;
	public static String FileImport_importTask;
	public static String FileExport_exportTask;
	public static String FileImport_success;
	public static String FileImport_success_msg;
	public static String FileExport_success;
	public static String FileExport_success_msg;

	// designer logging error messages
	public static String error_import_file;
	public static String error_export_file;
	public static String error_create_logfile;
	public static String error_can_not_modify_file;
	public static String error_count_task;
	public static String error_save_designer_resource;
	public static String error_create_designer_resource;
	public static String error_merge_designer_resource;
	public static String error_open_eclipse_project;

	// export logging messages
	public static String log_export_failed;
	public static String log_export_succeed;
	public static String log_export_older;
	public static String log_export_updated;

	// import logging messages
	public static String log_import_failed;
	public static String log_import_notoverwrite;
	public static String log_import_succeed;
	public static String log_import_older;
	public static String log_import_updated;
	public static String log_import_new_created;
	public static String log_import_usercanecled;

	// overwrite query
	public static String OverwriteQuery_existsQuestion;
	public static String OverwriteQuery_overwriteNameAndPathQuestion;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
