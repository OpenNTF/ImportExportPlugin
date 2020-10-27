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
package com.ibm.designer.extensibility.copyfile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ibm.designer.extensibility.copyfile.resources.OpenNTFConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ImportExportPlugin extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.ibm.designer.extensibility.copyfile"; //$NON-NLS-1$

	// The shared instance
	private static ImportExportPlugin plugin;

	private static File tempDir = null;

	/**
	 * The constructor
	 */
	public ImportExportPlugin() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ImportExportPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param location
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String location) {
		String iconPath = OpenNTFConstants.ICON_DIR;
		URL imageUrl = getDefault().getBundle().getEntry(iconPath + location);
		if (imageUrl != null) {
			return ImageDescriptor.createFromURL(imageUrl);
		}
		return imageDescriptorFromPlugin(PLUGIN_ID, location);
	}

	public static Image getImage(String location) {
		Image ret = getDefault().getImageRegistry().get(location);
		if (ret == null) {
			ImageDescriptor imgDesc = getImageDescriptor(location);
			if (imgDesc != null) {
				ret = imgDesc.createImage();
				getDefault().getImageRegistry().put(location, ret);
			}
		}
		return ret;
	}

	/**
	 * Return the root directory of the plug-in. This code does not work in a
	 * packaged deployment. To get this working, you must set the unpack
	 * attribute in your feature for the plug-in.
	 */
	public static File getRootDirectory() throws IOException,
			URISyntaxException {
		URL bundleRoot = getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
		URL fileURL = FileLocator.resolve(bundleRoot);
		return new File(fileURL.getFile());
	}

	public static Logger getLogger() {
		return Logger.getLogger(PLUGIN_ID);
	}

	public static File getTempDir() {
		if (tempDir == null) {
			tempDir = ImportExportPlugin.getDefault().getStateLocation()
					.append(OpenNTFConstants.TEMP_FOLDER).toFile();
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
		}
		return tempDir;
	}

}
