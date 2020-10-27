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
package com.ibm.designer.extensibility.copyfile.openntf.tree;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	@Override
	@SuppressWarnings( { "unchecked" })
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			List input = (List) inputElement;
			return input.toArray();
		}
		return new Object[0];
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object[] getChildren(Object parentElement) {
		Itree node = (Itree) parentElement;
		List list = node.getChildren();
		if (list == null) {
			return new Object[0];
		}
		return list.toArray();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean hasChildren(Object element) {
		Itree node = (Itree) element;
		List list = node.getChildren();
		return !(list == null || list.isEmpty());
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}
}