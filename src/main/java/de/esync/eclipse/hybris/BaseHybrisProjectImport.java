package de.esync.eclipse.hybris;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.seeq.eclipse.ImportProjects;
import com.seeq.eclipse.LogUtil;

/**
 * @author Jan Riewe 
 */
public class BaseHybrisProjectImport extends ImportProjects {
	private static final String UNEXPECTED_XML_STRUCTURE = "Unexpected XML Structure";
	private static final String ARG_HYBRIS_ROOT = "-importHybris";
	private File HYBRIS_HOME;

	Document extensionsDoc = null;
	XPathExpression extensionsExpr = null;

	@Override
	public void earlyStartup() {
		List<String> parameterHybrisHomeDirectory = getListOfMatchingParameters(getApplicationContext(), ARG_HYBRIS_ROOT);
		if (parameterHybrisHomeDirectory.size() == 1){
			HYBRIS_HOME = new File(parameterHybrisHomeDirectory.get(0));
			if(HYBRIS_HOME.exists() && HYBRIS_HOME.isDirectory()){
				File localExtensions = new File(HYBRIS_HOME.getAbsolutePath()+File.separator+"config"+File.separator+"localextensions.xml");
				if(localExtensions.exists() && localExtensions.isFile())
					importUsingXML(localExtensions);
			}
		}
		super.earlyStartup();
	}

	private void importUsingXML(final File localExtensionsXML) {
		try {
			prepareDocumentForParsing(localExtensionsXML);
			// run the query and get a nodeset
			final Object matchingNodes = extensionsExpr.evaluate(extensionsDoc, XPathConstants.NODESET);
			if(matchingNodes instanceof NodeList){
				List<String> extensionDir = extractExtensionLocations((NodeList) matchingNodes);
				
				final List<File> locateExtensions = getExtensionByDescription(extensionDir);
				importProject(this.getProjectFile(locateExtensions));
			} else
				throw new ParserConfigurationException(UNEXPECTED_XML_STRUCTURE);
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}
	
	private void prepareDocumentForParsing(File localExtensionsXML)
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		extensionsDoc = builder.parse(localExtensionsXML);
		
		if(extensionsExpr == null){
			final XPathFactory xFactory = XPathFactory.newInstance();
			final XPath xpath = xFactory.newXPath();
			extensionsExpr = xpath.compile("//extension");
		}
	}

	private List<String> extractExtensionLocations(final NodeList extNodes) {
		List<String> extensionDir = new LinkedList<String>();
		for (int i = 0; i < extNodes.getLength(); i++) {
			final Node extensionNode = extNodes.item(i);
			final String extLocations = extensionNode.getAttributes().getNamedItem("dir").getNodeValue();
			extensionDir.add(extLocations);
		}
		return extensionDir;
	}

	private List<File> getExtensionByDescription(final List<String> relativeExtensionLocations) {
		List<File> extensions = new LinkedList<File>();
		
		for(String extLoc : clearExtensionNames(relativeExtensionLocations)){
			File ext = new File(HYBRIS_HOME + File.separator + "bin"+ File.separator +extLoc.replaceAll("/", File.separator));
			if(ext.exists())
				extensions.add(ext);
			else
				LogUtil.error("Failed to locate extension: " + extLoc);
		}
		
		return extensions;
	}
	
	private List<File> getProjectFile(final List<File> extensionDirectories){
		final List<File> projectFiles = new LinkedList<File>();
		for (File file : extensionDirectories) {
			final File projectFile = new File(file.getAbsolutePath()+File.separator+".project");
			if(projectFile.exists())
				projectFiles.add(projectFile);
			// TODO add deeper search for extensions.
			else 
				LogUtil.error("Missing project file for "+ projectFile);
			
		}
		return projectFiles;
	}

	private List<String> clearExtensionNames(final List<String> extensionLocations) {
		final List<String> cleanExtensions = new LinkedList<String>();
		
		for (String extLoc : extensionLocations) {
			if(extLoc.contains("$")){
				extLoc = extLoc.substring(extLoc.indexOf("/"));
			}
			cleanExtensions.add(extLoc);
		}
		return cleanExtensions;
	}
}