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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.datatransfer.FileSystemImportWizard;

import com.ibm.designer.extensibility.copyfile.resources.Messages;

/**
 * @author Guo Yi(guoyibj@cn.ibm.com)
 */
public class ImportWizard extends FileSystemImportWizard {
	private ImportWizardPage mainPage = null;
	private IStructuredSelection selection = null;
	private IWorkbench workbench = null;

	public ImportWizard() {
	}

	public ImportWizard(IWorkbench workbench, ISelection selection) {
		this.workbench = workbench;
		this.selection = (IStructuredSelection) selection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	@SuppressWarnings("restriction")
	public boolean performFinish() {
		return mainPage.finish();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.FileImport_WindowTitle);
		setNeedsProgressMonitor(true);
		this.workbench = workbench;
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages() {
		mainPage = new ImportWizardPage(Messages.FileImport_PageTitle,
				workbench, selection);
		addPage(mainPage);
	}

}
