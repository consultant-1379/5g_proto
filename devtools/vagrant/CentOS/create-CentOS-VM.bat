@echo off

REM BASE
echo "BASE: Installing Vagrant guest OS plugin..."
vagrant plugin install vagrant-vbguest
echo "BASE: Bringing up CentOS VM first time..."
vagrant up
echo "BASE: Updating installed CentOS packages..."
vagrant ssh -c 'sudo yum update -y'
echo "BASE: Reloading CentOS VM..."
vagrant reload --provision
REM vagrant ssh -c 'ln -s /hostdata/5g_proto .'
REM
REM VirtualBOX guest extension HOWTO at:
REM https://www.megajason.com/2017/06/10/install-virtualbox-guest-additions-on-centos-7/


REM BASE-DEV
echo "BASE-DEV: Installing GIT..."
vagrant ssh -c 'sudo yum install -y git'
REM  git config --global user.name "Your Name"
REM  git config --global user.email you@example.com
REM CentOS DSC Evo Development Environment V0.1.ova
echo "BASE-DEV: Installing Docker..."
vagrant ssh -c 'sudo yum install -y docker'
vagrant ssh -c 'sudo systemctl start docker'
vagrant ssh -c 'sudo systemctl enable docker'



REM GUI
echo "GUI: Installing CentOS Gnome..."
vagrant ssh -c 'sudo yum groupinstall -y gnome'
REM echo "Installing CentOS Desktop..."
REM vagrant ssh -c 'sudo yum groupinstall -y "gnome desktop"'
echo "GUI: Installing CentOS X11..."
vagrant ssh -c 'sudo yum install -y xorg*'
vagrant ssh -c 'sudo yum install -y xterm'
echo "GUI: Uninstalling superfluous packages..."
vagrant ssh -c 'sudo yum remove -y initial-setup initial-setup-gui'
echo "GUI: Let machine automatically start GUI on start-up..."
vagrant ssh -c 'sudo systemctl isolate graphical.target'
vagrant ssh -c 'sudo systemctl set-default graphical.target'
echo "GUI: Installing Firefox..."
vagrant ssh -c 'sudo yum install -y firefox'
echo "GUI: Installing Sublime..."



REM FINALLY
echo "Rebooting..."
vagrant halt
vagrant up

REM READY
echo "CentOS VM ready to be used for DSC development."
echo
echo "User the existing vagrant user or create a new one for you."
echo "From your hub environment copy id_rsa and id_rsa.pub to ~/.ssh and make sure user rights are like:"
echo "-rw-------   id_rsa"
echo "-rw-r--r--   id_rsa.pub"
echo
echo "READY"
echo
