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

package com.ibm.designer.extensibility.copyfile.wizards;

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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceImportPage1;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.internal.EclipseImportOperation;
import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
@SuppressWarnings("restriction")
public class ImportWizardPage extends WizardFileSystemResourceImportPage1 {

	// widgets
	private Button overwriteAllExistingFilesButton;
	private Button overwriteOlderExistingFilesButton;
	private Button createLogFileCheckbox;

	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID = "ImportWizardPage.STORE_SOURCE_NAMES_ID";//$NON-NLS-1$
	private final static String STORE_OVERWRITE_ALL_EXISTING_RESOURCES_ID = "ImportWizardPage.STORE_OVERWRITE_ALL_EXISTING_RESOURCES_ID";//$NON-NLS-1$
	private final static String STORE_CREATE_LOGFILE_ID = "ImportWizardPage.STORE_CREATE_LOGFILE_ID";//$NON-NLS-1$    

	public ImportWizardPage(String name, IWorkbench aWorkbench,
			IStructuredSelection selection) {
		super(name, aWorkbench, selection);
		setTitle(name);
		setDescription(Messages.FileImport_description);
	}

	/**
	 * Import the resources with extensions as specified by the user
	 */
	@SuppressWarnings("unchecked")
	protected boolean importResources(List fileSystemObjects) {
		EclipseImportOperation operation = new EclipseImportOperation(
				getContainerFullPath(), getSourceDirectory(),
				FileSystemStructureProvider.INSTANCE, this, fileSystemObjects);

		operation.setContext(getShell());
		return executeImportOperation(operation);
	}

	/**
	 * Execute the passed import operation. Answer a boolean indicating success.
	 */
	protected boolean executeImportOperation(EclipseImportOperation op) {
		op.setCreateContainerStructure(true);
		op.setOverwriteResources(true);
		op.setCreateLog(createLogFileCheckbox.getSelection());
		op.setOverwriteOlder(overwriteOlderExistingFilesButton.getSelection());

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
					Messages.FileImport_success,
					Messages.FileImport_success_msg);
			return true;
		} else {
			ErrorDialog.openError(getContainer().getShell(),
					Messages.FileImport_importProblems,
					Messages.FileImport_importProblems_msg, status);
			return false;
		}

	}

	/**
	 * Create the import options specification widgets.
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {
		Font font = optionsGroup.getFont();

		// overwrite all existing files radio
		overwriteAllExistingFilesButton = new Button(optionsGroup, SWT.RADIO
				| SWT.LEFT);
		overwriteAllExistingFilesButton
				.setText(Messages.FileImport_overwriteAllExisting);
		overwriteAllExistingFilesButton.setSelection(false);
		overwriteAllExistingFilesButton.setFont(font);

		// overwrite older files radio
		overwriteOlderExistingFilesButton = new Button(optionsGroup, SWT.RADIO
				| SWT.LEFT);
		overwriteOlderExistingFilesButton
				.setText(Messages.FileImport_overwriteOlderExisting);
		overwriteOlderExistingFilesButton.setSelection(true);
		overwriteOlderExistingFilesButton.setFont(font);

		// create log checkbox
		createLogFileCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		createLogFileCheckbox.setText(Messages.FileImport_createLog);
		createLogFileCheckbox.setSelection(true);
		createLogFileCheckbox.setFont(font);
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) {
				return; // ie.- no values stored, so stop
			}

			// set filenames history
			for (int i = 0; i < sourceNames.length; i++) {
				sourceNameField.add(sourceNames[i]);
			}

			// radio buttons and checkboxes
			boolean overwriteAllExistingFiles = settings
					.getBoolean(STORE_OVERWRITE_ALL_EXISTING_RESOURCES_ID);
			overwriteAllExistingFilesButton
					.setSelection(overwriteAllExistingFiles);
			overwriteOlderExistingFilesButton
					.setSelection(!overwriteAllExistingFiles);
			createLogFileCheckbox.setSelection(settings
					.getBoolean(STORE_CREATE_LOGFILE_ID));

		}
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			// update source names history
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) {
				sourceNames = new String[0];
			}

			sourceNames = addToHistory(sourceNames, getSourceDirectory()
					.getAbsolutePath());
			settings.put(STORE_SOURCE_NAMES_ID, sourceNames);

			// radio buttons and checkboxes
			settings.put(STORE_OVERWRITE_ALL_EXISTING_RESOURCES_ID,
					overwriteAllExistingFilesButton.getSelection());

			settings.put(STORE_CREATE_LOGFILE_ID, createLogFileCheckbox
					.getSelection());

		}
	}

	/**
	 * Answer a boolean indicating whether self's source specification widgets
	 * currently all contain valid values.
	 */
	protected boolean validateSourceGroup() {
		if (!super.validateSourceGroup()) {
			return false;
		}

		String destinationName = getContainerFullPath().segment(0);

		if(!destinationName.endsWith(".nsf")) { //$NON-NLS-1$
			setErrorMessage(Messages.FileImport_error_folder_name);
			return false;
		}

		return true;
	}

}
