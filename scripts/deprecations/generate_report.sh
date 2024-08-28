#! /bin/bash +x

# set output directory
OUTPUT=${1}

# flags to count adp deprecation issues per status
created=0
updated=0
unchanged=0
unknown=0

# list of adp deprecation issues per status
newDndCases=()
newAdpCases=()
modifiedDndCases=()
modifiedAdpCases=()
existingDndCases=()
existingAdpCases=()

# get deprecation
getDeprecation() {
	project=array_${1}_${2}[project]
	component=array_${1}_${2}[component]
	issueKey=array_${1}_${2}[issueKey]
	issueLink=array_${1}_${2}[issueLink]
	deprecationKey=array_${1}_${2}[deprecationKey]
	status=array_${1}_${2}[status]
}

while IFS="," read -r projectColumn componentColumn issueKeyColumn issueLinkColumn deprecationKeyColumn statusColumn
do
	echo "project: $projectColumn"
	echo "component: $componentColumn"
	echo "issue-key: $issueKeyColumn"
	echo "issue-link: $issueLinkColumn"
	echo "deprecation-key: $deprecationKeyColumn"
	echo "status: $statusColumn"

	case ${statusColumn} in
		"created")
			declare -gA array_1_${created}
			eval array_1_${created}[project]=$projectColumn
			#eval array_1_${created}[component]=$componentColumn
			eval array_1_${created}[issueKey]=$issueKeyColumn
			eval array_1_${created}[issueLink]=$issueLinkColumn
			eval array_1_${created}[deprecationKey]=$deprecationKeyColumn
			eval array_1_${created}[status]=$statusColumn
			((created+=1))
			;;
		"updated")
			declare -gA array_2_${updated}
			eval array_2_${updated}[project]=$projectColumn
			#eval array_2_${updated}[component]=$componentColumn
			eval array_2_${updated}[issueKey]=$issueKeyColumn
			eval array_2_${updated}[issueLink]=$issueLinkColumn
			eval array_2_${updated}[deprecationKey]=$deprecationKeyColumn
			eval array_2_${updated}[status]=$statusColumn
			((updated+=1))
			;;
		"unchanged")
			declare -gA array_3_${unchanged}
			eval array_3_${unchanged}[project]=$projectColumn
			#eval array_3_${unchanged}[component]=$componentColumn
			eval array_3_${unchanged}[issueKey]=$issueKeyColumn
			eval array_3_${unchanged}[issueLink]=$issueLinkColumn
			eval array_3_${unchanged}[deprecationKey]=$deprecationKeyColumn
			eval array_3_${unchanged}[status]=$statusColumn
			((unchanged+=1))
			;;
		*)
			echo "Error: unknown record"
			declare -gA array_4_${unknown}
			eval array_4_${unknown}[project]=$projectColumn
			#eval array_4_${unknown}[component]=$componentColumn
			eval array_4_${unknown}[issueKey]=$issueKeyColumn
			eval array_4_${unknown}[issueLink]=$issueLinkColumn
			eval array_4_${unknown}[deprecationKey]=$deprecationKeyColumn
			eval array_4_${unknown}[status]=$statusColumn
			((unknown+=1))
			;;
	esac
	echo ""
done < <(tail -n +2 ${OUTPUT}/report.csv | sed -e 's/\"//g' )

# print number of issues per status
echo "created: ${created}"
echo "updated: ${updated}"
echo "unchanged: ${unchanged}"
echo "unknown: ${unknown}"
echo ""

# check if records with invalid/unknown status identified
if [[ ${unknown} -gt 0 ]]; then
	echo "Error: Identified records with invalid/unknown status";
	exit 1;
fi

# Create the HTML file
cat <<EOL >${OUTPUT}/report.html
<!DOCTYPE html>
<html>
<head>
<title>ADP Deprecations</title>
</head>
<body>

<h1>ADP Deprecations</h1>

<h2>New ADP Deprecations</h2>
<table border="1">
<tr>
<th>DND cases</th>
<th>ADPPRG cases</th>
</tr>
EOL

# Create table with new deprecations
for (( i=1; i<${created}; i++ )); do
	getDeprecation 1 ${i}
	echo "<tr><td><a href='${!issueLink}' target='_blank'>${!issueKey}</a></td><td><a href='https://eteamproject.internal.ericsson.com/browse/${!deprecationKey}' target='_blank'>${!deprecationKey}</a></td></tr>" >> ${OUTPUT}/report.html
done
echo "</table><br>" >> ${OUTPUT}/report.html

cat <<EOL >>${OUTPUT}/report.html
<h2>Modified Existing ADP Deprecations</h2>
<table border="1">
<tr>
<th>DND cases</th>
<th>ADPPRG cases</th>
</tr>
EOL

# Create table with existing/know deprecations that modified
for (( i=1; i<${updated}; i++ )); do
	getDeprecation 2 ${i}
	echo "<tr><td><a href='${!issueLink}' target='_blank'>${!issueKey}</a></td><td><a href='https://eteamproject.internal.ericsson.com/browse/${!deprecationKey}' target='_blank'>${!deprecationKey}</a></td></tr>" >> ${OUTPUT}/report.html
done
echo "</table><br>" >> ${OUTPUT}/report.html

cat <<EOL >>${OUTPUT}/report.html
<h2>Existing ADP Deprecations</h2>
<table border="1">
<tr>
<th>DND cases</th>
<th>ADPPRG cases</th>
</tr>
EOL

# Create table with existing/known deprecations
for (( i=1; i<${unchanged}; i++ )); do
	getDeprecation 3 ${i}
	echo "<tr><td><a href='${!issueLink}' target='_blank'>${!issueKey}</a></td><td><a href='https://eteamproject.internal.ericsson.com/browse/${!deprecationKey}' target='_blank'>${!deprecationKey}</a></td></tr>" >> ${OUTPUT}/report.html
done
echo "</table><br>" >> ${OUTPUT}/report.html


# Closing HTML tags
cat <<EOL >>${OUTPUT}/report.html
</body>
</html>
EOL

echo "report.html created successfully!"