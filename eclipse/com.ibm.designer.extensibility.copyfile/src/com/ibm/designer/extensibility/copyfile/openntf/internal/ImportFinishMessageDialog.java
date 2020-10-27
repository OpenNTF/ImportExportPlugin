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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;

public class ImportFinishMessageDialog extends MessageDialog {
	private static final Logger logger = ImportExportPlugin.getLogger();
	/**
	 * Labels for buttons in the button bar (localized strings).
	 */
	private String[] buttonLabels;

	/**
	 * The buttons. Parallels <code>buttonLabels</code>.
	 */
	private Button[] buttons;

	/**
	 * Index into <code>buttonLabels</code> of the default button.
	 */
	private int defaultButtonIndex;

	private int tempDirIndex;

	public ImportFinishMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex, int tempDirIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.buttonLabels = dialogButtonLabels;
		this.defaultButtonIndex = defaultIndex;
		this.tempDirIndex = tempDirIndex;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		buttons = new Button[buttonLabels.length];
		for (int i = 0; i < buttonLabels.length; i++) {
			String label = buttonLabels[i];
			Button button = createButton(parent, i, label,
					defaultButtonIndex == i);
			buttons[i] = button;
		}

		Button button = buttons[tempDirIndex];
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					IPath tempPath = PathUtil.getImportTempDir();
					Runtime.getRuntime().exec("cmd /c start " + tempPath); //$NON-NLS-1$
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		});
	}

}
