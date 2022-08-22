# Quantifying the Potential to Automate the Synchronization of Variants in Clone-and-Own
This is the replication package of the paper _"Quantifying the Potential to Automate the Synchronization of Variants in Clone-and-Own"_ accepted at
the 38th IEEE International Conference on Software Maintenance and Evolution (ICSME 2022).
It contains the Java code of our simulation, the dataset extracted from BusyBox, the code used to analyze the results, and the files containing
the results that we report in the paper.

__This is a quickstart guide. A more detailed replication package will be made available upon the artifact submission at the ICSME Artifact Track.__

## Obtaining the Artifacts
Clone the repository to a location of your choice using [git](https://git-scm.com/):
  ```
  git clone https://github.com/SynchronizationStudy/SynchronizationStudy.git
  ```

## Project Structure
This is brief overview on the most relevant directories and files:
* [`docker`](docker) contains the script and property files used by the Docker containers.
    * `docker-resources/config-simulation.properties` configures the simulation as presented in our paper.
    * `docker-resources/config-validation.properties` configures a quick simulation for validating the correctness of the Docker setup.
* [`variability-busybox`](simulation-files/variability-busybox) contains the domain knowledge that we extracted from BusyBox. The files comprise lists
  of commits that state whether a commit could be processed, and the domain knowledge for over 8,000 commits. For about 5,000 commits, the complete domain knowledge could be extracted.
* [`src`](src/main/java/anonymous/simulation) contains the source files used to run the simulation.
* `reported-results-part*.zip` are archives with the raw result data reported in our paper. To evaluate them they have to be
  unpacked and copied into a single file.
* [`LICENSE.md`](LICENSE.md) contains the open source license (Apache License 2.0).

## Requirements and Installation

### Setup Instructions
* Install [Docker](https://docs.docker.com/get-docker/) on your system and start the [Docker Daemon](https://docs.docker.com/config/daemon/).
* Open a terminal and navigate to the project's root directory (i.e., `SynchronizationStudy`).
* Build the docker image by calling docker in your terminal:
  ```shell
  docker build -t simulation .  
  ```

* Before replicating our study, you may want to validate the correctness of the Docker setup. To do so, call the validation corresponding to your OS. The validation should take a few minutes depending on your system.
  ```shell
  # Windows:
  .\simulation.bat validate
  # Linux | MacOS
  docker run --rm -v "simulation-files":"/home/user/simulation-files" simulation validate
  ```
  The script will generate figures similar to the ones presented in our paper. They are automatically saved to
  `./simulation-files`.

## Running the Experiments Using Docker
**ATTENTION**
```text
! Before running or re-running the simulation:
! Make sure to delete all previously collected results by deleting the files in the "./simulation-files" directory, as they will otherwise be 
! counted as results of parallel experiment executions. We only append results data, not overwrite it, to make it 
! possible to run multiple instances of the same experiment in parallel.
```

All of the commands in this section are assumed to be executed in a terminal with working directory at project root.
You can **stop the execution** of any experiment by running the following command in another terminal (depending on your OS):
```shell
# Windows:
.\stop-simulation.bat
# Linux | MacOS
docker stop $(docker ps -a -q --filter "ancestor=simulation")
# or with sudo
sudo docker stop $(sudo docker ps -a -q --filter "ancestor=simulation")
```
Stopping the simulation may take a moment.

### Running the Complete Simulation
You can replicate the simulation exactly as presented in our paper. The following command will execute 30 repetitions of the simulation
```shell
docker run --rm -v "$(pwd)/simulation-files":"/home/user/simulation-files" simulation validate

```

_Expected runtime for the simulation: 10-30 days depending on the used hardware._


### Result Evaluation
You can run the result evaluation by calling the simulation with `evaluate`.
The script will generate figures similar to the ones presented in our paper.
```shell
docker run --rm -v "simulation-files":"/home/user/simulation-files" simulation evaluate
```
_Expected Average Runtime: several seconds to a few minutes_


### Docker Experiment Configuration
By default, the properties used by Docker are configured to run the experiments as presented in our paper. We offer the
possibility to change the default configuration.
* Open the properties file [`config-simulation.properties`](docker/config-simulation.properties).
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
