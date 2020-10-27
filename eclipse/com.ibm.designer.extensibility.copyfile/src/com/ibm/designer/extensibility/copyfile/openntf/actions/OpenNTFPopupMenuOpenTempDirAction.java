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
package com.ibm.designer.extensibility.copyfile.openntf.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.openntf.internal.PathUtil;

public class OpenNTFPopupMenuOpenTempDirAction implements IObjectActionDelegate {
	private static final Logger logger = ImportExportPlugin.getLogger();

	@Override
	public void setActivePart(IAction action, IWorkbenchPart workbenchpart) {
	}

	@Override
	public void run(IAction action) {
		try {
			IPath tempPath = PathUtil.getImportTempDir();
			Runtime.getRuntime().exec("cmd /c start " + tempPath); //$NON-NLS-1$
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
