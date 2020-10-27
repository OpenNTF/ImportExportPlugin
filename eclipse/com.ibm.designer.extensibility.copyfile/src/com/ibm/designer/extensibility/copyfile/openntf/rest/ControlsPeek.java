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
package com.ibm.designer.extensibility.copyfile.openntf.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;

import com.ibm.designer.extensibility.copyfile.ImportExportPlugin;
import com.ibm.designer.extensibility.copyfile.openntf.Config;
import com.ibm.designer.extensibility.copyfile.openntf.beans.Catalog;
import com.ibm.designer.extensibility.copyfile.openntf.beans.Control;
import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

/**
 * @author Yang Jun Chen(cyangjun@cn.ibm.com), Guo Yi(guoyibj@cn.ibm.com)
 */
public class ControlsPeek {

	private static final Logger logger = ImportExportPlugin.getLogger();
	private static final String sourceClass = "ControlsPeek"; //$NON-NLS-1$

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public ArrayList<Control> getControlsList(Catalog catalog)
			throws JSONException, IOException {
		String sourceMethod = "getControlsList"; //$NON-NLS-1$
		logger.entering(sourceClass, sourceMethod);

		ArrayList<Control> controlsList = new ArrayList<Control>();
		JSONArray jArray = null;
		HttpURLConnection urlc = null;
		InputStream istream = null;
		try {
			URL url = new URL(catalog.getUrl());
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("Content-type", "text/xml; charset=" //$NON-NLS-1$ //$NON-NLS-2$
					+ "UTF-8"); //$NON-NLS-1$
			urlc.connect();
			istream = urlc.getInputStream();
			jArray = new JSONArray(istream);

			for (int i = 0; i < jArray.size(); i++) {
				Control ctl = new Control();
				JSONObject obj = (JSONObject) jArray.get(i);
				ctl.setUnid((String) obj.get("@unid")); //$NON-NLS-1$
				ctl.setName((String) obj.get("Name")); //$NON-NLS-1$
				ctl.setDescription((String) obj.get("Description")); //$NON-NLS-1$
				ctl.setDevelopers((String) obj.get("Developers")); //$NON-NLS-1$
				ctl.setLastUpdated((String) obj.get("LastUpdated")); //$NON-NLS-1$
				String zipFile = (String) obj.get("ZipFile"); //$NON-NLS-1$
				if (zipFile.equals("")) { //$NON-NLS-1$
					ctl.setZipFile(zipFile);
				} else {
					ctl.setZipFile(OpenNTFConstants.ZIP_FILE_URL_PREFIX
							+ zipFile);
				}
				String hasImportlist = (String) obj.get("HasImportlist"); //$NON-NLS-1$
				if (hasImportlist.equalsIgnoreCase("yes") || hasImportlist.equalsIgnoreCase("y")) { //$NON-NLS-1$ //$NON-NLS-2$
					ctl.setHasImportlist(true);
					String importlistUrl = catalog.getImportlistUrlPattern()
							.replaceFirst(
									catalog.getImportlistUrlReplacement(),
									ctl.getUnid());
					ctl.setImportListUrl(importlistUrl);
				} else {
					ctl.setHasImportlist(false);
					ctl.setImportListUrl(""); //$NON-NLS-1$
				}

				controlsList.add(ctl);
			}
		} finally {
			if (istream != null) {
				try {
					istream.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			if (urlc != null) {
				urlc.disconnect();
			}
		}

		logger.exiting(sourceClass, sourceMethod);
		return controlsList;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ControlsPeek controlsPeek = new ControlsPeek();
		ArrayList<Control> controlsList = null;
		try {
			Catalog apacheCatalog = new Catalog(
					Config.openntf_apache_catalog_rest_name,
					Config.openntf_apache_catalog_rest_url,
					Config.openntf_apache_catalog_importlist_url_pattern,
					Config.openntf_apache_catalog_importlist_url_replacement);
			controlsList = controlsPeek.getControlsList(apacheCatalog);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < controlsList.size(); i++) {
			Control ctl = controlsList.get(i);
			System.out.println(i);
			System.out.println("@unid: " + ctl.getUnid()); //$NON-NLS-1$
			System.out.println("Name: " + ctl.getName()); //$NON-NLS-1$
			System.out.println("Description: " + ctl.getDescription()); //$NON-NLS-1$
			System.out.println("Developers: " + ctl.getDevelopers()); //$NON-NLS-1$
			System.out.println("ZipFile: " + ctl.getZipFile()); //$NON-NLS-1$
			System.out.println("LastUpdated: " + ctl.getLastUpdated()); //$NON-NLS-1$
		}
	}
}
