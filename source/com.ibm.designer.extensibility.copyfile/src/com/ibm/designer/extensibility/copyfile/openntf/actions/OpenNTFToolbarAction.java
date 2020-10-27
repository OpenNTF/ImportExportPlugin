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
package com.ibm.designer.extensibility.copyfile.openntf.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ui.commons.IDesignerSelection;
import com.ibm.designer.domino.ui.commons.extensions.DesignerResource;
import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.openntf.internal.PathUtil;
import com.ibm.designer.extensibility.copyfile.openntf.wizards.OpenNTFExportWizard;
import com.ibm.designer.extensibility.copyfile.openntf.wizards.OpenNTFImportWizard;
import com.ibm.designer.extensibility.copyfile.resources.Messages;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;
import com.ibm.designer.extensibility.copyfile.wizards.ExportWizard;
import com.ibm.designer.extensibility.copyfile.wizards.ImportWizard;

public class OpenNTFToolbarAction implements IWorkbenchWindowPulldownDelegate2 {
	private static final Logger logger = ImportExportPlugin.getLogger();

	private Menu menu = null;
	private IWorkbenchWindow window = null;
	private ISelection selection = null;

	public Menu createMenu(Menu menu) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.OpenNTF_Import_Menu_Title);
		menuItem.setImage(ImportExportPlugin
				.getImage(OpenNTFConstants.IMPORT_OPENNTF_ICON));
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showImportOpenntfWizard();
			}
		});
		menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.OpenNTF_Export_Menu_Title);
		menuItem.setImage(ImportExportPlugin
				.getImage(OpenNTFConstants.EXPORT_OPENNTF_ICON));
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showExportOpenntfWizard();
			}
		});
		menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.OpenNTFImport_TempDir_button);
		menuItem.setImage(ImportExportPlugin
				.getImage(OpenNTFConstants.TEMP_FOLDER_ICON));
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					IPath tempPath = PathUtil.getImportTempDir();
					Runtime.getRuntime().exec("cmd /c start " + tempPath); //$NON-NLS-1$
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.FileResource_Import_Menu_Title);
		menuItem.setImage(ImportExportPlugin
				.getImage(OpenNTFConstants.IMPORT_RESOURCE_ICON));
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showImportResourceWizard();
			}
		});
		menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.FileResource_Export_Menu_Title);
		menuItem.setImage(ImportExportPlugin
				.getImage(OpenNTFConstants.EXPORT_RESOURCE_ICON));
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showExportResourceWizard();
			}
		});
		return menu;
	}

	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	public Menu getMenu(Menu parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		return createMenu(menu);
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		return createMenu(menu);
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		showImportOpenntfWizard();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		action.setEnabled(false);

		// selection should be of IStructuredSelection
		if (!(selection instanceof IStructuredSelection))
			return;

		if (((IStructuredSelection) selection).size() > 1)
			return;

		Object obj = getSelectedObject(selection);

		if (obj instanceof DesignerProject)
			action.setEnabled(true);
	}

	private Object getSelectedObject(ISelection selection) {
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof IDesignerSelection)
			obj = ((IDesignerSelection) obj).getSelectionObject();

		if (obj instanceof IResource) {
			if (obj instanceof IProject) {
				obj = DesignerResource.getDesignerProject((IProject) obj);
			} else {
				if (((IResource) obj).exists())
					obj = DesignerResource.getDesignElement((IResource) obj);
			}
		}
		return obj;
	}

	private void showImportOpenntfWizard() {
		Shell shell = new Shell(window.getShell());
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		WizardDialog dialog = new WizardDialog(shell, new OpenNTFImportWizard(
				window.getWorkbench(), selection));
		dialog.open();
	}

	private void showExportOpenntfWizard() {
		Shell shell = new Shell(window.getShell());
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		WizardDialog dialog = new WizardDialog(shell, new OpenNTFExportWizard(
				selection));
		dialog.open();
	}

	private void showImportResourceWizard() {
		Shell shell = new Shell(window.getShell());
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		WizardDialog dialog = new WizardDialog(shell, new ImportWizard(window
				.getWorkbench(), selection));
		dialog.open();
	}

	private void showExportResourceWizard() {
		Shell shell = new Shell(window.getShell());
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		WizardDialog dialog = new WizardDialog(shell, new ExportWizard(
				selection));
		dialog.open();
	}

}
