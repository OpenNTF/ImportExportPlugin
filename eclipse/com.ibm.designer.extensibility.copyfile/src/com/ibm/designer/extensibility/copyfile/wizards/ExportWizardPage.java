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
package com.ibm.designer.extensibility.copyfile.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.internal.EclipseExportOperation;
import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
@SuppressWarnings("restriction")
public class ExportWizardPage extends WizardFileSystemResourceExportPage1 {

	// widgets
	private Button overwriteAllExistingFilesButton;
	private Button overwriteOlderExistingFilesButton;
	private Button createLogFileCheckbox;

	// dialog store id constants
	private static final String STORE_DESTINATION_NAMES_ID = "ExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$
	private static final String STORE_OVERWRITE_ALL_EXISTING_FILES_ID = "ExportWizardPage.STORE_OVERWRITE_EXISTING_FILES_ID"; //$NON-NLS-1$
	private static final String STORE_CREATE_LOGFILE_ID = "ExportWizardPage.STORE_CREATE_LOGFILE_ID"; //$NON-NLS-1$

	protected ExportWizardPage(String name, IStructuredSelection selection) {
		super(name, selection);
		setTitle(name);
		setDescription(Messages.FileExport_description);
	}

	/**
	 * The Finish button was pressed. Try to do the required work now and answer
	 * a boolean indicating success. If false is returned then the wizard will
	 * not close.
	 *
	 * @return boolean
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean finish() {
		List resourcesToExport = getWhiteCheckedResources();
		if (!ensureTargetIsValid(new File(getDestinationValue()))) {
			return false;
		}

		// Save dirty editors if possible but do not stop if not all are saved
		saveDirtyEditors();
		// about to invoke the operation so save our state
		saveWidgetValues();
		return executeExportOperation(new EclipseExportOperation(null,
				resourcesToExport, getDestinationValue(), this));
	}

	/**
	 * Set up and execute the passed Operation. Answer a boolean indicating
	 * success.
	 *
	 * @return boolean
	 */
	protected boolean executeExportOperation(EclipseExportOperation op) {
		op.setCreateLeadupStructure(true);
		op.setOverwriteFiles(true);
		op.setOverwriteOlder(overwriteOlderExistingFilesButton.getSelection());
		op.setCreateLog(createLogFileCheckbox.getSelection());

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			// Got interrupted. Do nothing.
			return false;
		} catch (InvocationTargetException e) {
			// Couldn't start. Do nothing.
			displayErrorDialog(e.getTargetException());
			return false;
		}
		if (createLogFileCheckbox.getSelection()) {
			op.createLogFile(null);
		}

		IStatus status = op.getStatus();
		if (status.isOK()) {
			MessageDialog.openInformation(getContainer().getShell(),
					Messages.FileExport_success,
					Messages.FileExport_success_msg);
			return true;
		} else {
			ErrorDialog.openError(getContainer().getShell(),
					Messages.FileExport_exportProblems,
					Messages.FileExport_exportProblems_msg, status);
			return false;
		}

	}

	/**
	 * Create the buttons in the options group.
	 */
	@Override
	protected void createOptionsGroupButtons(Group optionsGroup) {

		Font font = optionsGroup.getFont();

		// overwrite all existing files radio
		overwriteAllExistingFilesButton = new Button(optionsGroup, SWT.RADIO
				| SWT.LEFT);
		overwriteAllExistingFilesButton
				.setText(Messages.FileExport_overwriteAllExisting);
		overwriteAllExistingFilesButton.setSelection(false);
		overwriteAllExistingFilesButton.setFont(font);

		// overwrite older existing files radio
		overwriteOlderExistingFilesButton = new Button(optionsGroup, SWT.RADIO
				| SWT.LEFT);
		overwriteOlderExistingFilesButton
				.setText(Messages.FileExport_overwriteOlderExisting);
		overwriteOlderExistingFilesButton.setSelection(true);
		overwriteOlderExistingFilesButton.setFont(font);

		// create log checkbox
		createLogFileCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		createLogFileCheckbox.setText(Messages.FileExport_createLog);
		createLogFileCheckbox.setSelection(true);
		createLogFileCheckbox.setFont(font);
	}

	/**
	 * Hook method for saving widget values for restoration by the next instance
	 * of this class.
	 */
	@Override
	protected void internalSaveWidgetValues() {
		// update directory names history
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings
					.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				directoryNames = new String[0];
			}

			directoryNames = addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

			// options
			settings.put(STORE_OVERWRITE_ALL_EXISTING_FILES_ID,
					overwriteAllExistingFilesButton.getSelection());

			settings.put(STORE_CREATE_LOGFILE_ID, createLogFileCheckbox
					.getSelection());

		}
	}

	/**
	 * Hook method for restoring widget values to the values that they held last
	 * time this wizard was used to completion.
	 */
	@Override
	protected void restoreWidgetValues() {
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings
					.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				return; // ie.- no settings stored
			}

			// destination
			setDestinationValue(directoryNames[0]);
			for (String directoryName : directoryNames) {
				addDestinationItem(directoryName);
			}

			// options
			boolean overwriteAllExistingFiles = settings
					.getBoolean(STORE_OVERWRITE_ALL_EXISTING_FILES_ID);
			overwriteAllExistingFilesButton
					.setSelection(overwriteAllExistingFiles);
			overwriteOlderExistingFilesButton
					.setSelection(!overwriteAllExistingFiles);

			createLogFileCheckbox.setSelection(settings
					.getBoolean(STORE_CREATE_LOGFILE_ID));

		}
	}

}
