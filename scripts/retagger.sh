#!/bin/bash


print_usage () {
    echo "Usage: "$0 PRIVATE_REGISTRY_URL IMAGES_FILE
    echo ""
    echo "     PRIVATE_REGISTRY_URL:    the private registry URL for the docker images to be pushed."
    echo "     IMAGES_FILE:             the images list file"
    echo ""
    echo "Example: $0 'k8s-registry.eccd.local:32330'  Files/images.txt"
}


print_desc () {
    echo "This script performes $DOCKER_BINARY tag and push to the provided PRIVATE_REGISTRY_URL"
    echo "for the images listed in the provided IMAGES_FILE."
}


#Check with binary to use: docker or nerdctl
if which docker > /dev/null 2>&1; then
    DOCKER_BINARY=docker
elif which nerdctl > /dev/null 2>&1; then
    DOCKER_BINARY=nerdctl
else
    echo "No docker or nerdctl binary found."
	echo "Please install either binary first to proceed."
	exit 0
fi

#checking the arguments
if [ "$1" == "-h" ]; then
    print_desc
    echo " "
    print_usage
    exit 1
elif [ "$#" != 2 ]; then
    echo "Illegal number of parameters"
    print_usage
    exit 1
fi

PRIVATE_REGISTRY_URL=$1
IMAGES_FILE=$2


#check PRIVATE_REGISTRY_URL
echo "Checking private registry URL..."
echo ""
echo "Executing $DOCKER_BINARY login $PRIVATE_REGISTRY_URL"
echo "=============================================="

sudo $DOCKER_BINARY login $PRIVATE_REGISTRY_URL

if [ $? != 0 ]; then
    echo "Failed to connect to the private registry: $PRIVATE_REGISTRY_URL. Exiting..."
    exit 1
fi



#check IMAGES_FILE
if [ ! -f $IMAGES_FILE ]; then
    echo ""
    echo "The file: $IMAGES_FILE is not found. Exiting..."
    exit 1
fi



numberOfImages=0

#check if the images exists in the registry
echo -n "Checking registy images..."
echo "=============================================="
while IFS= read -r url || [[ -n "$url" ]]
do
    sudo $DOCKER_BINARY inspect $url > /dev/null
    if [ $? != 0 ]; then
        echo ""
        echo "The image $url is not found. Exiting..."
        exit 1
    fi

    urls[$numberOfImages]=$url
    #escape slashes
    reg_urls=$(echo $PRIVATE_REGISTRY_URL | sed 's/\//\\\//g')
#    urls_retagged[$numberOfImages]=`echo $url | sed "s/^[^/]*/$PRIVATE_REGISTRY_URL/g"`
    urls_retagged[$numberOfImages]=$(echo $url | sed -z "s/^[^/]*/$reg_urls/g")

    numberOfImages=$((numberOfImages+1))

done < "$IMAGES_FILE"
echo "     OK"


#retag and push the images
echo""
echo "Start retaging and pushing the images..."
echo "=============================================="

for url in ${urls[@]}
do
    reg_urls=$(echo $PRIVATE_REGISTRY_URL | sed 's/\//\\\//g')
    new_url=$(echo $url | sed "s/^[^/]*/$reg_urls/g")

    echo ""
    echo "Retaging image $new_url"
    echo "=============================================="
    sudo $DOCKER_BINARY tag $url $new_url
    
    if [ $? != 0 ]; then
        echo ""
        echo "The image $url could not be retagged. Exiting..."
        exit 1
    fi

    echo ""
    echo "Pushing image $new_url"
    echo "=============================================="
    sudo $DOCKER_BINARY push $new_url

    if [ $? != 0 ]; then
        
        echo ""
        echo "The image $new_url could not be pushed. Exiting..."
        exit 1
    fi

done 
echo "Retagging and pushing of images is complete..."
echo "=============================================="


#Cleaning the registry
echo ""
echo -n "Cleaning the registry..."
echo "=============================================="
for url in ${urls[@]}
do  
    sudo $DOCKER_BINARY rmi $url > /dev/null

    if [ $? != 0 ]; then
        echo ""
        echo "The image $url could not be untagged. Exiting..."
        # exit 1
    fi

done
echo "     OK"


echo ""
echo "*******************************************************************************"
echo "The images in $IMAGES_FILE were successfully retagged and pushed to $PRIVATE_REGISTRY_URL."
echo "*******************************************************************************"

for url in ${urls_retagged[@]}
do
    echo $url
done
echo "*******************************************************************************"
