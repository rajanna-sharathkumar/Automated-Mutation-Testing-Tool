package mutation_testingV2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class FileAndReportUtils {

	static String sep = "\n-------------------------------------------------------------------------------";
	static String projectFolderPrefix = null;
	static String reportloc = null;
	static String mutantsloc = null;
	static String mutantprojloc = null;
	static StringBuilder report = new StringBuilder();
	static String projectName = null;
	static String projectCopyLoc = null;

	public static String getProjectFolderPrefix() {
		return projectFolderPrefix;
	}

	public static String getProjectCopyLoc() {
		return projectCopyLoc;
	}

	public static String getProjectName() {
		return projectName;
	}

	public static String getMutantsloc() {
		return mutantsloc;
	}

	public static String getMutantprojloc() {
		return mutantprojloc;
	}

	public static StringBuilder getReport() {
		return report;
	}

	public static String getSep() {
		return sep;
	}

	public static String getReportloc() {
		return reportloc;
	}

	// File Handling Section
	public static void getProjectLocation() throws IOException{
		JFileChooser f = new JFileChooser();
		f.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); 
		f.showSaveDialog(null);


		projectName = f.getSelectedFile().getName();
		report.append("Project Name ; "+ FileAndReportUtils.getProjectName());

		projectFolderPrefix = f.getSelectedFile().getAbsolutePath();
		report.append("\nProject Location ; "+getProjectFolderPrefix() );

		projectCopyLoc = getProjectFolderPrefix()+ File.separator+projectName+"_copy";

		reportloc = projectFolderPrefix + File.separator +"report";
		report.append("\nReport Location ; "+reportloc);

		mutantsloc = projectFolderPrefix + File.separator +"mutants" ;
		report.append("\nMutants Location ; "+getMutantsloc());

		mutantprojloc = projectFolderPrefix + File.separator + "MutatedProjects";
		report.append("\nMutated Projects Location ; "+getMutantprojloc());

		checkAndDelete();

		FileUtils.copyDirectory(new File(getProjectFolderPrefix()), new File(getProjectFolderPrefix()+ File.separator+projectName+"_copy"));

		// create Report Directory
		createDir(reportloc);

		// create Mutants Directory
		createDir(getMutantsloc());

		// create mutates projects directory
		createDir(getMutantprojloc());

		FileUtils.copyDirectory(new File(getProjectFolderPrefix()), new File(getProjectFolderPrefix()+ File.separator+projectName+"_copy"));
		addSep();
	}


	public static void checkAndDelete() throws IOException{
		File file = new File(projectCopyLoc);
		buildReport(sep);
		buildReport("Checking and deleting exiting files");
		if(file.exists()){
			buildReport("Deleting: "+projectCopyLoc);
			delete(file);
		}

		file = new File(reportloc);
		if(file.exists()){
			buildReport("Deleting: "+reportloc);
			delete(file);
		}

		file = new File(mutantsloc);
		if(file.exists()){
			buildReport("Deleting: "+mutantsloc);
			delete(file);
		}

		file = new File(mutantprojloc);
		if(file.exists()){
			buildReport("Deleting: "+mutantprojloc);
			delete(file);
		}
		buildReport(sep);

	}
	public static void addSep(){
		report.append(sep);
	}

	public static void createProjectCopy(int copyNumber) throws IOException {
		FileUtils.copyDirectory(new File(getProjectCopyLoc()), new File(getMutantprojloc() + File.separator+ getProjectName()+ "-" + copyNumber));
	}

	public static void deleteProjectCopy(int copyNumber) throws IOException {
		FileUtils.deleteDirectory(new File(getMutantprojloc() + File.separator+ getProjectName()+ "-" + copyNumber));
	}

	public static void createDir(String loc) throws IOException{
		File theDir = new File(loc);
		if (theDir.exists()) {
			delete(theDir);
		}

		try{
			theDir.mkdir();
		} 
		catch(SecurityException se){
			//handle it
			report.append(se.fillInStackTrace());
		}        


	}

	public static void delete(File file)
			throws IOException{

		if(file.isDirectory()){

			//directory is empty, then delete it
			if(file.list().length==0){

				file.delete();
				//				System.out.println("Directory is deleted : "
				//						+ file.getAbsolutePath());

			}else{

				//list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(file, temp);

					//recursive delete
					delete(fileDelete);
				}

				//check the directory again, if empty then delete it
				if(file.list().length==0){
					file.delete();
					//					System.out.println("Directory is deleted : "
					//							+ file.getAbsolutePath());
				}
			}

		}else{
			//if file, then delete it
			file.delete();
			//			System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}
	public static Collection<File> getFileList() {

		// All project files with code EXCLUDING test files.
		Collection<File> files = FileUtils.listFiles(new File(getProjectFolderPrefix() + File.separator + "src" + File.separator
				+ "main" + File.separator + "java"), new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);

		report.append("\n Got Files List");
		addSep();
		return files;
	}

	// Report handling section
	public static void buildReport(String str){
		report.append("\n"+str);
	}

	public static void printReport(){
		System.out.println(report);
	}
	public static void commitReport() throws IOException {

		// Save the report in Project folder

		File f = new File(reportloc + File.separator + "MutationInsertionReport.txt");
		if (f.exists() && !f.isDirectory()) {
			// delete previously generated report
			delete(f);
		}
		FileUtils.writeStringToFile(f, report.toString(), true);


		// Save the report in cuurent folder
		f = new File(new File(".").getCanonicalPath() + File.separator + "MutationReport.txt");
		if (f.exists() && !f.isDirectory()) {
			// delete previously generated report
			delete(f);
		}
		FileUtils.writeStringToFile(f, report.toString(), true);
	}

}
