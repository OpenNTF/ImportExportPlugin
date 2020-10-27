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
package com.ibm.designer.extensibility.copyfile.openntf.beans;

import java.util.ArrayList;
import java.util.List;

import com.ibm.designer.extensibility.copyfile.openntf.tree.Itree;

public class Catalog implements Itree<Control> {
	private String name;
	private String url;
	private String importlistUrlPattern;
	private String importlistUrlReplacement;
	private List<Control> children = new ArrayList<Control>();

	public Catalog() {
	}

	public Catalog(String name, String url, String importlistUrlPattern, String importlistUrlReplacement) {
		this.name = name;
		this.url = url;
		this.importlistUrlPattern = importlistUrlPattern;
		this.importlistUrlReplacement = importlistUrlReplacement;
	}

	@Override
	public List<Control> getChildren() {
		return children;
	}

	@Override
	public void setChildren(List<Control> children) {
		this.children = children;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setImportlistUrlPattern(String importlistUrlPattern) {
		this.importlistUrlPattern = importlistUrlPattern;
	}

	public String getImportlistUrlPattern() {
		return importlistUrlPattern;
	}

	public void setImportlistUrlReplacement(String importlistUrlReplacement) {
		this.importlistUrlReplacement = importlistUrlReplacement;
	}

	public String getImportlistUrlReplacement() {
		return importlistUrlReplacement;
	}


}
