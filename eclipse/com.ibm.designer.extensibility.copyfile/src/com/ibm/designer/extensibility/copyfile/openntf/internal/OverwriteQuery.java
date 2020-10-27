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

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

import com.ibm.designer.extensibility.copyfile.resources.Messages;

@SuppressWarnings("restriction")
public class OverwriteQuery implements IOverwriteQuery {
	private int result = 0;

	/**
	 * The <code>OverwriteQuery</code> implementation of this
	 * <code>IOverwriteQuery</code> method asks the user whether the existing
	 * resource at the given path should be overwritten.
	 *
	 * @param pathString
	 * @return the user's reply: one of <code>"YES"</code>, <code>"NO"</code>,
	 *         <code>"ALL"</code>, or <code>"CANCEL"</code>
	 * @see org.eclipse.ui.dialogs.WizardDataTransferPage.queryOverwrite()
	 */
	@Override
	public String queryOverwrite(String pathString) {
		Path path = new Path(pathString);
		final String messageString;
		// Break the message up if there is a file name and a directory
		// and there are at least 2 segments.
		if (path.getFileExtension() == null || path.segmentCount() < 2) {
			messageString = NLS.bind(Messages.OverwriteQuery_existsQuestion,
					pathString);
		} else {
			messageString = NLS.bind(
					Messages.OverwriteQuery_overwriteNameAndPathQuestion, path
							.lastSegment(), path.removeLastSegments(1)
							.toOSString());
		}

		// run in syncExec because callback is from an operation,
		// which is probably not running in the UI thread.
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog dialog = new MessageDialog(null,
						IDEWorkbenchMessages.Question, null, messageString,
						MessageDialog.QUESTION, new String[] {
								IDialogConstants.YES_LABEL,
								IDialogConstants.YES_TO_ALL_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.NO_TO_ALL_LABEL,
								IDialogConstants.CANCEL_LABEL }, 0);
				dialog.open();
				result = dialog.getReturnCode();
			}
		});

		String[] response = new String[] { YES, ALL, NO, NO_ALL, CANCEL };
		return result < 0 ? CANCEL : response[result];
	}
}
