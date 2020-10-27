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
package com.ibm.designer.extensibility.copyfile.openntf.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.json.JSONException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.WizardDataTransferPage;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.openntf.beans.Catalog;
import com.ibm.designer.extensibility.copyfile.openntf.beans.Control;
import com.ibm.designer.extensibility.copyfile.openntf.internal.ImportThread;
import com.ibm.designer.extensibility.copyfile.openntf.tree.TreeContentProvider;
import com.ibm.designer.extensibility.copyfile.openntf.tree.TreeFactory;
import com.ibm.designer.extensibility.copyfile.openntf.tree.TreeLabelProvider;
import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class OpenNTFImportWizardPage extends WizardDataTransferPage {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private String sourceClass = this.getClass().getName();

	private IResource currentResourceSelection;

	// initial value stores
	private String initialContainerFieldValue;

	// widgets
	private ContainerCheckedTreeViewer controlTreeViewer;
	private Text controlDescription;
	private Text containerNameField;
	private Button containerBrowseButton;
	private Button importUnitTestCheckbox;
	private Button overwriteExistingResourcesCheckbox;
	private Button removeTempFilesCheckbox;
	private Button createLogFileCheckbox;

	// dialog store id constants
	private final static String STORE_OPENNTF_IMPORT_UNIT_TESTS_ID = "OpenNTF.ImportWizardPage.IMPORT_UNIT_TESTS_ID";//$NON-NLS-1$
	private final static String STORE_OPENNTF_OVERWRITE_EXISTING_RESOURCES_ID = "OpenNTF.ImportWizardPage.STORE_OVERWRITE_EXISTING_RESOURCES_ID";//$NON-NLS-1$
	private final static String STORE_OPENNTF_REMOVE_TEMP_FILES_ID = "OpenNTF.ImportWizardPage.REMOVE_TEMP_FILES_ID";//$NON-NLS-1$
	private final static String STORE_OPENNTF_CREATE_LOGFILE_ID = "OpenNTF.ImportWizardPage.STORE_CREATE_LOGFILE_ID";//$NON-NLS-1$    

	// Tree viewer size
	private final static int TREE_HEIGHT = 300;

	private String destinationProject;

	/**
	 * Creates an instance of this class
	 */
	public OpenNTFImportWizardPage(String name, IWorkbench aWorkbench,
			IStructuredSelection selection) {
		super(name);
		setTitle(name);
		setDescription(Messages.OpenNTFImport_description);

		// Initialize to null
		currentResourceSelection = null;
		if (selection.size() == 1) {
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				Object resource = ((IAdaptable) firstElement)
						.getAdapter(IResource.class);
				if (resource != null) {
					currentResourceSelection = (IResource) resource;
				}
			}
		}

		if (currentResourceSelection != null) {
			if (currentResourceSelection.getType() == IResource.FILE) {
				currentResourceSelection = currentResourceSelection.getParent();
			}

			if (!currentResourceSelection.isAccessible()) {
				currentResourceSelection = null;
			}
		}
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		composite.setFont(parent.getFont());

		createSourceGroup(composite);
		createDestinationGroup(composite);
		createOptionsGroup(composite);

		restoreWidgetValues();
		updateWidgetEnablements();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message

		setControl(composite);
	}

	protected final void createSourceGroup(Composite parent) {
		createControlSelectionGroup(parent);
	}

	/**
	 * Create the import options specification widgets.
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {
		Font font = optionsGroup.getFont();
		// import unit tests
		importUnitTestCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		importUnitTestCheckbox.setFont(font);
		importUnitTestCheckbox.setText(Messages.OpenNTFImport_importUnitTests);
		importUnitTestCheckbox.setSelection(false);

		// overwrite checkbox
		overwriteExistingResourcesCheckbox = new Button(optionsGroup, SWT.CHECK
				| SWT.LEFT);
		overwriteExistingResourcesCheckbox.setFont(font);
		overwriteExistingResourcesCheckbox
				.setText(Messages.OpenNTFImport_overwriteAllExisting);
		overwriteExistingResourcesCheckbox.setSelection(false);

		// remove temp files checkbox
		removeTempFilesCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		removeTempFilesCheckbox.setFont(font);
		removeTempFilesCheckbox.setText(Messages.OpenNTFImport_removetempfiles);
		removeTempFilesCheckbox.setSelection(true);

		// create log checkbox
		createLogFileCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		createLogFileCheckbox.setFont(font);
		createLogFileCheckbox.setText(Messages.OpenNTFImport_createLog);
		createLogFileCheckbox.setSelection(true);
	}

	protected void createControlSelectionGroup(Composite parent) {
		Label groupLabel = new Label(parent, SWT.NONE);
		groupLabel.setText(Messages.OpenNTFImport_select_controls);
		groupLabel.setFont(parent.getFont());

		Composite controlContainerGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		controlContainerGroup.setLayout(layout);
		controlContainerGroup.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		controlContainerGroup.setLayoutData(data);

		// Create check box tree
		controlTreeViewer = new ContainerCheckedTreeViewer(
				controlContainerGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		data.heightHint = TREE_HEIGHT;
		Tree controlTree = controlTreeViewer.getTree();
		controlTree.setLayoutData(data);

		controlTreeViewer.setLabelProvider(new TreeLabelProvider());
		controlTreeViewer.setContentProvider(new TreeContentProvider());
		List<Catalog> treeList = null;
		try {
			treeList = TreeFactory.createTree();
		} catch (JSONException e) {
			setErrorMessage(Messages.OpenNTFImport_error_getControlFromOpenNTF);
			logger.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			setErrorMessage(Messages.OpenNTFImport_error_getControlFromOpenNTF);
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		controlTreeViewer.setInput(treeList);

		controlTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						handleSelectionChanged((IStructuredSelection) e
								.getSelection());
					}
				});

		controlTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				setPageComplete(determinePageCompletion());
			}
		});

		// Create control description text
		controlDescription = new Text(controlContainerGroup, SWT.BORDER
				| SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		controlDescription.setLayoutData(data);
		controlDescription.setEditable(false);
	}

	private void handleSelectionChanged(IStructuredSelection ssel) {
		Object item = ssel.getFirstElement();
		StringBuffer strbuf = new StringBuffer(""); //$NON-NLS-1$
		if (item instanceof Control) {
			Control ctl = (Control) item;
			strbuf.append(ctl.getName());
			strbuf.append("\n\nDescription: ").append(ctl.getDescription()); //$NON-NLS-1$
			strbuf.append("\n\nDevelopers: ").append(ctl.getDevelopers()); //$NON-NLS-1$
			strbuf.append("\n\nLastUpdated: ").append(ctl.getLastUpdated()); //$NON-NLS-1$
		}
		controlDescription.setText(strbuf.toString());
	}

	/**
	 * Creates the import destination specification controls.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected final void createDestinationGroup(Composite parent) {
		// container specification group
		Composite containerGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		containerGroup.setLayout(layout);
		containerGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		containerGroup.setFont(parent.getFont());

		// container label
		Label resourcesLabel = new Label(containerGroup, SWT.NONE);
		resourcesLabel.setText(Messages.OpenNTFImport_label_intoDir);
		resourcesLabel.setFont(parent.getFont());

		// container name entry field
		containerNameField = new Text(containerGroup, SWT.SINGLE | SWT.BORDER);
		containerNameField.addListener(SWT.Modify, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		containerNameField.setLayoutData(data);
		containerNameField.setFont(parent.getFont());
		containerNameField.addListener(SWT.Selection, this);

		// container browse button
		containerBrowseButton = new Button(containerGroup, SWT.PUSH);
		containerBrowseButton.setText(Messages.OpenNTFImport_button_browse);
		containerBrowseButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		containerBrowseButton.addListener(SWT.Selection, this);
		containerBrowseButton.setFont(parent.getFont());
		setButtonLayoutData(containerBrowseButton);

		initialPopulateContainerField();
	}

	/**
	 * Sets the initial contents of the container name field.
	 */
	protected final void initialPopulateContainerField() {
		if (initialContainerFieldValue != null) {
			containerNameField.setText(initialContainerFieldValue);
		} else if (currentResourceSelection != null) {
			containerNameField.setText(currentResourceSelection.getFullPath()
					.makeRelative().segment(0).toString());
		}
	}

	/**
	 * Sets the value of this page's container resource field, or stores it for
	 * future use if this page's controls do not exist yet.
	 * 
	 * @param value
	 *            String
	 */
	public void setContainerFieldValue(String value) {
		if (containerNameField == null) {
			initialContainerFieldValue = value;
		} else {
			containerNameField.setText(value);
		}
	}

	/**
	 * The Finish button was pressed. Try to do the required work now and answer
	 * a boolean indicating success. If false is returned then the wizard will
	 * not close.
	 * 
	 * @return boolean
	 */
	public boolean finish() {
		// about to invoke the operation so save our state
		saveWidgetValues();

		MessageDialog.openInformation(getContainer().getShell(),
				Messages.OpenNTFImport_Start_title,
				Messages.OpenNTFImport_Start_msg);

		boolean result = executeImportOperation();
		return result;
	}

	protected boolean executeImportOperation() {
		String sourceMethod = "executeImportOperation"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		// Get controls list need to be imported
		ArrayList<Control> impControls = new ArrayList<Control>();
		Object[] checkedObjs = controlTreeViewer.getCheckedElements();
		int i;
		for (i = 0; i < checkedObjs.length; i++) {
			if (checkedObjs[i] instanceof Control) {
				Control ctl = (Control) checkedObjs[i];
				impControls.add(ctl);
			}
		}

		destinationProject = getContainerFullPath().segment(0).toString();
		boolean createLogFile = createLogFileCheckbox.getSelection();
		boolean overwriteWithoutWarning = overwriteExistingResourcesCheckbox
				.getSelection();
		boolean removeTempFile = removeTempFilesCheckbox.getSelection();
		boolean importUnitTest = importUnitTestCheckbox.getSelection();

		// Import reusable controls in multi-threads
		ImportThread.setCount(impControls.size());
		ImportThread.setDestinationProject(destinationProject);
		ImportThread.setCreateLogFile(createLogFile);
		ImportThread.setOverwriteWithoutWarning(overwriteWithoutWarning);
		ImportThread.setRemoveTempFile(removeTempFile);
		ImportThread.setImportUnitTest(importUnitTest);
		for (i = 0; i < impControls.size(); i++) {
			Thread thread = new Thread(new ImportThread(impControls.get(i)));
			thread.start();
		}

		logger.exiting(sourceClass, sourceMethod, true);
		return true;
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			// radio buttons and checkboxes
			settings.put(STORE_OPENNTF_IMPORT_UNIT_TESTS_ID,
					importUnitTestCheckbox.getSelection());
			settings.put(STORE_OPENNTF_OVERWRITE_EXISTING_RESOURCES_ID,
					overwriteExistingResourcesCheckbox.getSelection());
			settings.put(STORE_OPENNTF_REMOVE_TEMP_FILES_ID,
					removeTempFilesCheckbox.getSelection());
			settings.put(STORE_OPENNTF_CREATE_LOGFILE_ID, createLogFileCheckbox
					.getSelection());
		}
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			if (settings.get(STORE_OPENNTF_OVERWRITE_EXISTING_RESOURCES_ID) == null) {
				return; // ie.- no settings stored, so stop
			}
			// radio buttons and checkboxes
			importUnitTestCheckbox.setSelection(settings
					.getBoolean(STORE_OPENNTF_IMPORT_UNIT_TESTS_ID));
			overwriteExistingResourcesCheckbox.setSelection(settings
					.getBoolean(STORE_OPENNTF_OVERWRITE_EXISTING_RESOURCES_ID));
			removeTempFilesCheckbox.setSelection(settings
					.getBoolean(STORE_OPENNTF_REMOVE_TEMP_FILES_ID));
			createLogFileCheckbox.setSelection(settings
					.getBoolean(STORE_OPENNTF_CREATE_LOGFILE_ID));
		}
	}

	/**
	 * Return the path for the resource field.
	 * 
	 * @return IPath
	 */
	protected IPath getResourcePath() {
		return getPathFromText(this.containerNameField);
	}

	/**
	 * Returns the container resource specified in the container name entry
	 * field, or <code>null</code> if such a container does not exist in the
	 * workbench.
	 * 
	 * @return the container resource specified in the container name entry
	 *         field, or <code>null</code>
	 */
	protected IContainer getSpecifiedContainer() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath path = getContainerFullPath();
		if (workspace.getRoot().exists(path)) {
			IResource resource = workspace.getRoot().findMember(path);
			if (resource.getType() == IResource.PROJECT) {
				return (IContainer) resource;
			}
			return null;
		}
		return null;
	}

	/**
	 * Returns the path of the container resource specified in the container
	 * name entry field, or <code>null</code> if no name has been typed in.
	 * <p>
	 * The container specified by the full path might not exist and would need
	 * to be created.
	 * </p>
	 * 
	 * @return the full path of the container resource specified in the
	 *         container name entry field, or <code>null</code>
	 */
	protected IPath getContainerFullPath() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// make the path absolute to allow for optional leading slash
		IPath testPath = getResourcePath();

		if (testPath.equals(workspace.getRoot().getFullPath())) {
			return testPath;
		}

		IStatus result = workspace.validatePath(testPath.toString(),
				IResource.PROJECT | IResource.FOLDER | IResource.ROOT);
		if (result.isOK()) {
			return testPath;
		}

		return null;
	}

	/**
	 * The <code>OpenNTFImportWizardPage</code> implementation of this
	 * <code>WizardDataTransferPage</code> method returns <code>true</code>.
	 * Subclasses may override this method.
	 */
	protected boolean allowNewContainerName() {
		return true;
	}

	/**
	 * The <code>OpenNTFImportWizardPage</code> implementation of this
	 * <code>Listener</code> method handles all events and enablements for
	 * controls on this page.
	 * 
	 * @param event
	 *            Event
	 */
	public void handleEvent(Event event) {
		Widget source = event.widget;

		if (source == containerNameField) {
			setPageComplete(determinePageCompletion());
		}

		if (source == containerBrowseButton) {
			handleContainerBrowseButtonPressed();
		}

		updateWidgetEnablements();
	}

	/**
	 * Opens a container selection dialog and displays the user's subsequent
	 * container resource selection in this page's container name field.
	 */
	protected void handleContainerBrowseButtonPressed() {
		IPath containerPath = queryForContainer(getSpecifiedContainer(),
				Messages.OpenNTFImport_selectAppLabel,
				Messages.OpenNTFImport_selectAppTitle);

		// if a container was selected then put its name in the container name
		// field
		if (containerPath != null) { // null means user cancelled
			setErrorMessage(null);
			containerNameField.setText(containerPath.makeRelative().segment(0)
					.toString());
		}
	}

	/*
	 * @see WizardDataTransferPage.determinePageCompletion.
	 */
	protected boolean determinePageCompletion() {
		if (controlTreeViewer.getCheckedElements().length == 0) {
			setErrorMessage(Messages.OpenNTFImport_error_noSource);
			return false;
		}

		// Check for valid projects before making the user do anything
		if (noOpenProjects()) {
			setErrorMessage(Messages.FileImport_error_noOpenProjects);
			return false;
		}

		IPath destinationPath = getContainerFullPath();
		if (destinationPath == null) {
			setErrorMessage(Messages.OpenNTFImport_warn_specify_application);
			return false;
		}

		if (destinationPath.segmentCount() > 1) {
			setErrorMessage(Messages.FileImport_error_hierarchy);
			return false;
		}

		String destinationName = destinationPath.segment(0);
		if (!destinationName.endsWith(".nsf")) { //$NON-NLS-1$
			setErrorMessage(Messages.FileImport_error_folder_name);
			return false;
		}

		return super.determinePageCompletion();
	}

	/**
	 * Returns whether or not the passed workspace has any open projects
	 * 
	 * @return boolean
	 */
	private boolean noOpenProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen()) {
				return false;
			}
		}
		return true;
	}
}
