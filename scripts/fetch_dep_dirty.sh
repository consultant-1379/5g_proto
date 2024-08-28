#!/bin/bash -x

chartsDirectory=.bob/eric-sc-umbrella/charts/
dirtyChartsDirectory=dirty_charts
dirtyServices=($(cat .bob/var.dirty-service))
dirtyTgzs=($(cat .bob/var.dirty-tgz))

if [ ${#dirtyServices[@]} -eq 0 ] && [ ${#dirtyTgzs[@]} -eq 0 ]; then
	echo "No dirty packages identified";
	exit 0;
fi

if [ ${#dirtyServices[@]} -ne ${#dirtyTgzs[@]} ]; then
	echo "Inconsistency between dirty images and tgz files";
	exit 1;
fi

echo "${#dirtyServices[@]} dirty services identified"
for ((i=0;i<${#dirtyServices[@]};i++))
do
	oldService=($(ls ${chartsDirectory} | grep ${dirtyServices[i]}));
	if [ ${#oldService[@]} -gt 1 ]; then
		echo "Multiple services with the same name identified. Dirty charts function terminated."
		exit 1;
	fi
	
	if [ ${#oldService[@]} -eq 1 ]; then
		echo "Deleting old service ${chartsDirectory}/${oldService}"
		rm ${chartsDirectory}/${oldService[@]}
	fi
	echo ${dirtyTgzs[i]}
	echo "Introduce dirty service ${dirtyChartsDirectory}/${dirtyTgzs[i]}"
	cp ${dirtyChartsDirectory}/${dirtyTgzs[i]} ${chartsDirectory}
done
