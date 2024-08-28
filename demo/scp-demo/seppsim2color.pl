#!   /usr/bin/perl
#
# @CHFSIM1@ip.addr==10.101.216.165@[0,26728,49087][0,0,0]
# @CHFSIM2@ip.addr==10.98.147.167@[50886,43176,8995][0,0,0]
# @CHFSIM3@ip.addr==10.107.0.86@[3084,39835,23387][0,0,0]
#



$user = $ENV{SUDO_USER};
print "You are this script with sudo as '$user'\n";

if ($user eq "") {
 die "You need to use sudo to call this script\n";
}

@colors=("",
        "[0,26728,49087]",
        "[50886,43176,8995]",
        "[3084,39835,23387]",
        "[52428,10280,10280]",
        "[52428,28784,1799]",
        "[35980,24672,43176]",
        "[45746,55512,63993]"
        );


$command = "kubectl -n 5g-bsf-${user} get svc -o=custom-columns=:.metadata.name,:.spec.clusterIP | grep seppsim";
$outfile = "/home/$user/.config/wireshark/colorfilters.new";
$infile = "/home/$user/.config/wireshark/colorfilters";
$backupfile = "/home/$user/.config/wireshark/colorfilters.bak";
$startmarker = 0;

open CMD,'-|',$command or die $@;

#open(OUT,">","~/.config/wireshark/colorfilters.new");
open OUT,">", "$outfile" or die $@;

my $line;
print "\nAdding those lines on top of $infile\n";

while (defined($line=<CMD>)) {

    if ($line =~ /(eric-seppsim-p)(\d+)-\S+\s+(\d+\.\d+\.\d+\.\d+)/ ) {
                #print "\@$1\@ip.addr==\$2\@[0,26728,49087][0,0,0]\n";
                printf ("\@%s-%d\@ip.addr==\%s\@%s[0,0,0]\n" , $1, $2, $3, $colors[$2]);
                printf (OUT "\@%s-%d\@ip.addr==\%s\@%s[0,0,0]\n" , $1, $2, $3,  $colors[$2]);
                }
}
close CMD;

open IN,"<", "$infile" or die $@;
while(<IN>) {
        $startmarker = 1 if (/# DO NOT EDIT THIS FILE!  It was created by Wireshark/);

    if ($startmarker) {
                print OUT;
        }
}

#@m = <IN>;
close(IN);

#print OUT @m;
close(OUT);

system("cp $infile $backupfile");
system("mv $outfile $infile");

print "\nBackup file generated: $backupfile\n\n";
