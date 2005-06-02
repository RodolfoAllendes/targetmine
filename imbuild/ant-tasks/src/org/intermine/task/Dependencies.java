package org.intermine.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * 
 * 
 * @author Thomas Riley
 */
public class Dependencies extends Task
{
    /** Base directory that all projects are relative to. */
    private String workspaceBaseDir;
    /** Main classpath. */
    private Path mainPath;
    /** Main classpath represented as FileSet. */
    private FileSet mainFileSet;
    /** Target to run for each dependent project (optional). */
    private String target;
    /**  */
    private String pathid = "main.class.path";
    /** Whether or not to build project dependencies (default is true). */
    private boolean build = true;
    /** Whether or not to fully traverse dependency graph (default is false). */
    private boolean nofollow = false;
    /** Type of depenency to load from project.properties (default is "compile"). */
    private String type = "compile";
    
    /**
     * Base directory that all projects are assumed relative to.
     * 
     * @param basedir base directory that all projects are assumed relative to
     */
    public void setBasedir(String basedir) {
        workspaceBaseDir = basedir;
    }
    
    /**
     * Set the ant target to execute on each dependency. Targets are not executed
     * if any of the following are true:
     * <ul>
     * <li>buildDependencies is set to false</li>
     * <li>the property "no.dep" is set to "true" (the value of this
     * property is set by the Dependencies task and should not be set or altered</li>
     * <li>nofollow is set to true</li>
     * </ul>
     * 
     * @param target target to run on each dependency
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * Set whether or not to build project dependencies (default is true).
     * 
     * @param build false to disable building of dependencies
     */
    public void setBuildDependencies(boolean build) {
        this.build = build;
    }
    
    /**
     * Set whether or not to visit dependencies of dependencies. Default is false - the
     * enture dependency graph is searched.
     * 
     * @param nofollow true to disable traversal of the dependency graph and to only
     *                 look at the immediate project dependencies
     */
    public void setNoFollow(boolean nofollow) {
        this.nofollow = nofollow;
    }
    
    /**
     * Specify an alternative path id. The default is main.class.path. The fileset id
     * will be this value with ".fileset" appended.
     * 
     * @param id id for class path
     */
    public void setPathid(String id) {
        pathid = id;
    }
    
    /**
     * Set the dependency type. This is basically a way to alter the property read from
     * project.properties identifying project dependencies. The name of the property
     * read will by TYPE.dependencies where, by default, TYPE is "compile". Setting this
     * property to some non-default also has the side-effect of stopping the following of
     * dependencies (in other words it sets nofollow to true).
     * 
     * @param type type of dependencies to load from project.properties
     */
    public void setType(String type) {
        this.type = type;
    }
    
    protected boolean shouldExecute() {
        return false;
    }
    
    protected boolean shouldFollow() {
        return false;
    }
    
    /**
     * Execute the task.
     * 
     * @throws BuildException if something goes wrong
     */
    public void execute() throws BuildException {
        if (workspaceBaseDir == null) {
            throw new BuildException("basedir attribute required");
        }
        
        // Don't run twice if target not specified.
        if (getProject().getReference(pathid) != null && target == null) {
            return;
        }
        
        mainPath = new Path(getProject());
        mainFileSet = new FileSet();
        mainFileSet.setDir(new File(workspaceBaseDir.replace('/', File.separatorChar)));
        mainFileSet.setProject(getProject());
        String includes = "";
        
        FileSet artifactFileSet = new FileSet();
        artifactFileSet.setDir(new File(workspaceBaseDir.replace('/', File.separatorChar)));
        artifactFileSet.setProject(getProject());
        String artifactIncludes = "";
        
        getProject().addReference(pathid, mainPath);
        
        // Gather list of projects, removing redundancy
        List projects = new ArrayList();
        List testProjects = new ArrayList();
        followProjectDependencies(getProject().getBaseDir(), projects);
        
        List allProjects = projects;
        
        // Find out whether to run targets on dependencies
        // We only want the root level invocation to run targets
        boolean executeTargets = !("true".equalsIgnoreCase(getProject().getProperty("no.dep")));
        executeTargets = build && executeTargets && !nofollow;
        
        // Describe complete dependency set
        if (executeTargets) {
            describeDependencies(projects, "Dependency build order:");
        }
        
        // Deal with this projects libs
        {
            // Add lib/main/*.jar
            FileSet fileset = new FileSet();
            fileset.setDir(getProject().getBaseDir());
            fileset.setIncludes("lib/*.jar");
            fileset.setProject(getProject());
            mainPath.addFileset(fileset);
            
            String thisProj = calcThisProjectName();
            System.out.println(thisProj);
            includes += thisProj + "/lib/*.jar ";
        }
        
        for (int i = allProjects.size() - 1; i >= 0; i--) {
            String depName = (String) allProjects.get(i);
            File projDir = getProjectBaseDir(depName);
            
            String theTarget = target;
            if (theTarget == null) {
                theTarget = "jar";
            }
            
            if (executeTargets /*&& (mainDep || test)*/) {
                System.out.println("Executing " + theTarget + " on " + depName + "...");
                
                // Run target if specified
                Ant ant = new Ant();
                ant.setDir(projDir);
                ant.setInheritAll(false);
                ant.setTarget(theTarget);
                ant.setProject(getProject());
                
                // Tell sub-invocation not to execute targets on dependencies
                Property prop = ant.createProperty();
                prop.setName("no.dep");
                prop.setValue("true");
                prop.setProject(getProject());
                prop.execute();
                
                ant.execute();
            }
            
            // Add lib/main/*.jar
            FileSet fileset = new FileSet();
            fileset.setDir(projDir);
            fileset.setIncludes("dist/*.jar,dist/*.war");
            fileset.setProject(getProject());
            mainPath.addFileset(fileset);
            
            includes += depName + "/dist/*.jar " + depName + "/dist/*.war ";
            artifactIncludes += depName + "/dist/*.jar " + depName + "/dist/*.war ";
            
            // Add lib/main/*.jar
            fileset = new FileSet();
            fileset.setDir(projDir);
            fileset.setIncludes("lib/*.jar");
            fileset.setProject(getProject());
            mainPath.addFileset(fileset);
            
            includes += depName + "/lib/*.jar ";
        }
        
        if (includes.length() > 0) {
            mainFileSet.setIncludes(includes);
            getProject().addReference(pathid + ".fileset", mainFileSet);
        }
        
        if (artifactIncludes.length() > 0) {
            artifactFileSet.setIncludes(artifactIncludes);
        } else {
            artifactFileSet.setIncludes("nothing");
        }
        getProject().addReference(pathid + ".artifact.fileset", artifactFileSet);
    }
    
