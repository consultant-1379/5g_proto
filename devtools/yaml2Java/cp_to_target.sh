#!/bin/bash

target_dir=$1

if [[ ! -z "`ls -A java`" ]]
then
	# Copy only classes starting with a character.
	# Classes starting with a number are specially treated.
	#
	for f in java/[A-Z]*.java
	do 
		target=$target_dir/${f##*/}

		echo "Copying $target"
		cp -n $f $target
	done

	# Classes starting with a number will be prefixed with "Model",
	# as this is what the YAML2Java compiler does in such cases.
	# As the file name is changed, the class name must be adapted, too.
	#
	for f in java/[0-9]*.java
	do
		n=${f##*/}	# 5G.java
		c=${n%%.java}	# 5G

		sed -i "s/$c/Model$c/g" $f

		target="$target_dir/Model$n"

		echo "Copying $target"
		cp -n $f $target
	done
fi
