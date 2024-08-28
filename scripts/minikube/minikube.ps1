#Minikube Launcher/Installer
#Created by Bjoern Hellebrand (eedbjhe)
#More info: https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/Autoinstaller

#Revision Info
#1.0    - Initial version
#1.0.1  - Store kubernetes version in Minikube config after installation
#       - Upgraded Kubernetes 1.13.0 -> 1.13.5
#       - Added missing "stop" function
#1.0.2  - Added wrapper for "minikube ip"     
#       - Updated Kubernetes 1.13.5 -> 1.15.3 to be equal to the ECCD 2.5 Kubernetes version
#       - Enable Metrics Server after Minikube installation
#1.0.3  - Disabled "New version available" notification
#       - Upgraded Helm to 2.14.2 to equal to the ECCD 2.5 version
#       - Upgraded kubectl to 1.15.3 to equal to the ECCD 2.5 version
#1.0.4  - Changed check for the minikube VM to match the name exactly and not partially
#1.0.5  - Small formatting fix in version printout
#       - Added possibility to set an ECCD version which will set the matching kubernetes, kubectl and helm versions used in the ECCD release
#1.0.6  - Added ECCD 2.6.1, 2.5.2 and corrected ECCD 2.6.0
#       - Upgraded to Minikube 1.6.2
#       - Fixed downloads by setting TLS to v1.2
#1.0.7  - Switched to ECCD 2.6.1 software level as default
#         Create folder in Minikube VM after start for ADP Diameter component compatibility
#         Updated to Minikube 1.7.1 -> Preserves 20% CPU overhead
#         Ingress is enabled by default
#         Added ECCD 2.7 (Untested)
#1.0.8  - Updated to Minikube 1.7.2
#       - Minikube Ingress disabled
#1.0.9  - Updated to Minikube 1.9.2
#       - ECCD 2.8 added (with helm 2 support)
#       - Switched to ECCD 2.7
#1.0.10 - Updated to Minikube 1.12.0
#       - Switched to ECCD 2.8
#1.0.11 - Updated to Minikube 1.12.3
#1.1.0  - Added helm 3 support
#       - Updated Minikube to 1.15.1
#       - Added new ECCD versions
#       - Switched to ECCD 2.12.0
#       - Added ECCD 2.13.x
#       - Disabled metrics server to preserve resources


#minikube config set feature-gates SCTPSupport=true -> Unavailable in Windows

#Todo: Kubectl, check if host only nic exists and create if necessary
# .\vboxmanage.exe showvminfo minikube | Select-String -Pattern nic
# - Remove pattern for Minikube VM namematch

#Kubernetes Version, Kubectl Version, Helm Version
$eccd_2_13_1 = @{ kubernetes = "v1.18.9"; kubectl = "v1.18.9"; helm = "v2.16.12"; helm3 = "v3.3.3"} #Same as 2.13.0
$eccd_2_13_0 = @{ kubernetes = "v1.18.9"; kubectl = "v1.18.9"; helm = "v2.16.12"; helm3 = "v3.3.3"}
$eccd_2_12_0 = @{ kubernetes = "v1.18.8"; kubectl = "v1.18.8"; helm = "v2.16.9"; helm3 = "v3.3.0"} #Helm 3.3.0 introduced, 2 still included
$eccd_2_11_0 = @{ kubernetes = "v1.18.2"; kubectl = "v1.18.2"; helm = "v2.16.1"; helm3 = "v3.2.1"} #Helm 3.2.1 introduced, 2 still included
$eccd_2_10_0 = @{ kubernetes = "v1.18.2"; kubectl = "v1.18.2"; helm = "v2.16.1"; helm3 = "v3.2.0"} #Helm 3.2.0 introduced, 2 still included
$eccd_2_9_1 = @{ kubernetes = "v1.17.3"; kubectl = "v1.17.3"; helm = "v2.16.1"; helm3 = "v3.1.0"} #Helm 3.1.0 introduced, 2 still included
$eccd_2_8_0 = @{ kubernetes = "v1.17.3"; kubectl = "v1.17.3"; helm = "v2.16.1"; helm3 = "v3.1.0"} #Helm 3.1.0 introduced, 2 still included
$eccd_2_7_0 = @{ kubernetes = "v1.16.3"; kubectl = "v1.16.3"; helm = "v2.16.1"} 
$eccd_2_6_1 = @{ kubernetes = "v1.16.2"; kubectl = "v1.16.2"; helm = "v2.15.1"} #Same as 2.6.0
$eccd_2_6_0 = @{ kubernetes = "v1.16.2"; kubectl = "v1.16.2"; helm = "v2.15.1"}
$eccd_2_5_2 = @{ kubernetes = "v1.15.3"; kubectl = "v1.15.3"; helm = "v2.14.2"} #Same as 2.5.0
$eccd_2_5_1 = @{ kubernetes = "v1.15.3"; kubectl = "v1.15.3"; helm = "v2.14.2"} #Same as 2.5.0
$eccd_2_5_0 = @{ kubernetes = "v1.15.3"; kubectl = "v1.15.3"; helm = "v2.14.2"}
$eccd_2_4_0 = @{ kubernetes = "v1.15.3"; kubectl = "v1.15.3"; helm = "v2.12.2"}
$eccd_2_3_0 = @{ kubernetes = "v1.14.2"; kubectl = "v1.14.2"; helm = "v2.12.2"}
$eccd_2_2_0 = @{ kubernetes = "v1.13.5"; kubectl = "v1.13.5"; helm = "v2.12.2"}
$eccd_2_1_0 = @{ kubernetes = "v1.13.5"; kubectl = "v1.13.5"; helm = "v2.12.2"}

