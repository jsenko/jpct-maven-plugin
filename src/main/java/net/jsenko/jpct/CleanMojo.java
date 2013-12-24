/*
    Copyright 2013 Red Hat

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package net.jsenko.jpct;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URL;

/**
 * 
 * @author Jakub Senko
 */
@Mojo(name = "clean", aggregator = true)
public class CleanMojo extends AbstractMojo
{

    @Parameter(property = "basedir", required = true)
    private File   basedir;

    /**
     * Place to store plugin data (e.g. generated config).
     */
    @Parameter(property = "dataDir", defaultValue = "${project.build.directory}/jmp", required = true)
    private File dataDir;

    /**
     * If set, delete all Jenkins jobs that have been created by this plugin.
     */
    @Parameter(property = "jenkinsUrl", required = false)
    private URL jenkinsUrl;
    
    
    @Parameter(property = "jenkinsUser", required = false)
    private String jenkinsUser;

    @Parameter(property = "jenkinsToken", required = false)
    private String jenkinsToken;

    private Log log;
    
    public void execute() throws MojoExecutionException
    {
        log = getLog();
        try
        {
            // if jenkinsUrl is set {
            // get config object
            // get stored job names
            // check they exist in jenkins else log warning
            // to be sure ask if there is more that 3 jobs
            // verify that they have been created by this plugin so we don't fuck up
            // delete them
            // update configuration (necessary? if deleting anyway, yes in case delete fails) }
            // delete data dir
            deleteRecursively(dataDir);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Delete single file or folder and all it's contents.
     */
    private void deleteRecursively(File file)
    {
        if(dataDir.isDirectory())
            for(File subFile: file.listFiles())
                deleteRecursively(subFile);
        file.delete();
    }
}
