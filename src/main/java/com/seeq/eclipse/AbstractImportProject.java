package com.seeq.eclipse;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public abstract class AbstractImportProject {

	protected IApplicationContext getApplicationContext() {
		BundleContext context = Activator.getContext();
        ServiceReference<?> ser = context.getServiceReference(IApplicationContext.class.getName());
        IApplicationContext iac = (IApplicationContext) context.getService(ser);
		return iac;
	}
	
	/**
	 * @param projectFiles
	 * 		List of ".project" files that should be imported / refreshed
	 */
	protected void importProject(List<File> projectFiles) {
		for (File projectFile : projectFiles) {
		    try {
		        IWorkspace workspace = ResourcesPlugin.getWorkspace();
		        
		        IProjectDescription description = workspace.loadProjectDescription(
		                new Path(projectFile.toString()));
		                                                
		        IProject project = workspace.getRoot().getProject(description.getName());
		        
		        if (project.isOpen() == false) {
		        	LogUtil.info(String.format("Importing project %s %s", description.getName(), description.getLocationURI()));
		            project.create(description, null);
		            project.open(null);
		        } else {
		        	LogUtil.info(String.format("Refreshing project %s %s", description.getName(), description.getLocationURI()));
		            project.refreshLocal(IResource.DEPTH_INFINITE, null);
		        }
		    } catch (CoreException e) {
		    	LogUtil.error(e);
		    }
		}
	}

}