#Specify which ECCD version to use
$eccd = $eccd_2_12_0

#Config Part - Feel free to edit these parameters
$minikube_cpu = 4
$minikube_ram = 12288
$minikube_disk = 61440
$minikube_kubernetes = $eccd["kubernetes"]
$helm_version = 3

#$version_minikube = "v1.12.1"
#$version_minikube = "v1.13.1"
#$version_minikube = "v1.14.2"
$version_minikube = "v1.15.1"
$version_helm = $eccd["helm"]
$version_kubectl = $eccd["kubectl"]

if ($helm_version -match "2"){
	$version_helm = $eccd["helm"]
} else {
	$version_helm = $eccd["helm3"]
}

$version_tartool = "1.0.0"
$version_wget = "1.20.3"

$dir_vbox = "C:\Program Files\Oracle\VirtualBox"
$dir_work = "C:\Temp\Minikube-Setup"
$dir_install = "$env:userprofile\Software\Minikube"
$env:HELM_HOME = "$dir_install\.helm"
$env:MINIKUBE_HOME = "$env:userprofile\Software\Minikube"

#Do not edit below this line
$version = "1.1.0"

$url_wget = "https://eternallybored.org/misc/wget/$version_wget/64/wget.exe"
$url_minikube = "https://github.com/kubernetes/minikube/releases/download/$version_minikube/minikube-windows-amd64.exe"
$url_helm = "https://get.helm.sh/helm-$version_helm-windows-amd64.zip"
$url_kubectl = "https://dl.k8s.io/$version_kubectl/kubernetes-client-windows-amd64.tar.gz"
$url_tartool = "https://github.com/senthilrajasek/tartool/releases/download/$version_tartool/TarTool.zip"

$env:Path += ";$dir_install"

#Enable TLS1.2 to suppress download error/warning
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$wc = New-Object System.Net.WebClient


function download([string]$url, [string]$version, [string]$name)
{
	if (!(Test-Path $dir_work\$name-$version)) { 
		echo "[INFO] Downloading $name $version."
		& $dir_work\wget.exe $url -P $dir_work\$name-$version -q --show-progress
		} else {
			echo "[INFO] $name $version found - Skipping download."
	}
}


function installMinikube()
{
	echo "[INFO] Installing Minikube."
	& minikube start --vm-driver=virtualbox --memory=$minikube_ram --cpus=$minikube_cpu --disk-size=$minikube_disk --kubernetes-version=$minikube_kubernetes --v=7 
	& minikube config set vm-driver virtualbox
	& minikube config set kubernetes-version $minikube_kubernetes
	& minikube config set WantUpdateNotification false
	echo "[INFO] Saving minikube.conf to $dir_install."
	$ip = & minikube ip
	echo "[INFO] Minikube IP: $ip"
	$conf = & minikube ssh 'sudo cat /etc/kubernetes/admin.conf'
	
    #Not working anymore?
	$conf -replace "control-plane.minikube.internal", "$ip" > $dir_install/minikube.conf
	#For versions prior 1.10.1
	#$conf -replace "localhost", "$ip" > $dir_install/minikube.conf
	
#	if ($version -match "1.10.1") {
#	    $conf -replace "control-plane.minikube.internal", "$ip" > $dir_install/minikube.conf
#	} else {	
#	    $conf -replace "localhost", "$ip" > $dir_install/minikube.conf
#	}
}


