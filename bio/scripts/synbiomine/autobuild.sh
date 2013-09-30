#!/bin/bash
#
# default usage: IM_REPO/synbiomine$ ../bio/scripts/synbiomine/autobuild.sh
#
# This script will assist biologists to build synbiomine.

set -e # Automatic exit from bash shell script on error 
# Ref http://stackoverflow.com/questions/2870992/automatic-exit-from-bash-shell-script-on-error

DATE=`date +%d-%m-%Y`
LOG=synbiomine-build.$DATE.log

# SAN_SYNBIOMINE_DATA=/SAN_synbiomine/data
SAN_SYNBIOMINE_DUMPS=/SAN_synbiomine/dumps
SAN_SYNBIOMINE_LOGS=/SAN_synbiomine/logs/synbiomine

echo ""
echo "Autobuild script will help you to build synbiomine in an interactive way."
echo "Usage:" 
echo "IM_REPO/synbiomine$ ../bio/scripts/synbiomine/autobuild.sh"
echo ""
echo "Prerequisites:"
echo "* Run project build on theleviathan with correct configurations for Postgres and MySQL databases"
echo "* Email client (sudo apt-get install mailutils) is installed and properly configured"
echo "* Check parsers are up-to-date with model changes and data format changes"
echo "* synbiomine.properties.build.theleviathan and synbiomine.properties.webapp.theleviathanin are in ~/.intermine directory"
echo "* Keep your git repository up-to-date"
echo ""
echo "Note:"
echo "* Run this script from synbiomine directory"
echo "* Database dumps are in SAN dumps directory"
echo "* Logs are in SAN log directory"
echo ""

#----------------------------- Functions -----------------------------

update_datasets() {
    # Ref http://intermine.readthedocs.org/en/latest/database/download-scripts/?highlight=data%20download
    if [ -z "$1" ]
        then
            echo "Update all the datasets in synbiomine.yml (Ref https://github.com/intermine/intermine/blob/dev/bio/scripts/DataDownloader/config/synbiomine.yml)"
            echo ""
            perl ../bio/scripts/DataDownloader/bin/download_data -e synbiomine
    else
        echo "Update datasets: $@"
        echo ""
        perl ../bio/scripts/DataDownloader/bin/download_data -e synbiomine $@        
    fi
}

run_project_build() {
    echo "Build synbiomine database..."
    echo ""
    ../bio/scripts/project_build -l -v -Vbuild.theleviathan localhost $SAN_SYNBIOMINE_DUMPS/synbiomine/theleviathan/synbiomine-build-$DATE.final
    
    echo "Copy database synbiomine-build to synbiomine-$DATE"
    echo ""
    createdb -O fh293 -T synbiomine-build synbiomine-$DATE

    echo "Update database name in synbiomine.properties.webapp.theleviathan"
    echo ""
    sed -i 's/^db.production.datasource.databaseName=.*/db.production.datasource.databaseName=synbiomine-$DATE/' ~/.intermine/synbiomine.properties.webapp.theleviathan
}

run_sources () {
    # Ref http://intermine.readthedocs.org/en/latest/database/database-building/build-script/#running-a-single-datasource
    if [ -z "$1" ]
        then
            echo "No sources supplied"
            echo ""
    else
        echo "Run sources: $1"
        echo ""
        (cd dbmodel/; ant clean build-db -Drelease=build.theleviathan) && (cd integrate; ant clean-all; ant -v -Dsource=$1 -Drelease=build.theleviathan)       
    fi  
}

run_a_postprocess () {
    # Ref http://intermine.readthedocs.org/en/latest/database/database-building/post-processing/?highlight=postprocess
    if [ -z "$1" ]
        then
            echo "No postprocess name supplied"
            echo ""
    else
        echo "Run postprocess: $1"
        echo ""
        (cd postprocess; ant -v -Daction=$1 -Drelease=webapp.theleviathan)       
    fi   
}

