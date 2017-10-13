#!/bin/bash
if test ! -d "$1"
then
	echo "Argument must be a directory"
else
	if test ! -f "the-one_v1.6.0.patch"
	then
		echo "Patch must be in the same directory of apply_patch.sh"
	else
		path=`pwd`
		cd "$1"
		patch -p0 < "$path/the-one_v1.6.0.patch"
	fi
fi
