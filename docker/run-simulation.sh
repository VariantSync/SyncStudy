#! /bin/bash
if [ "$1" == '' ]
then
  echo "Either fully replicate the simulation as presented in the paper (replication), or a do quick setup validation (validation)."
  echo "-- Bash Examples --"
  echo "Run simulation: './execute.sh replication'"
  echo "Validate the setup: './execute.sh validation'"
  exit
fi

echo "Starting $1"

patch --help || exit
cd /home/user || exit
ls -l
echo "Files in simulation-files"
ls -l simulation-files

BB=/home/user/simulation-files/busybox
if test -d "$BB"; then
    echo "Found BusyBox sources."
else
    echo "BusyBox sources not cloned yet. Cloning repository"
    cd simulation-files || exit
    git clone git://busybox.net/busybox.git
    cd ..
fi


echo "Copying jars"
  cp target/*Runner*-jar-with* .
  cp target/ResultEval-jar-with-dependencies* .
  echo ""

  echo "Files in WORKDIR"
  ls -l
  echo ""

if [ "$1" == 'replication' ]
then
    echo "Running full replication. This might take up to a month."
    echo ""
    echo ""
    echo ""
    java -jar ExperimentRunner-jar-with-dependencies.jar config-simulation.properties
elif [ "$1" == 'validation' ]
then
    echo "Running a (hopefully) short validation."
    echo ""
    echo ""
    echo ""
    java -jar ExperimentRunner-jar-with-dependencies.jar config-validation.properties
else
    echo "Either fully replicate the simulation as presented in the paper (replication), or a do quick setup validation (validation)."
    echo "-- Bash Examples --"
    echo "Run simulation: './execute.sh replication'"
    echo "Validate the setup: './execute.sh validation'"
fi

echo "Running result evaluation"
java -jar ResultEval-jar-with-dependencies.jar

echo "Plotting figures"
PD=/home/user/simulation-files/plots
if test -d "$PD"; then
    echo ""
else
    mkdir $PD
fi
cd plots || exit
python3 main.py /home/user/simulation-files/results.txt
cd ..

echo "Cleaning temporary directories"
rm -rf /home/user/simulation-files/workdir*