run_template_comparison () {
    # Ref http://intermine.readthedocs.org/en/latest/database/data-integrity-checks/template-comparison/
    # sudo easy_install intermine

    if [ -z "$1" ]
        then
            echo "Run template comparison..."
            echo ""
            (cd ../intermine/scripts; python compare_templates_for_releases.py http://www.flymine.org/synbiomine mike@intermine.org)
    else
        echo "Run template comparison with parameters: $1"
        echo ""
        (cd ../intermine/scripts; python compare_templates_for_releases.py $1)     
    fi  
}

run_acceptance_tests () {
    # Ref http://intermine.readthedocs.org/en/latest/database/data-integrity-checks/acceptance-tests/?highlight=acceptance%20test
    echo "Run acceptance tests..."
    echo ""
    (cd integrate; ant acceptance-tests -Drelease=build.theleviathan)
    echo "The results will be in integrate/build/acceptance_test.html"

}

run_template_comparison_and_acceptance_tests () {
    run_template_comparison && run_acceptance_tests
}

release_webapp () {
    echo "Release webapp..."
    (cd webapp/; ant clean-all; ant default remove-webapp release-webapp -Drelease=webapp.theleviathan)
}

send_email() {
    echo "Send notification..."
    echo "Please check the log at $SAN_SYNBIOMINE_LOGS/$LOG" | mail -s "synbiomine build $DATE finished" mike@intermine.org
}

run_all_in_one () {
    echo ""
    echo "Update datasets -> Run project build -> Run template comparison -> Run acceptance tests -> Release webapp"
    echo ""
    echo "Please check the log at $SAN_SYNBIOMINE_LOGS/$LOG"

    set -o pipefail # Ref http://stackoverflow.com/questions/6871859/piping-command-output-to-tee-but-also-save-exit-code-of-command
    # TODO Can't use tee to print stdout and stderr on screen
    update_datasets 2>&1 | ( while read line; do echo "[$(date)]: ${line}"; done ) >> $SAN_SYNBIOMINE_LOGS/$LOG
    run_project_build 2>&1 | ( while read line; do echo "[$(date)]: ${line}"; done ) >> $SAN_SYNBIOMINE_LOGS/$LOG
    run_template_comparison_and_acceptance_tests 2>&1 | ( while read line; do echo "[$(date)]: ${line}"; done ) >> $SAN_SYNBIOMINE_LOGS/$LOG
    release_webapp 2>&1 | ( while read line; do echo "[$(date)]: ${line}"; done ) >> $SAN_SYNBIOMINE_LOGS/$LOG
    send_email
}

#---------------------------------------------------------------

# Ref http://stackoverflow.com/questions/226703/how-do-i-prompt-for-input-in-a-linux-shell-script
while true; do
    echo "Would you like to:" 
    echo "[1] Update all datasets by download script"
    # echo "[2] Update any datasets"
    echo "[2] Run project build"
    # echo "[4] Run datasources"
    # echo "[5] Run a single postprocess"
    # echo "[6] Run template comparison"
    # echo "[7] Run acceptance tests"
    echo "[3] Run template comparison and acceptance tests"
    echo "[4] Release webapp"
    echo "[5] All-in-one"
    echo "[6] Exit"
    read -p "Please select one of the options: " num
    case $num in
        1  ) update_datasets; break;;
        # 2  ) echo "Please enter the dataset names separated by space:"; read DATASET_NAMES; update_datasets $DATASET_NAMES; break;;
        2  ) run_project_build; break;;
        # 4  ) echo "Please enter the sources separated by comma, e.g. omim,hpo:"; read SOURCE_NAMES; run_sources $SOURCE_NAMES; break;;
        # 5  ) echo "Please enter the postprocess:"; read POSTPROCESS; run_a_postprocess $POSTPROCESS; break;;
        # 6  ) echo "Please enter the service url (e.g. www.flymine.org/query [beta.flymine.org/beta] [email@to] [email@from]) or press enter to use default setting:"; read TC_PARA; run_template_comparison $TC_PARA; break;;
        # 7  ) run_acceptance_tests; break;;
        3  ) run_template_comparison_and_acceptance_tests; break;;
        4  ) release_webapp; break;;
        5 ) run_all_in_one; break;;
        6 ) echo "Bye"; exit;;
        * ) echo "Please select.";;
    esac
done