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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MergeUtil {

	public File mergeClassPath(InputStream baseStream, File mergeFile,
			File resultFile) throws ParserConfigurationException, SAXException,
			IOException, TransformerException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

		// Parse the base classpath
		Document baseDoc = docBuilder.parse(baseStream);
		ArrayList<String> basePathList = new ArrayList<String>();
		Element baseRoot = baseDoc.getDocumentElement();
		NodeList baseEntries = baseRoot.getElementsByTagName("classpathentry"); //$NON-NLS-1$
		for (int i = 0; i < baseEntries.getLength(); i++) {
			Element baseEntry = (Element) baseEntries.item(i);
			basePathList.add(baseEntry.getAttribute("path")); //$NON-NLS-1$
		}

		// Merge classpath
		Document mergeDoc = docBuilder.parse(mergeFile);
		NodeList mergeEntries = mergeDoc.getDocumentElement()
				.getElementsByTagName("classpathentry"); //$NON-NLS-1$
		for (int j = 0; j < mergeEntries.getLength(); j++) {
			Element mergeEntry = (Element) mergeEntries.item(j);
			if (!basePathList.contains(mergeEntry.getAttribute("path"))) { //$NON-NLS-1$
				Node node = baseDoc.createTextNode("  "); //$NON-NLS-1$
				baseRoot.appendChild(node);
				node = baseDoc.importNode(mergeEntry, true);
				baseRoot.appendChild(node);
				node = baseDoc.createTextNode("\n"); //$NON-NLS-1$
				baseRoot.appendChild(node);
			}
		}

		// Output the result doc to a temporary file
		if (resultFile == null) {
			resultFile = File.createTempFile("TMP", ".classpath", PathUtil //$NON-NLS-1$ //$NON-NLS-2$
					.getImportTempDir().toFile());
		} else if (!resultFile.exists()) {
			resultFile.createNewFile();
		}
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(resultFile);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(baseDoc);
			Result result = new StreamResult(outputStream);
			transformer.transform(source, result);
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
		return resultFile;
	}

}
