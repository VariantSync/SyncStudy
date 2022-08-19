# Installation
## Installation Instructions
In the following, we describe how to build the Docker image and validate the successful installation.

### 1. Install Docker (if required)
How to install Docker depends on your operating system:

- _Windows or Mac_: You can find download and installation instructions [here](https://www.docker.com/get-started).
- _Linux Distributions_: How to install Docker on your system, depends on your distribution. The chances are high that Docker is part of your distributions package database.
Docker's [documentation](https://docs.docker.com/engine/install/) contains instructions for common distributions.

Then, start the [docker deamon](https://docs.docker.com/config/daemon/).

### 2. Open a Suitable Terminal
```
# Windows Command Prompt: 
 - Press 'Windows Key + R' on your keyboard
 - Type in 'cmd' 
 - Click 'OK' or press 'Enter' on your keyboard
 
# Windows PowerShell:
 - Open the search bar (Default: 'Windows Key') and search for 'PowerShell'
 - Start the PowerShell
 
# Linux:
 - Open the search bar (Default: 'Meta Key' (aka. 'Windows Key')) and search for 'terminal' or 'konsole'
```

Clone this repository to a directory of your choice using git:
```shell
git clone https://github.com/VariantSync/SynchronizationStudy.git
```
Then, navigate to the root of your local clone of this repository:
```shell
cd SynchronizationStudy
```

### 3. Build the Docker Container
To build the Docker container you can run the `build` script corresponding to your operating system:
```
# Windows: 
  .\build.bat
# Linux/Mac (bash): 
  ./build.sh
```

## 4. Validation & Replication

### Running the Replication or Verification
To execute the replication you can run the `execute` script corresponding to your operating system with `replication` as first argument.

#### Windows:
`.\execute.bat replication`
#### Linux/Mac (bash):
`./execute.sh replication`

While the study is running, the progress is constantly printed to the terminal, in which the container is running. The output should look similar the following:

```
[11:50:06.059073591] [STATUS] [main] [ExperimentBusyBox] Cleaning state of V0 repo.
[11:50:06.858602507] [STATUS] [main] [ExperimentBusyBox] Cleaning state of V1 repo.
[11:50:07.560662594] [STATUS] [main] [Experiment] Checkout of commits in SPL repo.
[11:50:08.054895965] [STATUS] [main] [ExperimentBusyBox] Normalizing BusyBox files...
[11:50:08.47664474] [STATUS] [main] [Experiment] Starting repetition 1 of 1 with 4 variants.
[11:50:08.476797596] [STATUS] [main] [Experiment] Sampling next set of variants...
[11:50:08.47694] [STATUS] [main] [ExperimentBusyBox] Loading feature models.
[11:50:08.497220194] [STATUS] [main] [ExperimentBusyBox] Creating model union.
[11:50:08.511823167] [STATUS] [main] [Experiment] Done. Sampled 0 variants.
[11:50:08.512066233] [STATUS] [main] [Experiment] Generating variants...
[11:50:08.512230861] [STATUS] [main] [Experiment] Done.

```

> WARNING!
> The replication will require several days depending on your system.
> Therefore, we offer a short validation (5-10 minutes) which runs a small portion of the study.
> You can run it by providing "validation" as argument instead of "replication" (i.e., `.\execute.bat validation`,  `./execute.sh validation`).
> If you want to stop the execution, you can call the provided script for stopping the container in a separate terminal.
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

The results of the validation will be stored in the [simulation-files](simulation-files) directory.

### Expected Output of the Validation
The results are saved in a simple [results](simulation-files/results.txt) file in the [simulation-files](simulation-files) directory. For each run, a JSON object, containing the required evaluation data, is written to the results file. 

A summary of the results is printed to the console at the end of the validation or replication. This summary should look similar to the one shown below. 
```
[11:51:11.061639214] [STATUS] [main] [Experiment] Done.
[11:51:11.061677737] [STATUS] [main] [Experiment] Finished commit pair 6126 of 6126.

[11:51:11.06171744] [STATUS] [main] [Experiment] All done.
Running result evaluation
Read a total of 681 results.
++++++++++++++++++++++++++++++++++++++
Patch Success
++++++++++++++++++++++++++++++++++++++
525 of 681 commit-sized patch applications succeeded (77.1%)
679 of 867 file-sized patch applications succeeded (78.3%)
23234 of 27267 line-sized patch applications succeeded (85.2%)
22646 of 23499 line-sized patch applications succeeded after filtering (96.4%)

++++++++++++++++++++++++++++++++++++++
Precision / Recall
++++++++++++++++++++++++++++++++++++++
Without Domain Knowledge
TP: 22553
FP: 637
TN: 3362
FN: 715
Precision: 0.97
Recall: 0.97
F-Measure: 0.97

++++++++++++++++++++++++++++++++++++++
With Domain Knowledge

TP: 22482
FP: 126
TN: 3873
FN: 786
Precision: 0.99
Recall: 0.97
F-Measure: 0.98
++++++++++++++++++++++++++++++++++++++
Accuracy
++++++++++++++++++++++++++++++++++++++
Normal patching achieved the expected result 25915 out of 27267 times
Accuracy: 95.0%
Balanced Accuracy: 0.90

Filtered patching achieved the expected result 26355 out of 27267 times
Accuracy: 96.7%
Balanced Accuracy: 0.97

++++++++++++++++++++++++++++++++++++++
Plotting figures

Loading chache /home/user/simulation-files/results.txt.cache

Parsed Values:
commitPatches = 681
normal = {'name': 'normal', 'tp': 22553, 'fp': 637, 'tn': 3362, 'fn': 715, 'wrongLocation': 44, 'commitPatches': 681, 'commitSuccess': 525, 'file': 867, 'fileSuccess': 679, 'line': 27267, 'lineSuccess': 23234}
filtered = {'name': 'filtered', 'tp': 22482, 'fp': 126, 'tn': 3873, 'fn': 786, 'wrongLocation': 38, 'commitPatches': 681, 'commitSuccess': 611, 'file': 757, 'fileSuccess': 687, 'line': 23499, 'lineSuccess': 22646}

RQ1
RQ2
RQ3
Done
Cleaning temporary directories
Done.
```

## Troubleshooting

### 'Got permission denied while trying to connect to the Docker daemon socket'
`Problem:` This is a common problem under Linux, if the user trying to execute Docker commands does not have the permissions to do so. 

`Fix:` You can fix this problem by either following the [post-installation instructions](https://docs.docker.com/engine/install/linux-postinstall/), or by executing the scripts in the replication package with elevated permissions (i.e., `sudo`).

### 'Unable to find image 'replication-package:latest' locally'
`Problem:` The Docker container could not be found. This either means that the name of the container that was built does not fit the name of the container that is being executed (this only happens if you changed the provided scripts), or that the Docker container was not built yet. 

`Fix:` Follow the instructions described above in the section `Build the Docker Container`.

### No results after validation, or 'cannot create directory...: Permission denied'
`Problem:` This problem can occur due to how permissions are managed inside the Docker container. More specifically, it will appear, if Docker is executed with elevated permissions (i.e., `sudo`) and if there is no [simulation-files](simulation-files) directory because it was deleted manually. In this case, Docker will create the directory with elevated permissions, and the Docker user has no permissions to access the directory.

`Fix:` If there is a _simulation-files_ directory, delete it with elevated permission (e.g., `sudo rm -r results`). 
Then, execute `git restore simulation-files` to restore the deleted directory.
