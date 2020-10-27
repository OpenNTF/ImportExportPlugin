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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.ibm.designer.extensibility.copyfile.openntf.wizards.OpenNTFExportWizard;

public class OpenNTFPopupMenuExportAction implements IObjectActionDelegate {

	private ISelection selection;
	private IWorkbenchWindow window;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart workbenchpart) {
		this.window = workbenchpart.getSite().getWorkbenchWindow();
	}

	@Override
	public void run(IAction action) {
		Shell shell = new Shell(window.getShell());
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		WizardDialog dialog = new WizardDialog(shell, new OpenNTFExportWizard(
				selection));
		dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
