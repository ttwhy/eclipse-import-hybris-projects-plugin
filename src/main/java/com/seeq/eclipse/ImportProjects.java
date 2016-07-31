package com.seeq.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.equinox.app.IApplicationContext;

public class ImportProjects extends AbstractImportProject implements org.eclipse.ui.IStartup {

    protected static final String ECLIPSEPROJECTFILE = "\\.project";
	private static final String ARG_IMPORT = "-import";

	private String[] getImportPaths() {
        final IApplicationContext iac = getApplicationContext();
        final List<String> importPath = getListOfMatchingParameters(iac, ARG_IMPORT);

        return importPath.toArray(new String[importPath.size()]);
    }

	protected List<String> getListOfMatchingParameters(IApplicationContext iac, final String requestedParameter) {
		String[] args = (String[]) iac.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        List<String> importPath = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.compareToIgnoreCase(requestedParameter) == 0) {
                i++;
                if (i < args.length) {
                    importPath.add(args[i]);
                }
            }
        }
		return importPath;
	}

    private List<File> findFilesRecursively(String path, String pattern, List<File> returnedList) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null)
            return returnedList;

        for (File f : list) {
            if (f.isDirectory()) {
                this.findFilesRecursively(f.getAbsolutePath(), pattern, returnedList);
            }
            else {
                if (Pattern.matches(pattern, f.getName()) == true) {
                    returnedList.add(f);
                }
            }
        }

        return returnedList;
    }

    @Override
    public void earlyStartup() {

        String[] importPaths = this.getImportPaths();

        for (String importPath : importPaths) {
        	LogUtil.info(String.format("Searching for projects in %s", importPath));
            List<File> projectFiles = this.findFilesRecursively(importPath, ECLIPSEPROJECTFILE, new ArrayList<File>());

            importProject(projectFiles);
        }//for importPath
    }
}
