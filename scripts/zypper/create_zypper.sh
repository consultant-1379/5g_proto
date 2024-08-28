#!/bin/bash

CBOS_VERSION=$1
SLES_PATH=arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$CBOS_VERSION/
SLES_REPO=https://$SLES_PATH
TEMPDIR=$2/zypper/tmp
OUTDIR=$2/zypper

rm -rf $OUTDIR
mkdir -p $TEMPDIR/zypper-rpm

#rpm rpms
package_rpm=("rpm-*.x86_64.rpm" \
             "libpopt0-*.x86_64.rpm" \
  	         "liblua5_*.x86_64.rpm" \
   	         "libgcrypt20-*.x86_64.rpm" \
   	         "libelf1-*.x86_64.rpm" \
 	         "libzstd1-*.x86_64.rpm" \
	         "libgpg-error0-*.x86_64.rpm" \
             "tar-*.x86_64.rpm" )

#zypper rpms
package_zypper=("liblz4-*.x86_64.rpm" \
                "libsystemd0-*.x86_64.rpm" \
                "libprocps7-*.x86_64.rpm" \
                "procps-*.x86_64.rpm" \
                "libzck1-*.x86_64.rpm" \
                "libyaml-cpp0_*.x86_64.rpm" \
                "libsolv-tools-*.x86_64.rpm" \
                "libsigc-2_*.x86_64.rpm" \
                "file-magic-*.noarch.rpm"
                "libmagic1-*.x86_64.rpm" \
                "libpopt0-*.x86_64.rpm" \
                "liblua5_*.x86_64.rpm" \
                "libelf1-*.x86_64.rpm" \
                "libdw1-*.x86_64.rpm" \
                "libgdbm4-*.x86_64.rpm" \
                "perl-base-5*.x86_64.rpm" \
                "perl-5*.x86_64.rpm" \
                "rpm-config-SUSE-*.noarch.rpm" \
                "rpm-*.1.x86_64.rpm" \
                "libproxy1-*.x86_64.rpm" \
                "libprotobuf-lite20-*.x86_64.rpm" \
                "pinentry-*.x86_64.rpm" \
                "libudev1-*.x86_64.rpm" \
                "libusb-*.x86_64.rpm" \
                "libnpth0-*.x86_64.rpm" \
                "libksba8-*.x86_64.rpm" \
                "libgcrypt20-*.x86_64.rpm" \
                "libgpg-error0-*.x86_64.rpm" \
                "libassuan0-*.x86_64.rpm" \
                "gpg2-*.x86_64.rpm" \
                "libgpgme11-*.x86_64.rpm" \
                "libzstd1-*.x86_64.rpm" \
                "libssh-config-*.x86_64.rpm" \
                "libssh4-*.x86_64.rpm" \
                "libpsl5-*.x86_64.rpm" \
                "libnghttp2-*.x86_64.rpm" \
                "libsasl2*.x86_64.rpm" \
                "libldap-data-*.noarch.rpm" \
                "libldap-*.x86_64.rpm" \
                "libunistring2-*.x86_64.rpm" \
                "libidn2-*.x86_64.rpm" \
                "fillup-*.x86_64.rpm" \
                "diffutils-*.x86_64.rpm" \
                "permissions-*.x86_64.rpm" \
                "libtirpc-netconfig-*.x86_64.rpm" \
                "libtirpc3-*.x86_64.rpm" \
                "libnsl2-*.x86_64.rpm" \
                "cracklib-*.x86_64.rpm" \
                "libcrack2-*.x86_64.rpm" \
                "libaudit1-*.x86_64.rpm" \
                "pam-*.x86_64.rpm" \
                "libverto1-*.x86_64.rpm" \
                "libkeyutils1-*.x86_64.rpm" \
                "libcom_err2-*.x86_64.rpm" \
                "krb5-*.x86_64.rpm" \
                "libbrotlicommon1-*.x86_64.rpm" \
                "libbrotlidec1-*.x86_64.rpm" \
                "libcurl4-*.x86_64.rpm" \
                "libboost_system1*.x86_64.rpm" \
                "boost-license1*.noarch.rpm" \
                "libboost_thread1*.x86_64.rpm" \
                "libzypp-*.x86_64.rpm" \
                "libxml2-*.x86_64.rpm" \
                "libaugeas0-*.x86_64.rpm" \
                "zypper-*.x86_64.rpm")

#Fetch rpm rpms and unpack
echo "Fetching RPMs and tar binary for RPM package."
for rpm in ${package_rpm[@]}; do
  wget --quiet -P $TEMPDIR -r -l2 --no-parent -A "$rpm" $SLES_REPO
  #Move rpms out of full path to tempdir parent
  file_x86_64=$TEMPDIR/$SLES_PATH/x86_64/$rpm
  file_noarch=$TEMPDIR/$SLES_PATH/noarch/$rpm
  [ -f $file_x86_64 ] && mv $file_x86_64 $TEMPDIR/
  [ -f $file_noarch ] && mv $file_noarch $TEMPDIR/
  #Unpack rpms and remove rpms/full path folders
  rpm2cpio $TEMPDIR/$rpm | (cd $TEMPDIR; cpio -idm --quiet)
  rm -rf $TEMPDIR/$rpm
done
#Cleanup
rm -rf $TEMPDIR/arm.rnd.ki.sw.ericsson.se

#Fetch zypper rpms
echo "Fetching Zypper RPMs."
for rpm in ${package_zypper[@]}; do
  wget --quiet -P $TEMPDIR -r -l2 --no-parent -A "$rpm" $SLES_REPO
done
#Move Zypper rpms to tempdir parent
mv $TEMPDIR/$SLES_PATH/x86_64/*.rpm $TEMPDIR/zypper-rpm
mv $TEMPDIR/$SLES_PATH/noarch/*.rpm $TEMPDIR/zypper-rpm
rm -rf $TEMPDIR/arm.rnd.ki.sw.ericsson.se

#Create rpm tarfile to transfer to pod
#Tar is needed for kubectl cp to work
echo "Copy tar binary and tar file to transfer to pod."
cd $TEMPDIR
tar -cf ../zypper.tar .
cp usr/bin/tar ../