echo "######################################"
echo "# Minikube Installer/Launcher $version #"
echo "######################################"
echo "`n"

if ($args[0] -match "install") {

	#Check if Minikube VM exists
	$vms = & $dir_vbox\VboxManage.exe list vms | Select-String -Pattern ^minikube$
	$number_of_vms = $vms.Matches.Count

	if ($number_of_vms -gt 0) {
		echo "[WARNING] Minikube VM found - Please remove first and rerun installation."
	} else {	
		#The workdir contains the downloaded files that will be installed/copied
		if (!(Test-Path $dir_work)) { 
			echo "[INFO] Creating Setup directory $dir_work."
			mkdir $dir_work
		} else {
			echo "[INFO] Workdir found."
		}
		#Download wget since it provides download progress information
		if (!(Test-Path $dir_work\wget.exe)) { 
			echo "[INFO] Downloading wget."
			$start_time = Get-Date
			$wc.DownloadFile($url_wget, "$dir_work\wget.exe")
			Write-Output "[INFO] Download time: $((Get-Date).Subtract($start_time).Seconds) second(s)"
		} else { 	
			echo "[INFO] Wget found - Skipping download." 
		}
		
		download $url_tartool $version_tartool "Tartool"
		echo "[INFO] Extracting Tartool"
		expand-archive -path "$dir_work\Tartool-$version_tartool\Tartool.zip" -destinationpath "$dir_work\Tartool-$version_tartool" -Force
		
		#Download the Minikube binary
		download $url_minikube $version_minikube "Minikube"

		if (!(Test-Path $dir_install)) { 
			echo "[INFO] Creating Minikube directory."
			mkdir $dir_install
		} else {
			echo "[INFO] Minikube directory found."
		}
		echo "[INFO] Copying Minikube $version_minikube binary."
		cp $dir_work\Minikube-$version_minikube\minikube-windows-amd64.exe $dir_install\minikube.exe
		
		#Download the helm binary
		download $url_helm $version_helm "helm"

		echo "[INFO] Extracting helm"
		expand-archive -path "$dir_work\helm-$version_helm\helm-$version_helm-windows-amd64.zip" -destinationpath "$dir_install" -Force
		mv $dir_install\windows-amd64\* $dir_install -Force
		rm -r $dir_install\windows-amd64
		
		#Download the kubectl binary
		download $url_kubectl $version_kubectl "kubectl"

		echo "[INFO] Extracting kubectl"
		& $dir_work\Tartool-$version_tartool\Tartool.exe $dir_work\kubectl-$version_kubectl\kubernetes-client-windows-amd64.tar.gz $dir_install
		#expand-archive -path "$dir_work\helm-$version_helm\helm-$version_helm-windows-amd64.zip" -destinationpath "$dir_install" -Force
		mv $dir_install\kubernetes\client\bin\* $dir_install -Force
		rm -r $dir_install\kubernetes	
	
		installMinikube

        if ($helm_version -match "2"){
			echo "[INFO] Installing Tiller in Minikube."
			& helm init
		}	
		#echo "[INFO] Enabling Metrics Server in Minikube."
		#& minikube addons enable metrics-server
		
		#echo "[INFO] Enabling ingress in Minikube."
		#& minikube addons enable ingress
		
		echo "[INFO] Creating /boot directory for compatibility with the ADP Diameter component"
	    & minikube ssh 'sudo mkdir -p /boot'
		
		echo "[INFO] Done. Minikube is setup."		
		echo ""
		echo "[INFO] In order for the ingress to work, update the /etc/hosts file in your development VM by adding this line:"
		$ip = & minikube ip
		echo "[INFO] $ip minikube"
	}
}	


if ($args[0] -match "delete") {

	&minikube delete
		
}


if ($args[0] -match "clean") {

	&minikube delete
	rm -r $dir_work -Force
	rm -r $dir_install -Force
		
}


if ($args[0] -match "start") {

	&minikube start --kubernetes-version=$minikube_kubernetes --insecure-registry "192.168.99.0/24"
	#TODO: Check if still needed
	echo "[INFO] Creating /boot directory for compatibility with the ADP Diameter component"
	& minikube ssh 'sudo mkdir -p /boot'

}


if ($args[0] -match "stop") {

	&minikube stop
	
}


if ($args[0] -match "ip") {

	&minikube ip
	
}