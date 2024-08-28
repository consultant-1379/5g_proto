#!/bin/bash

#-Wignore added to python call as the Python builder's site-packages are not fulfilling the "request" library dependencies

export XDG_DATA_HOME=/home/helmuser/.helm
export XDG_CONFIG_HOME=/home/helmuser/.config
export XDG_CACHE_HOME=/home/helmuser/.cache

if [ "$1" == "-r" ]; 
then 

      printf "helm repo add %s %s --username %s --password %s \n" $(ruby -ryaml -e "x=YAML.load(ARGF); d=Hash.new; x[\"dependencies\"].each { |c| d[c[\"name\"]] = c[\"repository\"]}; d.each { |k, v|; puts \"#{k} #{v}\"; system(\"python -Wignore ./scripts/helm_repositories.py -u --repository #{v}\"); system(\"python -Wignore ./scripts/helm_repositories.py -p --repository #{v}\"); }; " <.bob/eric-sc-umbrella/requirements.yaml) | bash ;
else
 
      printf "helm repo add %s %s --username %s --password %s \n" $(ruby -ryaml -e "x=YAML.load(ARGF); d=Hash.new; x[\"dependencies\"].each { |c| d[c[\"name\"]] = c[\"repository\"]}; d.each { |k, v|; puts \"#{k} #{v}\"; system(\"python -Wignore ./scripts/helm_repositories.py -u --repository #{v}\"); system(\"python -Wignore ./scripts/helm_repositories.py -p --repository #{v}\"); }; " <esc/helm/eric-sc-umbrella/requirements.yaml) | bash;

fi;
