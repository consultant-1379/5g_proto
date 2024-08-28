NAMESPACE=$2
# user=(${NAMESPACE/"5g-bsf-"/})
umbrella_release="eric-sc-${USER}"
cncs_base_release="eric-cloud-native-base-${USER}"

echo "Deleting Key Management ClusterRoleBinding..."

kmsClusterRoleBindings=($(kubectl -n ${NAMESPACE} get ClusterRoleBinding | grep "eric-sec-key-management" | awk '{print $1}'))

for clusterRoleBinding in ${kmsClusterRoleBindings[@]}; do
    clusterRoleBindingRelease=$(kubectl -n ${NAMESPACE} get ClusterRoleBinding ${clusterRoleBinding} -o jsonpath="{.metadata.labels.release}")
    if [ "${clusterRoleBindingRelease}" = "${umbrella_release}" ]; then
        kubectl -n ${NAMESPACE} delete ClusterRoleBinding ${clusterRoleBinding}
    fi
    if [ "${clusterRoleBindingRelease}" = "${cncs_base_release}" ]; then
        kubectl -n ${NAMESPACE} delete ClusterRoleBinding ${clusterRoleBinding}
        break;
    fi
done

echo "Deleting namespace-named ClusterRoleBindings..."

nsClusterRoleBindings=($(kubectl -n ${NAMESPACE} get ClusterRoleBinding | grep "${NAMESPACE}" | awk '{print $1}'))
for clusterRoleBinding in ${nsClusterRoleBindings[@]}; do
    kubectl -n ${NAMESPACE} delete ClusterRoleBinding ${clusterRoleBinding}
done

echo "Deleting namespace-named ClusterRoles..."

nsClusterRoles=($(kubectl -n ${NAMESPACE} get ClusterRole | grep "${NAMESPACE}" | awk '{print $1}'))
for clusterRole in ${nsClusterRoles[@]}; do
    kubectl -n ${NAMESPACE} delete ClusterRole $clusterRole
done
