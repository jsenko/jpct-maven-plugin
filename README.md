## jpct-maven-plugin
Jenkins pre-commit test maven plugin

Test your local code on Jenkins before pushing it to a remote git repository. The work is offloaded to the Jenkins server so the build is not using resources of your machine. You can start tests using this maven plugin and continue working. After the results are available, the plugin will present them to you and if the build is successful, you can confidently push the tested code to the remote repository. The plugin performs all the necessary work, including job configuration and transfer of the local changes. This works by transfering only the difference between the local branch and the remote branch, so no large amount of data is transfered and the tool is very efficient.

#### Prerequisites:
 - Jenkins server with sufficient access rights (create and run jobs)
 - Your project must use Maven
 - The code is stored in a git repository. The local repository was cloned from some remote, and Jenkins must have access
   to pull from this remote repository.

#### Workflow:
 - Make some code changes, preferably in a dedicated feature branch (however any branch can be used).
 - Commit them locally or just add the to the staging area (git add).
 - Run the plugin with the required parameters (both CLI and more detailed POM configuration is possible).
 - Wait for the results.
 - Continue work until all tests pass.
 - safely delete the created jenkins job(s) using `clean` goal.

#### Installation
Just check out the code and run `mvn install`

#### Basic example
To run `mvn test -Dfoo` on Jenkins, execute:
`mvn net.jsenko.jpct:jpct-maven-plugin:1.0-SNAPSHOT:run -DjenkinsUrl='http://localhost:8080' -Dgoals='test -Dfoo'`
Notes:
 - The url and goals are automatically saved so you just need to provide them once per job
 - Many properties have default values, for example it is expected that you are currently on the branch that conains the     changes (`-DtopicBranch=HEAD`), the remote name is `origin` and the job name will be same as the branch name.

#### Configuration Overview
Although the plugin has a `help` goal, here we provide short table with all properties for `run` goal:

Property | Description | Default value
--- | --- | ---
jenkinsUrl | Url of the Jenkins instance on which the jobs will be executed. This property is saved per job name so it does not have to be specified again. | null
jenkinsUser, jenkinsToken | In case the Jenkins instance requires authentication | null
jobName | This is an unique job identifier. Name of the jenkins job. | current branch name
goals | Maven goals and options to be executed. | `test`
topicBranch | Reference to the branch to be tested. | `HEAD` (current branch)
gitRemoteName | Name of the git remote which will be cloned on Jenkins. It must contain a branch with a common ancestor with topic branch. This base commit will the be checked out, and a patch with the changes will be applied on it, effectively duplicating changes made locally. | `origin`
includeStaged | Also include uncommitted changes staged for commit in addition to local commits to the changes that will be tested | false
description | Provide jenkins job description. | empty string
buildCheckInterval | Interval between checking the build status, in milliseconds, because the plugin polls the jenkins whether the build finished. Therefore it is more efficient to be larger for longer builds. | `4000`
dataDir | Place to store generated job configurations, test reports and other settings. | `${project.build.directory}/jpct`
forceJobReuse | The jenkins jobs are reused if possible. This option forces the plugin to use a Jenkins job with the specified name even if we cannot verify that the job has been created by this plugin. | false
jobs | The jobs can be also defined in `pom.xml` using this property. If the job name fits with one of these, they are combined with the properties from command line (POM properties overwrite CLI). | null

#### Configuration in POM.
Job configuration is stored in several classes in `net.jsenko.jpct.configurator.model` package. These classes are then converted to an xml file that is used by Jenkins. Fields of these model classes can be set via pom.xml providing detailed job configuration options (instead of just `-Dgoals` property). Here is a simple example, that adds a build step that prints "hello world!" using bash command:

```xml
<plugin>
<groupId>net.jsenko.jpct</groupId>
<artifactId>jpct-maven-plugin</artifactId>
<version>1.0-SNAPSHOT</version>
<configuration>
    <jobs>
        <job>
            <name>custom-job</name>
            <description>Custom job description.</description>
            <before>
                <beforeStep>
                    <shell>echo "hello world!"</shell>
                </beforeStep>
            </before>
        </job>
    </jobs>
</configuration>
</plugin>
```
Execute following command:
`mvn net.jsenko.jpct:jpct-maven-plugin:1.0-SNAPSHOT:run -DjenkinsUrl='http://localhost:8080' -DjobName=custom-job`
If we look at the jenkins console output, the shell command has been executed.
Note that the model may change in futue and additional configuration options will be added.

#### Conclusion
Thank you for your interest in this tool. It is in active development, so it is very possible that it contains some bugs. If you find any, make new issue, or preferably, a pull request:)
