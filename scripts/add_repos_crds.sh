#!/bin/bash

export XDG_DATA_HOME=/home/helmuser/.helm
export XDG_CONFIG_HOME=/home/helmuser/.config
export XDG_CACHE_HOME=/home/helmuser/.cache

if [ "$1" == "-r" ]; 
then 

      printf "helm repo add %s %s --username %s --password %s \n" $(ruby -ryaml -e "x=YAML.load(ARGF); d=Hash.new; x[\"dependencies\"].each { |c| d[c[\"name\"]] = c[\"repository\"]}; d.each { |k, v|; puts \"#{k} #{v}\"; system(\"python ./scripts/helm_repositories.py -u --repository #{v}\"); system(\"python ./scripts/helm_repositories.py -p --repository #{v}\"); }; " <.bob/eric-sc-crds/requirements.yaml) | bash ;
else
 
      printf "helm repo add %s %s --username %s --password %s \n" $(ruby -ryaml -e "x=YAML.load(ARGF); d=Hash.new; x[\"dependencies\"].each { |c| d[c[\"name\"]] = c[\"repository\"]}; d.each { |k, v|; puts \"#{k} #{v}\"; system(\"python ./scripts/helm_repositories.py -u --repository #{v}\"); system(\"python ./scripts/helm_repositories.py -p --repository #{v}\"); }; " <esc/helm/eric-sc-crds/requirements.yaml) | bash;

fi;
