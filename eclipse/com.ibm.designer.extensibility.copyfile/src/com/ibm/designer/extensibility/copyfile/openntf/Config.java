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
package com.ibm.designer.extensibility.copyfile.openntf;

import org.eclipse.osgi.util.NLS;

public class Config extends NLS {
	private static final String BUNDLE_NAME = "com.ibm.designer.extensibility.copyfile.openntf.config"; //$NON-NLS-1$
	public static String openntf_import_reusablecontrol_website;

	public static String openntf_apache_catalog_rest_name;
	public static String openntf_apache_catalog_rest_url;
	public static String openntf_apache_catalog_importlist_url_pattern;
	public static String openntf_apache_catalog_importlist_url_replacement;

	public static String openntf_gpl_catalog_rest_name;
	public static String openntf_gpl_catalog_rest_url;
	public static String openntf_gpl_catalog_importlist_url_pattern;
	public static String openntf_gpl_catalog_importlist_url_replacement;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Config.class);
	}

	private Config() {
	}
}
