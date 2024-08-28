#!/bin/bash -x


NAMESPACE=$1
KUBE_HOST=$2


CLEAN_HELM=1;
CLEAN_RETRIES=0;


while (( ${CLEAN_HELM}  && ${CLEAN_RETRIES} < 3 ))
do
    echo "------ Deleting the resources ------"
    helm ls --kubeconfig ~/.kube/${KUBE_HOST}.config --namespace ${NAMESPACE} -q
    
    if [ -z "$(helm ls --kubeconfig ~/.kube/${KUBE_HOST}.config --namespace ${NAMESPACE} -q)" ]
    then 
        echo "No release found in namespace ${NAMESPACE}"
        break ;
    fi
    
    for i in `helm ls --kubeconfig ~/.kube/${KUBE_HOST}.config --namespace ${NAMESPACE} -q`;
    do
            echo "Deleting release $i"
               
            output=$(helm uninstall $i --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE);
            if [ "$?" -ne 0 ] && [[ ${output} == *"error"* ]] 
            then
                CLEAN_HELM=1;
                echo "Deletion with helm failed!"

                ((CLEAN_RETRIES+=1));
                echo "Clean retries is ${CLEAN_RETRIES}"
             
                sleep 5
                break;
            fi
            echo "Resetting clean helm parameter!!"
            CLEAN_HELM=0;
    done;

done

#  jobs deployment statefulset  deamonSets chronjob

echo "---- jobs deployments statefulsets replicasets daemonsets CronJobs -- "

CLEAN_KUBECTL=1; 
CLEAN_RETRIES=0;

while (( ${CLEAN_KUBECTL}  && ${CLEAN_RETRIES} < 3 ))
do
       
    # if clean helm was bad please try again.Maximum retries are 3
    for resource in cronjobs daemonsets deployments  statefulsets job ;
    do 
        echo "----- Deleting resources ${resource} ------"
        
        output=$(kubectl delete ${resource} --all --namespace ${NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config);

        if [ "$?" -ne 0 ] && [[ ${output} == *"error"* ]] 
                then
                    CLEAN_KUBECTL=1;
                    echo "Deletion with kubectl deleting ${resource} failed!"

                    ((CLEAN_RETRIES+=1));
                    echo "Clean retries is ${CLEAN_RETRIES}"
                
                    sleep 5;
                    break;
        fi
        echo "Resetting clean kubectl parameter!!"
        CLEAN_KUBECTL=0;
    done;
done;

sleep 5 

echo "------ Deleting pods svc serviceaccounts configmaps httpproxy ---"

CLEAN_KUBECTL=1;
CLEAN_RETRIES=0;

while (( ${CLEAN_KUBECTL}  && ${CLEAN_RETRIES} < 3 ))
do

    for resource in pods svc serviceaccounts configmaps httpproxy;
    do 
        
        echo "----- Deleting resources ${resource}------"
        output=$(kubectl delete ${resource} --all --namespace ${NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config);

        if [ "$?" -ne 0 ] && [[ ${output} == *"error"* ]] 
                then
                    CLEAN_KUBECTL=1;
                    echo "Deletion with kubectl deleting ${resource} failed!"

                    ((CLEAN_RETRIES+=1));
                    echo "Clean retries is ${CLEAN_RETRIES}"
                
                    sleep 5;
                    break;
        fi
        echo "Resetting clean kubectl parameter!!"
        CLEAN_KUBECTL=0;
    done;
done;

sleep 5 


echo "------ Deleting secrets ----- "

CLEAN_KUBECTL=1;
CLEAN_RETRIES=0;

while (( ${CLEAN_KUBECTL}  && ${CLEAN_RETRIES} < 3 ))
do
   
    echo "----- Deleting resources secrets ------"
    
    
    if [ -z "$(kubectl get secrets  --kubeconfig ~/.kube/${KUBE_HOST}.config --namespace ${NAMESPACE} )" ]
    then 
        echo "Nothing found in namespace ${NAMESPACE} for resources ${resource}"
        break ;
    fi

    output=$(kubectl delete secrets --all --namespace ${NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config);

    if [ "$?" -ne 0 ] && [[ ${output} == *"error"* ]] 
            then
                CLEAN_KUBECTL=1;
                echo "Deletion with kubectl deleting ${resource} failed!"

                ((CLEAN_RETRIES+=1));
                echo "Clean retries is ${CLEAN_RETRIES}"
            
                sleep 5;
                break;
    fi
    echo "Resetting clean kubectl parameter!!"
    CLEAN_KUBECTL=0;
done;

echo "Deleting external certificates!!"
kubectl delete externalcertificates.com.ericsson.sec.certm --all -n ${NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config;
echo "Resetting namespace ${NAMESPACE} in ${KUBE_HOST}!"
kubectl delete ns ${NAMESPACE} --namespace ${NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config;
kubectl create ns ${NAMESPACE} --namespace ${NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config;