    public String calcThisProjectName() throws BuildException {
        try {
            File dir = getProject().getBaseDir().getCanonicalFile();
            File wspace = new File(workspaceBaseDir.replace('/', File.separatorChar)).getCanonicalFile();
            String projName = "";
            while (!dir.equals(wspace)) {
                if (projName.length() > 0) {
                    projName = "/" + projName;
                }
                projName = dir.getName() + projName;
                dir = dir.getParentFile();
            }
            return projName;
        } catch (IOException err) {
            throw new BuildException(err);
        }
    }

    /**
     * @param projects
     */
    private void describeDependencies(List projects, String label) {
        System.out.println("---- " + label
                + " ---------------------------------------------".substring(label.length()));
        for (int i = projects.size() - 1; i >= 0; i--) {
            System.out.println(" " + projects.get(i));
        }
        if (projects.size() == 0) {
            System.out.println(" None.");
        }
        System.out.println("---------------------------------------------------");
    }
    
    /**
     * Load dependencies for a project and iterate over them.
     * 
     * @param projDir directory containing project
     * @param projects accumulation of project names found
     */
    protected void followProjectDependencies(File projDir, List projects) {
        //System.out.println("following " + projDir.getAbsolutePath());
        // Load project properties
        Properties properties = loadProjectProperties(projDir);
        String deps = properties.getProperty(type + ".dependencies");
        
        if (deps != null && deps.trim().length() > 0) {
            // Visit dependencies
            iterateOverDependencies(deps, projects);
        }
    }

    /**
     * Step over each dependency mentioned in depsString and record it. Also follow
     * each project once.
     * 
     * @param depsString comma seperated list of project dependencies
     * @param projects accumulation of project names found
     */
    protected void iterateOverDependencies(String depsString, List projects) {
        String deps[] = depsString.split(",");
        for (int i=0 ; i<deps.length ; i++) {
            String dep = deps[i].trim();
            if (dep.length() > 0) {
                if (projects.contains(dep)) {
                    // remove from current position and add to end
                    //System.out.println("Removed earlier dependency on " + dep);
                    projects.remove(dep);
                    projects.add(dep);
                } else {
                    //System.out.println("Adding " + dep);
                    projects.add(dep);
                }
                
                if (!nofollow && type.equals("compile")) {
                    followProjectDependencies(getProjectBaseDir(dep), projects);
                }
            }
        }
    }
    
    /**
     * Load project properties for given project.
     * 
     * @param projDir project directory
     * @return Properties object containing project properties
     */
    protected Properties loadProjectProperties(File projDir) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(projDir, "project.properties")));
        } catch (IOException e) {
            throw new BuildException("Failed to load project properties from "
                    + projDir.getAbsolutePath(), e);
        }
        return properties;
    }
    
    /**
     * Given a project name, returning the corresponding project directory.
     * 
     * @param projName the project name
     * @return the project directory
     * @throws BuildException if the project directory cannot be located
     */
    protected File getProjectBaseDir(String projName) throws BuildException {
        String absPath = workspaceBaseDir + "/" + projName;
        File projDir = new File(absPath.replace('/', File.separatorChar));
        if (!projDir.exists()) {
            throw new BuildException("Expected project " + projName + " to be located at "
                    + projDir.getAbsolutePath() + " but location doesn't exist");
        }
        return projDir;
    }
}
