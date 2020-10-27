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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ui.commons.extensions.DesignerResource;
import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.internal.MyFileUtil;
import com.ibm.designer.extensibility.copyfile.openntf.internal.DownloadJob;
import com.ibm.designer.extensibility.copyfile.openntf.internal.XMLCreater;
import com.ibm.designer.extensibility.copyfile.openntf.internal.ZipJob;
import com.ibm.designer.extensibility.copyfile.resources.Messages;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class OpenNTFExportWizardPage extends WizardDataTransferPage {
	private static final Logger logger = ImportExportPlugin.getLogger();
	private String sourceClass = this.getClass().getName();

	private IStructuredSelection initialResourceSelection;
	// private List selectedTypes = new ArrayList();

	// widgets
	private ResourceTreeAndListGroup coreResourceGroup;
	private ResourceTreeAndListGroup testResourceGroup;
	private Combo destinationNameField;
	private Button destinationBrowseButton;
	private Text zipNameField;
	private Text otherFileNamesField;
	private Composite otherFilesGroup;
	private Button editNoticeButton;

	private Combo licenseSelectionCombo;
	private Button otherFileCheckbox;

	// dialog store id constants
	private static final String STORE_DESTINATION_NAMES_ID = "ExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$
	private static final String STORE_LICENSE_ID = "ExportWizardPage.STORE_LICENSE_ID"; //$NON-NLS-1$
	private static final String STORE_OTHERFILE_ID = "ExportWizardPage.STORE_OTHERFILE_ID"; //$NON-NLS-1$

	// Licenses list
	private static final String[] LICENSE_ITEMS = {
			Messages.OpenNTFExport_license_Apache,
			Messages.OpenNTFExport_license_GNU_GPL3,
			Messages.OpenNTFExport_license_GNU_LGPL3,
			Messages.OpenNTFExport_license_GNU_Affero_GPL3 };

	private static final String[][] LICENSE_FILES = {
			{ "LICENSE_Apache", "" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "LICENSE_GNU_GPL3", "http://www.gnu.org/licenses/gpl-3.0.txt" },//$NON-NLS-1$ //$NON-NLS-2$
			{ "LICENSE_LGNU_GPL3", "http://www.gnu.org/licenses/lgpl.txt" },//$NON-NLS-1$ //$NON-NLS-2$
			{
					"LICENSE_GNU_Affero_GPL3", "http://www.gnu.org/licenses/agpl-3.0.txt" } //$NON-NLS-1$ //$NON-NLS-2$
	};

	// These filter names are displayed to the user in the file dialog. Note
	// that the inclusion of the actual extension in parentheses is optional,
	// and doesn't have any effect on which files are displayed.
	private static final String[] FILTER_NAMES = { "All Files (*.*)" }; //$NON-NLS-1$

	// These filter extensions are used to filter which files are displayed.
	private static final String[] FILTER_EXTS = { "*.*" }; //$NON-NLS-1$

	private String projectName = null;
	private String destinationZipPath = null;
	private File tempNoticeFile = null;

	private MyFileUtil myFileUtil = new MyFileUtil();

	protected OpenNTFExportWizardPage(String name,
			IStructuredSelection selection) {
		super(name);
		this.initialResourceSelection = selection;
		setTitle(name);
		setDescription(Messages.OpenNTFExport_description);
	}

	/**
	 * The Finish button was pressed. Try to do the required work now and answer
	 * a boolean indicating success. If false is returned then the wizard will
	 * not close.
	 * 
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean finish() {
		if (!ensureTargetIsValid(new File(getDestinationValue()))) {
			return false;
		}

		// Save dirty editors if possible but do not stop if not all are saved
		saveDirtyEditors();
		// about to invoke the operation so save our state
		saveWidgetValues();

		List resourcesToExport = getWhiteCheckedResources();
		List testResources = getTestCheckedResources();
		boolean result = executeExportOperation(resourcesToExport,
				testResources, getDestinationValue());

		if (result) {
			MessageDialog.openInformation(getContainer().getShell(),
					Messages.OpenNTFExport_Success_title, NLS.bind(
							Messages.OpenNTFExport_Success_msg, projectName,
							destinationZipPath));
		} else {
			MessageDialog.openInformation(getContainer().getShell(),
					Messages.OpenNTFExport_Problems_title,
					Messages.OpenNTFExport_Problems_msg);
		}

		return result;
	}

	/**
	 * Set up and execute the passed Operation. Answer a boolean indicating
	 * success.
	 * 
	 * @return boolean
	 */
	protected boolean executeExportOperation(List<IResource> coreResources,
			List<IResource> testResources, String destinationPath) {
		boolean result = true;
		String sourceMethod = "executeExportOperation"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		String zipName = zipNameField.getText();
		if (zipName.endsWith(".zip")) { //$NON-NLS-1$
			zipName = zipName.substring(0, zipName.length() - 4);
		}

		File tempDestination = new Path(destinationPath).append(zipName)
				.toFile();
		if (tempDestination.exists()) {
			if (tempDestination.isDirectory()) {
				myFileUtil.delFolder(tempDestination);
			} else {
				tempDestination.delete();
			}
		}
		tempDestination.mkdirs();

		File tempNoticeFile = null;

		while (true) {
			// Copy nsf file
			IResource resource = (IResource) coreResources.get(0);
			IProject eclipseProject = resource.getProject();

			DesignerProject designerProject = DesignerResource
					.getDesignerProject(eclipseProject);
			String serverName = designerProject.getServerName();
			projectName = designerProject.getDatabaseName();

			try {
				NotesThread.sinitThread();
				Session session = NotesFactory.createSessionWithFullAccess();
				Database db = session.getDatabase(serverName, projectName,
						false);
				if (db == null) {
					logger.logp(Level.WARNING, sourceClass, sourceMethod,
							String.format(
									"%s does not exist on %s", projectName, //$NON-NLS-1$
									serverName));
					result = false;
					break;
				}
				if (!db.isOpen()) {
					db.open();
				}
				db.createCopy(null, new File(tempDestination, projectName)
						.getAbsolutePath());
			} catch (NotesException e) {
				logger
						.logp(
								Level.SEVERE,
								sourceClass,
								sourceMethod,
								String
										.format(
												"Failed to create a copy database of %s on %s into the temporary directory %s.", projectName, serverName, serverName, tempDestination), e); //$NON-NLS-1$
				result = false;
				break;
			} finally {
				NotesThread.stermThread();
			}
			if (logger.isLoggable(Level.FINE)) {
				logger
						.fine(String
								.format(
										"Succed copy nsf file %s on %s to %s.", projectName, serverName, tempDestination));//$NON-NLS-1$
			}

			// Copy notice file
			tempNoticeFile = getTempNoticeFile(false);
			result = myFileUtil.copyfile(tempNoticeFile, new File(
					tempDestination, OpenNTFConstants.NOTICE_NAME), true);
			if (!result) {
				break;
			}
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(String.format(
						"Succed copy NOTICE file to %s.", tempDestination));//$NON-NLS-1$
			}

			// Copy license file.
			File licenseFile = null;
			int licenseSelection = licenseSelectionCombo.getSelectionIndex();
			String licenseFileName = LICENSE_FILES[licenseSelection][0];
			if (licenseSelection == 0) {
				// Copy Apache license directly from bundle source
				try {
					licenseFile = getFileFromBundle(new Path(
							OpenNTFConstants.LICENSE_DIR).append(
							licenseFileName).toString());
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					result = false;
					break;
				} catch (URISyntaxException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					result = false;
					break;
				}
				if (logger.isLoggable(Level.FINEST)) {
					logger
							.finest(String
									.format(
											"Succed get Apache License from bundle source. licenseFile = %s", licenseFile));//$NON-NLS-1$
				}
			} else {
				// Download GPL license from web. In this way, our source code
				// is
				// not implying that we are open-sourcing the text of those
				// licenses.
				licenseFile = downloadGPLLicenseFile(licenseSelection);
				if (logger.isLoggable(Level.FINEST)) {
					logger
							.finest(String
									.format(
											"Download GPL License from web. licenseFile = %s", licenseFile));//$NON-NLS-1$
				}
			}

			result = myFileUtil.copyfile(licenseFile, new File(tempDestination,
					OpenNTFConstants.LICENSE_NAME), true);
			if (!result) {
				break;
			}
			if (logger.isLoggable(Level.FINE)) {
				logger
						.fine(String
								.format(
										"Succed copy License file %s to %s.", licenseFile, tempDestination));//$NON-NLS-1$
			}

			// Copy other files
			String[] otherFiles = otherFileNamesField.getText().split(
					OpenNTFConstants.FILE_SEPARATOR_READ);
			for (int i = 0; i < otherFiles.length; i++) {
				File aFile = new File(otherFiles[i]);
				result = myFileUtil.copyfile(aFile, new File(tempDestination,
						aFile.getName()), true);
				if (!result) {
					break;
				}
				if (logger.isLoggable(Level.FINEST)) {
					logger
							.finest(String
									.format(
											"Succed copy other file %s to %s.", aFile, tempDestination));//$NON-NLS-1$
				}
			}
			if (!result) {
				break;
			}
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(String.format(
						"Succed copy all other files to %s.", tempDestination));//$NON-NLS-1$
			}

			// Create xml file
			ArrayList<String> files = new ArrayList<String>();
			ArrayList<String> folders = new ArrayList<String>();
			for (int i = 0; i < coreResources.size(); i++) {
				IResource res = (IResource) coreResources.get(i);
				IPath tempPath = res.getFullPath().removeFirstSegments(1);
				if (res instanceof IFile) {
					files.add(tempPath.toString());
				} else if (res instanceof IFolder) {
					folders.add(tempPath.toString());
				} else if (res instanceof IProject) {
					String[] default_folders = OpenNTFConstants.DEFAULT_IMPORT_FOLDERS;
					for (int f = 0; f < default_folders.length; f++) {
						folders.add(default_folders[f]);
					}
				}
			}

			ArrayList<String> testFiles = new ArrayList<String>();
			ArrayList<String> testFolders = new ArrayList<String>();
			for (int i = 0; i < testResources.size(); i++) {
				IResource res = (IResource) testResources.get(i);
				IPath tempPath = res.getFullPath().removeFirstSegments(1);
				if (res instanceof IFile) {
					testFiles.add(tempPath.toString());
				} else if (res instanceof IFolder) {
					testFolders.add(tempPath.toString());
				}
			}

			result = new XMLCreater(new File(tempDestination,
					OpenNTFConstants.default_importlist_xmlfile), folders,
					files, testFolders, testFiles).createXmlFile(true);
			if (!result) {
				logger.logp(Level.WARNING, sourceClass, sourceMethod,
						Messages.OpenNTFExport_warn_failed_create_xml);
			} else {
				if (logger.isLoggable(Level.FINE)) {
					logger
							.fine(String
									.format(
											"Succed create importlist.xml in %s.", tempDestination));//$NON-NLS-1$
				}

			}

			// Compress to a .zip file
			File destinationZipFile = new File(destinationPath, zipName
					+ ".zip"); //$NON-NLS-1$
			destinationZipPath = destinationZipFile.getAbsolutePath();
			ZipJob zipJob = new ZipJob(
					"ZipJob", tempDestination, destinationZipFile); //$NON-NLS-1$
			zipJob.schedule();
			try {
				zipJob.join();
			} catch (InterruptedException e) {
				// Do nothing if the thread is interrupted
				result = false;
			}
			break;
		}

		myFileUtil.delFolder(tempDestination);
		if (tempNoticeFile != null) {
			tempNoticeFile.delete();
		}

		logger.exiting(sourceClass, sourceMethod, result);
		return result;
	}

	private File getFileFromBundle(String location) throws IOException,
			URISyntaxException {
		String root = ImportExportPlugin.getRootDirectory().getAbsolutePath();
		// e.g., root = file:C:\Program Files\IBM\Lotus\Notes\Data\workspace
		// \applications\eclipse\plugins\com.ibm.designer.extensibility.copyfile_2.0.0
		if (root.startsWith("file:")) { //$NON-NLS-1$
			root = root.substring(5);
		}
		return new File(root, location);
	}

	private File getTempNoticeFile(boolean deleteOrigNotice) {
		if (tempNoticeFile == null) {
			File tempDir = ImportExportPlugin.getTempDir();
			tempNoticeFile = new File(tempDir, OpenNTFConstants.NOTICE_NAME);
		}

		if (tempNoticeFile.exists() && deleteOrigNotice) {
			tempNoticeFile.delete();
		}

		if (!tempNoticeFile.exists()) {
			File sourceFile = null;
			try {
				sourceFile = getFileFromBundle(OpenNTFConstants.NOTICE_LOCATION);
				myFileUtil.copyfile(sourceFile, tempNoticeFile, false);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} catch (URISyntaxException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		return tempNoticeFile;

	}

	private File downloadGPLLicenseFile(int licenseSelection) {
		File licenseFile = new File(ImportExportPlugin.getTempDir(),
				LICENSE_FILES[licenseSelection][0]);
		if (licenseFile.exists()) {
			return licenseFile;
		} else {
			URL url = null;
			try {
				url = new URL(LICENSE_FILES[licenseSelection][1]);
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, e.getMessage()
						+ " url = " + LICENSE_FILES[licenseSelection][1], e); //$NON-NLS-1$
				return licenseFile;
			}
			DownloadJob downloadJob = new DownloadJob(
					"DownloadJob", url, new Path(ImportExportPlugin.getTempDir().getAbsolutePath()), LICENSE_FILES[licenseSelection][0]); //$NON-NLS-1$
			downloadJob.schedule();
			try {
				downloadJob.join();
			} catch (InterruptedException e) {
				// do nothing if the thread is interrupted
			}
			licenseFile = downloadJob.getDownloadFilePath().toFile();
		}
		return licenseFile;

	}

	/**
	 * Set the initial selections in the resource group.
	 */
	@SuppressWarnings("unchecked")
	protected void setupBasedOnInitialSelections() {
		Iterator it = this.initialResourceSelection.iterator();
		while (it.hasNext()) {
			IResource currentResource = (IResource) it.next();
			if (currentResource.getType() == IResource.FILE) {
				this.coreResourceGroup.initialCheckListItem(currentResource);
			} else {
				this.coreResourceGroup.initialCheckTreeItem(currentResource);
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
		composite.setFont(parent.getFont());

		createResourcesGroup(composite);

		createDestinationGroup(composite);

		createOptionsGroup(composite);

		restoreWidgetValues(); // ie.- subclass hook
		if (initialResourceSelection != null) {
			setupBasedOnInitialSelections();
		}

		updateWidgetEnablements();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message

		setControl(composite);

		destinationNameField.setFocus();
	}

	/**
	 * Creates the checkbox tree and list for selecting resources.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected final void createResourcesGroup(Composite parent) {

		Label resourceLabel = new Label(parent, SWT.NONE);
		resourceLabel.setText(Messages.OpenNTFExport_label_select_core_files);
		resourceLabel.setFont(parent.getFont());
		coreResourceGroup = createResourceTreeAndListGroup(parent);
		createButtonsGroup(parent, coreResourceGroup);

		resourceLabel = new Label(parent, SWT.NONE);
		resourceLabel.setText(Messages.OpenNTFExport_label_select_test_files);
		resourceLabel.setFont(parent.getFont());
		testResourceGroup = createResourceTreeAndListGroup(parent);
		createButtonsGroup(parent, testResourceGroup);
	}

	protected final ResourceTreeAndListGroup createResourceTreeAndListGroup(
			Composite parent) {
		ResourceTreeAndListGroup resourceGroup;
		// create the input element, which has the root resource
		// as its only child
		List<IProject> input = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen()) {
				input.add(projects[i]);
			}
		}

		resourceGroup = new ResourceTreeAndListGroup(parent, input,
				getResourceProvider(IResource.FOLDER | IResource.PROJECT),
				WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
				getResourceProvider(IResource.FILE), WorkbenchLabelProvider
						.getDecoratingWorkbenchLabelProvider(), SWT.NONE, true);

		ICheckStateListener listener = new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateWidgetEnablements();
			}
		};

		resourceGroup.addCheckStateListener(listener);
		return resourceGroup;
	}

	/**
	 * Returns a content provider for <code>IResource</code>s that returns only
	 * children of the given resource type.
	 */
	private ITreeContentProvider getResourceProvider(final int resourceType) {
		return new WorkbenchContentProvider() {
			@SuppressWarnings("unchecked")
			public Object[] getChildren(Object o) {
				if (o instanceof IContainer) {
					IResource[] members = null;
					try {
						members = ((IContainer) o).members();
					} catch (CoreException e) {
						// just return an empty set of children
						return new Object[0];
					}

					// filter out the desired resource types
					ArrayList<IResource> results = new ArrayList<IResource>();
					for (int i = 0; i < members.length; i++) {
						// And the test bits with the resource types to see if
						// they are what we want
						if ((members[i].getType() & resourceType) > 0) {
							results.add(members[i]);
						}
					}
					return results.toArray();
				}
				// input element case
				if (o instanceof ArrayList) {
					return ((ArrayList) o).toArray();
				}
				return new Object[0];
			}
		};
	}

	/**
	 * Creates the buttons for selecting specific types or selecting all or none
	 * of the elements.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected final void createButtonsGroup(Composite parent,
			final ResourceTreeAndListGroup resourceGroup) {

		Font font = parent.getFont();

		// top level group
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		Button selectButton = createButton(buttonComposite,
				IDialogConstants.SELECT_ALL_ID,
				Messages.OpenNTFExport_button_select_all, false);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resourceGroup.setAllSelections(true);
			}
		};
		selectButton.addSelectionListener(listener);
		selectButton.setFont(font);
		setButtonLayoutData(selectButton);

		Button deselectButton = createButton(buttonComposite,
				IDialogConstants.DESELECT_ALL_ID,
				Messages.OpenNTFExport_button_deselect_all, false);

		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resourceGroup.setAllSelections(false);
			}
		};
		deselectButton.addSelectionListener(listener);
		deselectButton.setFont(font);
		setButtonLayoutData(deselectButton);
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates a
	 * standard push button, registers for selection events including button
	 * presses and registers default buttons with its shell. The button id is
	 * stored as the buttons client data. Note that the parent's layout is
	 * assumed to be a GridLayout and the number of columns in this layout is
	 * incremented. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code>
	 *            constants for standard dialog button ids)
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if the button is to be the default button,
	 *            and <code>false</code> otherwise
	 * 
	 * @see org.eclipse.ui.dialogs.WizardExportResourcesPage#createButton
	 */
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;

		Button button = new Button(parent, SWT.PUSH);

		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(buttonData);

		button.setData(new Integer(id));
		button.setText(label);
		button.setFont(parent.getFont());

		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
			button.setFocus();
		}
		button.setFont(parent.getFont());
		setButtonLayoutData(button);
		return button;
	}

	protected void createDestinationGroup(Composite parent) {
		createToDirGroup(parent);
		createZipNameGroup(parent);
		createNoticeGroup(parent);
		// new Label(parent, SWT.NONE); // vertical spacer
	}

	protected void createToDirGroup(Composite parent) {
		Font font = parent.getFont();
		// destination specification group
		Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		destinationSelectionGroup.setFont(font);

		Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
		destinationLabel.setText(Messages.OpenNTFExport_label_to_dir);
		destinationLabel.setFont(font);

		// destination name entry field
		destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE
				| SWT.BORDER);
		destinationNameField.addListener(SWT.Modify, this);
		destinationNameField.addListener(SWT.Selection, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		destinationNameField.setLayoutData(data);
		destinationNameField.setFont(font);

		// destination browse button
		destinationBrowseButton = new Button(destinationSelectionGroup,
				SWT.PUSH);
		destinationBrowseButton.setText(Messages.OpenNTFExport_button_browse);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setFont(font);
		setButtonLayoutData(destinationBrowseButton);
	}

	protected void createZipNameGroup(Composite parent) {
		Font font = parent.getFont();
		// Zip Name group
		Composite zipNameGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		zipNameGroup.setLayout(layout);
		zipNameGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL));
		zipNameGroup.setFont(font);

		// Zip Name Label field
		Label zipNameLabel = new Label(zipNameGroup, SWT.NONE);
		zipNameLabel.setText(Messages.OpenNTFExport_label_zip_file_name); //$NON-NLS-1$
		zipNameLabel.setFont(font);

		// Zip Name Text field
		zipNameField = new Text(zipNameGroup, SWT.SINGLE | SWT.BORDER);
		zipNameField.addListener(SWT.Modify, this);
		zipNameField.addListener(SWT.Selection, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		zipNameField.setLayoutData(data);
		zipNameField.setFont(font);
	}

	protected void createNoticeGroup(Composite parent) {
		final String sourceMethod = "createNoticeGroup"; //$NON-NLS-1$

		Font font = parent.getFont();
		// Edit Notice group
		Composite noticeGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		noticeGroup.setLayout(layout);
		noticeGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL));
		noticeGroup.setFont(font);

		// Notice label
		Label noticeLabel = new Label(noticeGroup, SWT.NONE);
		noticeLabel.setText(Messages.OpenNTFExport_notice_default);
		noticeLabel.setFont(font);
		noticeLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// Edit Notice button
		getTempNoticeFile(true);
		editNoticeButton = new Button(noticeGroup, SWT.PUSH);
		editNoticeButton.setText(Messages.OpenNTFExport_button_edit_notice);
		editNoticeButton.setFont(font);
		editNoticeButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		editNoticeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				File noticeFile = getTempNoticeFile(false);
				try {
					if (System.getProperty("os.name").contains("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$
						Runtime.getRuntime().exec("notepad.exe " + noticeFile); //$NON-NLS-1$
					} else {
						// Nothing to do.
						// Designer only run on Windows.
					}
				} catch (IOException e) {
					logger.logp(Level.SEVERE, sourceClass, sourceMethod, e
							.getMessage(), e);
				}
			}
		});
	}

	protected void createLicenseGroup(Composite parent) {
		Font font = parent.getFont();
		Composite licenseGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		licenseGroup.setLayout(layout);
		licenseGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL));
		licenseGroup.setFont(font);

		// Select license text
		Label destinationLabel = new Label(licenseGroup, SWT.NONE);
		destinationLabel.setText(Messages.OpenNTFExport_label_select_license);
		destinationLabel.setFont(font);

		// License selection combo
		licenseSelectionCombo = new Combo(licenseGroup, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		licenseSelectionCombo.setItems(LICENSE_ITEMS);
		licenseSelectionCombo.select(0);
		licenseSelectionCombo.setFont(font);
	}

	/**
	 * Create the buttons in the options group.
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {

		Font font = optionsGroup.getFont();

		createLicenseGroup(optionsGroup);

		// Other file checkbox
		otherFileCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		otherFileCheckbox.setText(Messages.OpenNTFExport_otherfile);
		otherFileCheckbox.setSelection(false);
		otherFileCheckbox.setFont(font);
		createOtherFilesGroup(optionsGroup);
		otherFilesGroup.setEnabled(otherFileCheckbox.getSelection());
		otherFileCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (otherFileCheckbox.getSelection()) {
					otherFilesGroup.setEnabled(true);
				} else {
					otherFilesGroup.setEnabled(false);
				}
			}
		});
	}

	protected void createOtherFilesGroup(Composite parent) {
		Font font = parent.getFont();
		// other files group
		otherFilesGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		otherFilesGroup.setLayout(layout);
		otherFilesGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		otherFilesGroup.setFont(font);

		// other files list
		otherFileNamesField = new Text(otherFilesGroup, SWT.MULTI | SWT.BORDER
				| SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		otherFileNamesField.setText("\n\n\n"); //$NON-NLS-1$

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		otherFileNamesField.setLayoutData(data);
		otherFileNamesField.setFont(font);

		// browse other files button
		Button otherFilesButton = new Button(otherFilesGroup, SWT.PUSH);
		otherFilesButton.setText(Messages.OpenNTFExport_button_browse); //$NON-NLS-1$
		otherFilesButton.setFont(font);
		otherFilesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// User has selected to open multiple files
				FileDialog dlg = new FileDialog(new Shell(), SWT.MULTI);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String fn = dlg.open();
				if (fn != null) {
					// Append all the selected files. Since getFileNames()
					// returns only the names, and not the path, prepend
					// the path, normalizing if necessary
					StringBuffer buf = new StringBuffer();
					String[] files = dlg.getFileNames();
					for (int i = 0, n = files.length; i < n; i++) {
						buf.append(dlg.getFilterPath());
						if (buf.charAt(buf.length() - 1) != File.separatorChar) {
							buf.append(File.separatorChar);
						}
						buf.append(files[i]);
						buf.append(OpenNTFConstants.FILE_SEPARATOR_WRITE);
					}
					otherFileNamesField.setText(buf.toString());
				}
			}
		});
	}

	protected void saveWidgetValues() {
		// update directory names history
		IDialogSettings settings = ImportExportPlugin.getDefault()
				.getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings
					.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				return; // ie.- no settings stored
			}

			directoryNames = addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

			// options
			settings.put(STORE_LICENSE_ID, licenseSelectionCombo
					.getSelectionIndex());
			settings.put(STORE_OTHERFILE_ID, otherFileCheckbox.getSelection());
		}
	}

	/**
	 * Hook method for restoring widget values to the values that they held last
	 * time this wizard was used to completion.
	 */
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
			destinationNameField.setText(directoryNames[0]);
			for (int i = 0; i < directoryNames.length; i++) {
				destinationNameField.add(directoryNames[i]);
			}

			// options
			licenseSelectionCombo.select(settings.getInt(STORE_LICENSE_ID));
			otherFileCheckbox.setSelection(settings
					.getBoolean(STORE_OTHERFILE_ID));
			otherFilesGroup.setEnabled(otherFileCheckbox.getSelection());

		}
	}

	protected boolean validateSourceGroup() {
		List<IResource> resourcesToExport = getWhiteCheckedResources();
		List<IResource> testResources = getTestCheckedResources();

		if (resourcesToExport.size() > 0) {
			IResource resource = (IResource) resourcesToExport.get(0);
			String project1 = resource.getProject().getName();
			for (int i = resourcesToExport.size() - 1; i > 0; i--) {
				resource = (IResource) resourcesToExport.get(i);
				String project2 = resource.getProject().getName();
				if (!project1.equals(project2)) {
					setErrorMessage(Messages.OpenNTFExport_warn_select_multi_application);
					return false;
				}
			}

			if (testResources.size() > 0) {
				for (int i = testResources.size() - 1; i > 0; i--) {
					resource = (IResource) testResources.get(i);
					String project2 = resource.getProject().getName();
					if (!project1.equals(project2)) {
						setErrorMessage(Messages.OpenNTFExport_warn_select_multi_application);
						return false;
					}
				}
			}
		}

		return super.validateSourceGroup();
	}

	protected boolean validateDestinationGroup() {
		String zipName = zipNameField.getText();
		if (zipName == null || zipName.equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.OpenNTFExport_warn_specify_zip_name);
			return false;
		}

		List<IResource> resourcesToExport = getWhiteCheckedResources();
		if (resourcesToExport.size() > 0) {
			IResource resource = (IResource) resourcesToExport.get(0);
			String projectName = resource.getProject().getName();
			if (zipName.equalsIgnoreCase(projectName)) {
				setErrorMessage(Messages.OpenNTFExport_warn_zip_name_equal_project_name);
				return false;
			}
		}

		return super.validateDestinationGroup();
	}

	/**
	 * The <code>addToHierarchyToCheckedStore</code> implementation of this
	 * <code>WizardDataTransferPage</code> method returns <code>false</code>.
	 */
	@Override
	protected boolean allowNewContainerName() {
		return false;
	}

	@Override
	/*
	 * * Handle all events and enablements for widgets in this page
	 * 
	 * @param e Event
	 */
	public void handleEvent(Event e) {
		Widget source = e.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}

		updatePageCompletion();
	}

	/**
	 * Open an appropriate destination browser so that the user can specify a
	 * source to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(),
				SWT.SAVE);
		dialog.setMessage(Messages.OpenNTFExport_dialog_create_target_dir_msg);
		dialog.setText(Messages.OpenNTFExport_dialog_create_target_dir_title);
		dialog.setFilterPath(getDestinationValue());
		String selectedDirectoryName = dialog.open();

		if (selectedDirectoryName != null) {
			setErrorMessage(null);
			destinationNameField.setText(selectedDirectoryName);
		}
	}

	/**
	 * Returns this page's collection of currently-specified resources to be
	 * exported. This returns both folders and files - for just the files use
	 * getSelectedResources.
	 * 
	 * @return a collection of resources currently selected for export (element
	 *         type: <code>IResource</code>)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardExportResourcesPage#getWhiteCheckedResources
	 */
	@SuppressWarnings("unchecked")
	protected List<IResource> getWhiteCheckedResources() {

		return this.coreResourceGroup.getAllWhiteCheckedItems();
	}

	@SuppressWarnings("unchecked")
	protected List<IResource> getTestCheckedResources() {

		return this.testResourceGroup.getAllWhiteCheckedItems();
	}

	/**
	 * Save any editors that the user wants to save before export.
	 * 
	 * @return boolean if the save was successful.
	 * 
	 * @see org.eclipse.ui.dialogs.WizardExportResourcesPage#saveDirtyEditors
	 */
	protected boolean saveDirtyEditors() {
		return ImportExportPlugin.getDefault().getWorkbench().saveAllEditors(
				true);
	}

	/**
	 * If the target for export does not exist then attempt to create it. Answer
	 * a boolean indicating whether the target exists (ie.- if it either
	 * pre-existed or this method was able to create it)
	 * 
	 * @return boolean
	 */
	protected boolean ensureTargetIsValid(File targetDirectory) {
		if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
			displayErrorDialog(Messages.OpenNTFExport_error_target_exist_as_file);
			destinationNameField.setFocus();
			return false;
		}

		return ensureDirectoryExists(targetDirectory);
	}

	/**
	 * Attempts to ensure that the specified directory exists on the local file
	 * system. Answers a boolean indicating success.
	 * 
	 * @return boolean
	 * @param directory
	 *            java.io.File
	 */
	protected boolean ensureDirectoryExists(File directory) {
		if (!directory.exists()) {
			if (!queryYesNoQuestion(Messages.OpenNTFExport_query_create_target_dir)) {
				return false;
			}

			if (!directory.mkdirs()) {
				displayErrorDialog(Messages.OpenNTFExport_error_create_target_dir);
				destinationNameField.setFocus();
				return false;
			}
		}

		return true;
	}

	/**
	 * Answer the contents of self's destination specification widget
	 * 
	 * @return java.lang.String
	 */
	protected String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

}
