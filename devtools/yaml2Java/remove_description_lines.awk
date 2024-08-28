BEGIN {
	line = NR;
}

{
	if (match($0, /^#.*$/) || match($0, /^[ /t]*$/))
	{
		inDescription = 0;
		line = NR;
	}
	else if (match($0, /^([ \t]+)description.*$/, a))
	{
		line = NR;
		inDescription = 1;
		lindent["description"] =  length(a[1]);
	}
	else if (inDescription == 1 && NR == line + 1 && match($0, /^([ \t]+).+$/, a) && length(a[1]) == lindent["description"] + 2)
	{
		line = NR;	
	}
	else
	{
		inDescription = 0;
		print $0;
	}
}

