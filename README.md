# Quantifying the Potential to Automate the Synchronization of Variants in Clone-and-Own
This is the replication package of the paper _"Quantifying the Potential to Automate the Synchronization of Variants in Clone-and-Own"_ accepted at
the 38th IEEE International Conference on Software Maintenance and Evolution (ICSME 2022).
It contains the Java code of our simulation, the dataset extracted from BusyBox, the code used to analyze the results, and the files containing
the results that we report in the paper.


## Obtaining the Artifacts
Clone the repository to a location of your choice using [git](https://git-scm.com/):
  ```
  git clone https://github.com/VariantSync/SynchronizationStudy.git
  ```

## Project Structure
This is brief overview on the most relevant directories and files:
* [`docker`](docker) contains the script and property files used by the Docker containers.
    * `docker-resources/config-simulation.properties` configures the simulation as presented in our paper.
    * `docker-resources/config-validation.properties` configures a quick simulation for validating the correctness of the Docker setup.
* [`docs`](docs) contains the Javadocs of our source code. You can open the Javadocs in your [browser][documentation].
* [`local-maven-repo`](local-maven-repo) contains additional libraries that are considered by Maven.
* [`plots`](plots) contains pythons scripts for plotting the figures shown in our paper. 
* [`simulation-files`](simulation-files) is the working directory of the study. This directory initially contains the dataset with the domain knowledge that we extracted from BusyBox. The files comprise lists
  of commits that state whether a commit could be processed, and the domain knowledge for over 8,000 commits. For about 5,000 commits, the complete domain knowledge could be extracted. All results can be found here
* [`src`](src/main/java/org/variantsync/studies/evolution/simulation) contains the source files used to run the simulation.
* `reported-results-part*.zip` are archives with the raw result data reported in our paper. To evaluate them they have to be
  unpacked and copied into a single file.

DiffDetective is a java library and command-line tool to parse and classify edits to variability in git histories of preprocessor-based software product lines by creating [variation tree diffs][difftree_class] and operating on them.

We offer a [Docker](https://www.docker.com/) setup to easily __replicate__ the validation performed in our paper.
In the following, we provide a quickstart guide for running the replication.
You can find detailed information on how to install Docker and build the container in the [INSTALL](INSTALL.md) file, including detailed descriptions of each step and troubleshooting advice.

## Study Replication
### 0. Preparation
- Start the docker deamon.
- Clone this repository to a directory of your choice.
- Open a terminal and navigate to the root directory of this repository.

### 1. Build the Docker container
To build the Docker container you should run the `build` script corresponding to your operating system.
#### Windows:
`.\build.bat`
#### Linux/Mac (bash):
`./build.sh`

### 2. Validate the successful installation
> The replication will require 10-30 days depending on your hardware.
> Therefore, we offer a short validation (5-10 minutes) which runs a small subset of the study.
> You can run it by providing "validation" as argument instead of "replication" (i.e., `.\execute.bat validation`,  `./execute.sh validation`).
> If you want to stop the execution, you can call the provided script for stopping the container in a separate terminal.
> When restarted, the execution will continue processing by restarting at the last unfinished repository.
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

To execute the validation you can run the `execute` script corresponding to your operating system with `validation` as first argument.

#### Windows:
`.\execute.bat validation`
#### Linux/Mac (bash):
`./execute.sh validation`

The validation's results are printed to the terminal and the generated plots are stored in the [simulation-files/plots](simulation-files/plots) directory.

### 3. Start the replication
**ATTENTION**

> Before running or re-running the replication:
> Make sure to delete all previously collected results by deleting the files in the "./simulation-files" directory, as they will otherwise be 
> counted as results of parallel experiment executions. We only append results data, not overwrite it, to make it 
> possible to run multiple instances of the study in parallel.

To execute the replication you can run the `execute` script corresponding to your operating system with `replication` as first argument.

#### Windows:
`.\execute.bat replication`
#### Linux/Mac (bash):
`./execute.sh replication`


### 4. View the results in the [simulation-files](simulation-files) directory
All raw results are stored in a [results](simulation-files/results.txt) file.
The aggregated results are printed to the terminal at the end of the execution.

Moreover, the results comprise the [plots](simulation-files/plots) that are part of our paper.

### Documentation

This replication package is documented with javadoc. The documentation can be accessed on this [website][documentation]. 

### Docker Experiment Configuration
By default, the properties used by Docker are configured to run the experiments as presented in our paper. We offer the
possibility to change the default configuration.
* Open the properties file [`config-replication.properties`](docker/config-replication.properties).
* Change the properties to your liking.
* Rebuild the docker image as described above.
* Delete old results in the `./simulation-files` folder.
* Start the simulation as described above.

### Clean-Up
The more experiments you run, the more space will be required by Docker. The easiest way to clean up all Docker images and
containers afterwards is to run the following command in your terminal. **Note that this will remove all other containers and images
not related to the simulation as well**:
```shell
docker system prune -a
```
Please refer to the official documentation on how to remove specific [images](https://docs.docker.com/engine/reference/commandline/image_rm/) and [containers](https://docs.docker.com/engine/reference/commandline/container_rm/) from your system.

[documentation]: https://variantsync.github.io/SynchronizationStudy/docs/
[website]: https://variantsync.github.io/SynchronizationStudy/