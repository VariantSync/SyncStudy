#! /bin/bash
if [ "$1" == '' ]
then
  echo "Either fully replicate the study as presented in the paper (replication), or a do quick installation validation (validation)."
  echo "-- Bash Examples --"
  echo "Replicate study: './execute.sh replication'"
  echo "Validate the installation: './execute.sh validation'"
  exit
fi

cd /home/user || exit

BB=/home/user/simulation-files/busybox
if test -d "$BB"; then
    echo "Found BusyBox sources."
else
    echo "BusyBox sources not cloned yet. Cloning repository"
    cd simulation-files || exit
    git clone git://busybox.net/busybox.git
    cd ..
fi

cp target/*Runner*-jar-with* .
cp target/ResultEval-jar-with-dependencies* .


if [ "$1" == 'replication' ]
then
    echo "Running full study replication. This might take up to a month depending on your system."
    echo ""
    echo ""
    echo ""
    java -jar StudyRunner-jar-with-dependencies.jar config-simulation.properties
elif [ "$1" == 'validation' ]
then
    echo "Running a (hopefully) short validation of the installation."
    echo ""
    echo ""
    echo ""
    java -jar StudyRunner-jar-with-dependencies.jar config-validation.properties
else
  echo "Either fully replicate the study as presented in the paper (replication), or a do quick installation validation (validation)."
  echo "-- Bash Examples --"
  echo "Replicate study: './execute.sh replication'"
  echo "Validate the installation: './execute.sh validation'"
  exit
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
