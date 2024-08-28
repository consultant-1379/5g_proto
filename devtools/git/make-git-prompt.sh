#!/bin/bash
#
# Little tool that installs GIT prompt showing current branch + indicates changes, etc...
#
#
set -e
sudo apt-get update || sudo yum install -y epel-release
sudo apt-get -y install git-review curl || sudo yum install -y git-review curl
root_dir=~/.git-own
mkdir -p $root_dir
pushd $root_dir
# git config --global user.email nikolas.hermanns@ericsson.com
# git config --global core.editor vim
git_bash="
source ~/.git-prompt.sh
export PS1='\[\033[01;32m\]\u@\h\[\033[01;34m\] \w\[\033[01;33m\]\$(__git_ps1)\[\033[01;34m\] \$\[\033[00m\] '
export GIT_PS1_SHOWDIRTYSTATE=1
"
if [[ `cat ~/.profile` != *"export PS1"* ]];then
	touch ~/.profile
    echo "$git_bash" >> ~/.profile
fi
curl -o ~/.git-prompt.sh https://raw.githubusercontent.com/git/git/master/contrib/completion/git-prompt.sh
curl -o ~/.git-completion.bash https://raw.githubusercontent.com/git/git/master/contrib/completion/git-completion.bash
. ~/.profile
popd


# Commented out from git_bash=... Known bug on CentOS.
# source ~/.git-completion.bash
#