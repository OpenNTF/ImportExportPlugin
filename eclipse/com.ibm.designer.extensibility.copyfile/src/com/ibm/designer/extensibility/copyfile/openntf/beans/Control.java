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
package com.ibm.designer.extensibility.copyfile.openntf.beans;

import java.util.List;
import com.ibm.designer.extensibility.copyfile.openntf.tree.Itree;

public class Control implements Itree<Object> {

	private String unid;
	private String name;
	private String description;
	private String lastUpdated;
	private String developers;
	private String zipFile;
	private String importListUrl;
	private boolean hasImportlist;

	public Control() {
	}

	public List<Object> getChildren() {
		return null;
	}

	public void setChildren(List<Object> children) {
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getDevelopers() {
		return developers;
	}

	public void setDevelopers(String developers) {
		this.developers = developers;
	}

	public String getZipFile() {
		return zipFile;
	}

	public void setZipFile(String zipFile) {
		this.zipFile = zipFile;
	}
	
	public boolean getHasImportlist(){
		return hasImportlist;
	}
	
	public void setHasImportlist(boolean hasImportlist){
		this.hasImportlist = hasImportlist;
	}

	public void setImportListUrl(String importListUrl) {
		this.importListUrl = importListUrl;
	}

	public String getImportListUrl() {
		return importListUrl;
	}
}
