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

package com.ibm.designer.extensibility.copyfile.openntf.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.json.JSONException;

import com.ibm.designer.extensibility.copyfile.openntf.Config;
import com.ibm.designer.extensibility.copyfile.openntf.beans.Catalog;
import com.ibm.designer.extensibility.copyfile.openntf.rest.ControlsPeek;

public class TreeFactory {
	private static ArrayList<Catalog> catalogList = null;

	public static List<Catalog> createTree() throws JSONException, IOException {
		if (catalogList == null) {
			ControlsPeek controlsPeek = new ControlsPeek();
			catalogList = new ArrayList<Catalog>();

			Catalog apacheCatalog = new Catalog(
					Config.openntf_apache_catalog_rest_name,
					Config.openntf_apache_catalog_rest_url,
					Config.openntf_apache_catalog_importlist_url_pattern,
					Config.openntf_apache_catalog_importlist_url_replacement);
			apacheCatalog.setChildren(controlsPeek
					.getControlsList(apacheCatalog));
			if (apacheCatalog.getChildren().size() > 0) {
				catalogList.add(apacheCatalog);
			}

			Catalog gplCatalog = new Catalog(
					Config.openntf_gpl_catalog_rest_name,
					Config.openntf_gpl_catalog_rest_url,
					Config.openntf_gpl_catalog_importlist_url_pattern,
					Config.openntf_gpl_catalog_importlist_url_replacement);
			gplCatalog.setChildren(controlsPeek.getControlsList(gplCatalog));
			if (gplCatalog.getChildren().size() > 0) {
				catalogList.add(gplCatalog);
			}
		}
		return catalogList;
	}
}
