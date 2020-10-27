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
package com.ibm.designer.extensibility.copyfile.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.json.JSONException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import com.ibm.designer.extensibility.copyfile.openntf.beans.Control;
import com.ibm.designer.extensibility.copyfile.openntf.tree.TreeContentProvider;
import com.ibm.designer.extensibility.copyfile.openntf.tree.TreeFactory;
import com.ibm.designer.extensibility.copyfile.openntf.tree.TreeLabelProvider;

public class TestTreeViewer {

	private static Text description;
	private static ContainerCheckedTreeViewer treeViewer;

	public static void main(String[] args) {
		final Display display = Display.getDefault();
		final Shell shell = new Shell();
		shell.setSize(500, 375);
		shell.setText("SWT Application"); //$NON-NLS-1$

		// Create tree viewer
		treeViewer = new ContainerCheckedTreeViewer(shell, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		Tree tree = treeViewer.getTree();
		tree.setBounds(83, 75, 264, 185);

		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setContentProvider(new TreeContentProvider());
		try {
			treeViewer.setInput(TreeFactory.createTree());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				handleSelectionChanged((IStructuredSelection) e.getSelection());
			}
		});

		treeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent e) {
				Object[] objs = treeViewer.getCheckedElements();
				System.out.println(objs.length);
			}
		});

		// Control description
		description = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI
				| SWT.V_SCROLL);
		description.setEditable(false);

		// Open viewer
		shell.open();
		shell.setLayout(new FillLayout());
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private static void handleSelectionChanged(IStructuredSelection ssel) {
		Object item = ssel.getFirstElement();
		StringBuffer strbuf = new StringBuffer(""); //$NON-NLS-1$
		if (item instanceof Control) {
			Control ctl = (Control) item;
			strbuf.append(ctl.getName());
			strbuf.append("\n\nDescription: ").append(ctl.getDescription()); //$NON-NLS-1$
			strbuf.append("\n\nDevelopers: ").append(ctl.getDevelopers()); //$NON-NLS-1$
			strbuf.append("\n\nLastUpdated: ").append(ctl.getLastUpdated()); //$NON-NLS-1$
			URL url = null;
			try {
				url = new URL(ctl.getZipFile());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			System.out.println("url = " + url); //$NON-NLS-1$
			if (url != null && !url.equals("")) { //$NON-NLS-1$
				System.out.println("filename = " + url.getFile().substring( //$NON-NLS-1$
						url.getFile().lastIndexOf("=") + 1)); //$NON-NLS-1$
			}
			if(ctl.getHasImportlist()){
				System.out.println("importlist = " + ctl.getImportListUrl()); //$NON-NLS-1$
			}
		}
		description.setText(strbuf.toString());
	}
}
