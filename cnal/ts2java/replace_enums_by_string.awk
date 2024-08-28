BEGIN {
	state = 0;
	buf = "";
	line = NR;
}

{
	if (match($0, /^#.*$/) || match($0, /^[ /t]*$/))
	{
		line = NR;
	}
	else if (NR == line + 1 && match($0, /^([ \t]+)description.*$/, a) && length(a[1]) == lindent[state] + 2)
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
		
		switch (state)
		{
			case 4:
				state = 0;
				line = 0;
				#printf ("state=%d, line=%d, lindent=%d\n", state, line, lindent[state]);
								
			case 0:
				if (match($0, /^([ \t]+)anyOf:.*$/, a))
				{
					state = 1;
					lindent[state] = length(a[1]);
					indent = a[1];
					line = NR;
					buf = (buf$0"\n");
					#printf ("state=%d, line=%d, lindent=%d\n", state, line, lindent[state]);
				}
				else
				{
					print $0;
					state = 0;
					line = 0;
					buf="";
				}
				break;
				
			case 1:
				if (NR == line + 1 && match($0, /^([ \t]+)- type: string.*$/, a))
				{
					state = 2;
					lindent[state] = length(a[1]);
					line = NR;
					buf = (buf$0"\n");
					#printf ("state=%d, line=%d, lindent=%d\n", state, line, lindent[state]);
				}
				else
				{
					print buf$0;
					state = 0;
					line = 0;
					buf="";
				}
				break;

			case 2:
				if (NR == line + 1 && match($0, /^([ \t]+)enum:.*$/, a))
				{
					state = 3;
					lindent[state] = length(a[1]);
					line = NR;
					buf = (buf$0"\n");
					#printf ("state=%d, line=%d, lindent=%d\n", state, line, lindent[state]);
				}
				else
				{
					print buf$0;
					state = 0;
					line = 0;
					buf="";
				}
				break;

			case 3:
				if (NR == line + 1 && match($0, /^([ \t]+)- .*$/, a) && length(a[1]) >= lindent[3])
				{
					line = NR;
					buf = (buf$0"\n");
					#printf ("state=%d, line=%d, lindent=%d\n", state, line, lindent[state]);
				}
				else if (NR == line + 1 && match($0, /^([ \t]+)- type: string.*$/, a) && length(a[1]) == lindent[2])
				{
					print indent"type: string";
			
					state = 4;
					lindent[state] = length(a[1]);
					line = NR;
					#buf = (buf$0"\n");
					#printf ("state=%d, line=%d, lindent=%d\n", state, line, lindent[state]);
				}
				else
				{
					print buf$0;
					state = 0;
					line = NR;
					buf="";
				}
				break;				
		}
	}
}

