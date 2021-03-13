#!/bin/bash
# ##################################################################
# SYNOPSYS
#   Generic executable JAR runner.
#   The script look for the JAR in the './target' directory
#   Default configuration files location is './resources'
#
#   Arguments:
#   <project>   project folder name      
#   --node      arbitrary value to specify a node like an environment 
#               (defaul node is 'local')
#   --profiles  comma separated spring profiles to use   
#
# ##################################################################

typeset EXEC_DIR="./run"

# Functions
function log { echo "[Starter] $*"; }
function warn { echo "[Starter] WARN : $*"; }
function die { echo "[Starter] ERROR: $*"; exit 1; }
function usage { echo "[Starter] USAGE: $(basename $0) <project> [--node=<nodename>:local] [--profiles=<prof1,...>]"; exit 1; }

# ##### MAIN #####
[ $# -lt 1 ] && usage

typeset _project="$1"
typeset _node="local"

typeset _base_name=$(dirname $0)
cd ${_base_name} || die "no such project directory: ${_base_name}"

# Goto project folder 
cd ${_project} || die "no such project directory: ${_project}"

log "current directory: ${PWD}"

# Look for last version of the JAR
typeset _name=$(find ./target/*.jar ! -name "*sources*" | head -1)
[ -z ${_name} ] && die "no such jar in ./target"

typeset _node _profiles

# Decode arguments
for _arg in $* ; do
  case ${_arg} in
  --node=*) 
    _node=$(echo ${_arg} | awk -F= '{print $2}') 
    ;;
    
  --profiles=*)
    _profiles=$(echo ${_arg} | awk -F= '{print $2}')
    ;;
  esac
done

[ -z ${_node} ] && { echo "ERROR: no such argument: --node"; usage; }
[ ! -z ${_profiles} ] && _profiles="--spring.profiles.active=${_profiles}"

typeset _jvm_opts="-Xms128m -Xmx512m"

typeset _cmd="java ${_jvm_opts} -jar ${_name} --node=${_node} ${_profiles} --spring.config.location=file:./,optional:./resources/"

echo "command: ${_cmd}"
${_cmd} 

exit 0
