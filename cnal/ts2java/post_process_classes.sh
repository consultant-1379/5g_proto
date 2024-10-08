#!/bin/bash
#
# Some post processing of generatod Java classes.
#
# Synopsis: post_process_classes.sh <path-to-5g_proto>

echo "$0: starting ..."

#set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

# Using absolute paths everywhere.
#
YAML_TO_JAVA_DIR="`pwd`/../../devtools/yaml2Java"
IN_TS_DIR="`pwd`/../src/main/resources/3gpp_r17"
IN_TMPL_DIR="`pwd`/templates/r17"
OUT_JAVA_DIR="`pwd`/tmp/tmp/src/main/java"
OUT_SRC_DIR="tmp/tmp/src/main/java/com/ericsson/cnal/openapi/r17"

# Generate enums as these are not generated by the code generator if enum is part of a YAML-oneof definition.
#
function generate_enums()
{
	echo "$0: Generating enums as these are not generated by the code generator if enum is part of a YAML-oneof definition."
	for ts in "$IN_TS_DIR"/TS*.yaml
	do
	    echo "$0: $ts"                                   # $IN_TS_DIR/TS29503_Nudm_PP.yaml
	    package=${ts##*/}                                # TS29503_Nudm_PP.yaml
	    package=${package%.yaml}                         # TS29503_Nudm_PP
	#    package=${package/TS[0-9][0-9][0-9][0-9][0-9]_/} # Nudm_PP
	    package=${OUT_SRC_DIR##*java/}"/"$package
	    package=${package//\//.}                         # Replace all '/' by '.'
	    package=${package//_/.}                          # Replace all '_' by '.'
	    package=${package,,}                             # Convert to lower case
	    echo "$0: $package"
	
	    pushd $YAML_TO_JAVA_DIR
	    rm -rf java resources
	    mkdir resources
	    cp "$ts" resources/
	    mkdir java
	    yaml2java "$package"
	    cp_to_target.sh "$OUT_JAVA_DIR/${package//.//}"
	    #rm -rf java resources
	    popd
	done
}

# Copy template files to their destinations (according to their Java package).
# Template files contain corrections that must always be applied after code generation.
#
function copy_template_files_to_destination()
{
	echo "$0: Copying template files to their destinations (according to their Java package)."
	for f in $IN_TMPL_DIR/*.java
	do
	    dest=`egrep "^package.*$" "$f"` # package com.ericsson;
	    dest=${dest#*package }          # com.ericsson;
	    dest=${dest%;*}                 # com.ericsson
	    dest=${dest//.//}               # com/ericsson
	    
	    echo "$0: cp -f $f $OUT_JAVA_DIR/$dest"
	    cp -f "$f" "$OUT_JAVA_DIR/$dest"
	done
}

# Search for classes with no members and replace all references to them by Object.
#
function replace_faulty_classes_by_object()
{
	echo "$0: Searching for classes with no members and replace all references to them by Object."
	for faulty_class_file in `grep -lr "Objects.hash()" tmp/tmp | sort`
	do
		echo "$0: faulty class file: ${faulty_class_file}"
		path=${faulty_class_file%/*}              # Remove /X.java
		import=${faulty_class_file/*"/com/"/com/} # "com/ericsson/X.java"
		import=${import%.java}                    # "com/ericsson/X"
		import="import ${import//\//.}"           # import com.ericsson.X
		faulty_class=${import##*.}                # X
		
		#echo "${path}"
		#echo "${faulty_class}"
		#echo "${import}"
		
		echo "$0: Replacing ${faulty_class} by Object in all Java files in the folder of the faulty class file."
		find "${path}" -name "*.java" | xargs -r sed -i -r 's/([^a-zA-Z0-9])'"${faulty_class}"'([^a-zA-Z0-9])/\1Object\2/g'
		
		echo "$0: Replacing ${faulty_class} by Object in all Java files importing the faulty class."
		grep -lrZ "${import};" "$OUT_SRC_DIR" | xargs -0 -r sed -i -r 's/([^a-zA-Z0-9])'"${faulty_class}"'([^a-zA-Z0-9])/\1Object\2/g'
	
		echo "$0: Removing ${import} from all Java files importing the faulty class."
		grep -lrZ "${import};" "$OUT_SRC_DIR" | xargs -0 -r sed -i '/${import};/d'
			
		echo "$0: Removing faulty Java file ${faulty_class_file}"
		rm -f ${faulty_class_file}
	
		echo
	done
}

# Cleanup
#
function cleanup()
{
	# Remove unused Java files.
	#
	echo "$0: Removing unused Java files."
	for f in ApiClient.java ApiException.java Configuration.java CustomInstantDeserializer.java JavaTimeFormatter.java RFC3339DateFormat.java ServerConfiguration.java ServerVariable.java StringUtil.java *AllOf.java *AnyOf.java
	do
	    find "$OUT_SRC_DIR" -name "$f" | xargs rm -f
	done
	
	# Replace AnyOfstringstring by String in all Java files.
	#
	echo "$0: Replacing AnyOfstringstring by String in all Java files."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i -r 's/([^a-zA-Z0-9])AnyOfstringstring([^a-zA-Z0-9])/\1String\2/g'
	
	# Replace AnyOfNFTypearray by Object in all Java files.
	#echo "$0: Replacing AnyOfNFTypearray by Object in all Java files."
	#find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i -r 's/([^a-zA-Z0-9])AnyOfNFTypearray([^a-zA-Z0-9])/\1Object\2/g'
	
	# Replace OneOfAv5gAkastring by Object in all Java files.
	echo "$0: Replacing OneOfAv5gAkastring by Object in all Java files."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i -r 's/([^a-zA-Z0-9])OneOfAv5gAkastring([^a-zA-Z0-9])/\1Object\2/g'
	
	# Replace OneOfstringintegernumberbooleanobject by Object in all Java files.
	echo "$0: Replacing OneOfstringintegernumberbooleanobject by Object in all Java files."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i -r 's/([^a-zA-Z0-9])OneOfstringintegernumberbooleanobject([^a-zA-Z0-9])/\1Object\2/g'
	
	# Remove all lines starting with "@javax.annotation.Generated" from all Java files.
	# Reason being that these lines are changing with every generation, hence causing lots of unnecessary diffs in git.
	#
	echo "$0: Removing all lines starting with '@javax.annotation.Generated' from all Java files."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i '/@javax.annotation.Generated/d'
	
	# Replace org.threeten.bp.OffsetDateTime by java.time.OffsetDateTime in all Java files.
	#
	echo "$0: Replacing org.threeten.bp.OffsetDateTime by java.time.OffsetDateTime in all Java files."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i 's/org.threeten.bp.OffsetDateTime/java.time.OffsetDateTime/g'
	
	# Add @JsonFormat(...) before all getters returning Date in order to serialize Date as string, formatted according to ISO 8601.
	#
	echo "$0: Adding @JsonFormat(...) before all getters returning Date in order to serialize Date as string, formatted according to ISO 8601."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i ':a;N;$!ba;s/\n\([ ]*\)\(public Date\)/\1@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")\n\n\1\2/g'
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i 's/import java.util.time.Date;/import java.util.time.Date;\nimport com.fasterxml.jackson.annotation.JsonFormat;/g'
	
	# Add @JsonFormat(...) before all getters returning OffsetDateTime in order to serialize OffsetDateTime as string, formatted according to ISO 8601.
	#
	echo "$0: Adding @JsonFormat(...) before all getters returning OffsetDateTime in order to serialize OffsetDateTime as string, formatted according to ISO 8601."
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i ':a;N;$!ba;s/\n\([ ]*\)\(public OffsetDateTime\)/\1@JsonFormat(shape = JsonFormat.Shape.STRING)\n\n\1\2/g'
	find "$OUT_SRC_DIR" -name "*.java" | xargs sed -i 's/import java.time.OffsetDateTime;/import java.time.OffsetDateTime;\nimport com.fasterxml.jackson.annotation.JsonFormat;/g'
}

generate_enums
replace_faulty_classes_by_object
copy_template_files_to_destination
cleanup

popd

# Exclude members not used by SC from deserialization.
#
ignore_unused_members.sh "$1"

echo "$0: finished."

