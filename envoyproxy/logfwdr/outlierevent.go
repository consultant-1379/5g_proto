package main

import (
	"fmt"
)

// JSON decodes straight into this struct:
type OutlierEvent struct {
	EjectionType          string            `json:"type"`
	ClusterName           string            `json:"cluster_name"`
	UpstreamURL           string            `json:"upstream_url"`
	Action                string            `json:"action"`
	NumEjections          uint32            `json:"num_ejections"`
	Enforced              bool              `json:enforced"`
	EjectConsecutiveEvent map[string]string `json:"eject_consecutive_event"`
	Timestamp             string            `json:"timestamp"`
	SecsSinceLastAction   string            `json:"secs_since_last_action"`
}


// JSON decodes straight into this struct:
type HealthCheckEvent struct {
	CheckerType    string            `json:"health_checker_type"`
	Address        Host              `json:"host"`
	ClusterName    string            `json:"cluster_name"`
	HealthCheckEjectUnhealthy *HealthCheckEjectUnhealthy `json:"eject_unhealthy_event,omitempty"`
	HealthCheckAddHealthy     *HealthCheckAddHealthy     `json:"add_healthy_event,omitempty"`
	HealthCheckFailure        *HealthCheckFailure        `json:"health_check_failure_event,omitempty"`
	DegradedHealthyHost       *DegradedHealthyHost       `json:"degraded_healthy_host,omitempty"`
	NoLongerDegradedHost      *NoLongerDegradedHost      `json:"no_longer_degraded_host,omitempty"`
	Timestamp      string            `json:"timestamp"`
	Metadata       interface{}       `json:"-"`
	Locality       Locality          `json:"locality"`
}

type Locality struct {
	Region   string `json:"region"`
	Zone     string `json:"zone"`
	SubZone  string `json:"sub_zone"`
}

type Host struct {
	SocketAddress SocketAddress `json:"socket_address"`
}

type SocketAddress struct {
	Protocol    string `json:"protocol"`
	Address     string `json:"address"`
	PortValue   uint32 `json:"port_value"`
	ResolverName string `json:"resolver_name"`
	IPv4Compat  bool   `json:"ipv4_compat"`
}

type HealthCheckEjectUnhealthy struct {
    FailureType string `json:"failure_type"`
}

type HealthCheckAddHealthy struct {
    FirstCheck bool	`json:"first_check"`
}

type HealthCheckFailure struct {
    FailureType string	`json:"failure_type"`
    FirstCheck  bool	`json:"first_check"`
}

type DegradedHealthyHost struct {
}

type NoLongerDegradedHost struct {
}

func (h *HealthCheckEvent) String() string {
	result := fmt.Sprintf(
		"CheckerType: %s\nAddress: {SocketAddress: {Protocol: %s, Address: %s, PortValue: %d, ResolverName: %s, IPv4Compat: %v}}\nClusterName: %s\n",
		h.CheckerType, h.Address.SocketAddress.Protocol, h.Address.SocketAddress.Address, h.Address.SocketAddress.PortValue, h.Address.SocketAddress.ResolverName, h.Address.SocketAddress.IPv4Compat, h.ClusterName,
	)

	if h.HealthCheckEjectUnhealthy != nil {
		result += fmt.Sprintf("HealthCheckEjectUnhealthy: {FailureType: %s}\n", h.HealthCheckEjectUnhealthy.FailureType)
	}

	if h.HealthCheckAddHealthy != nil {
		result += fmt.Sprintf("HealthCheckAddHealthy: {FirstCheck: %v}\n", h.HealthCheckAddHealthy.FirstCheck)
	}

	if h.HealthCheckFailure != nil {
		result += fmt.Sprintf("HealthCheckFailure: {FailureType: %s, FirstCheck: %v}\n", h.HealthCheckFailure.FailureType, h.HealthCheckFailure.FirstCheck)
	}

	// DegradedHealthyHost and NoLongerDegradedHost are empty structs, so we just print their presence
	if h.DegradedHealthyHost != nil {
		result += "DegradedHealthyHost: true\n"
	}

	if h.NoLongerDegradedHost != nil {
		result += "NoLongerDegradedHost: true\n"
	}

	result += fmt.Sprintf("Timestamp: %s\n", h.Timestamp)

	// Metadata can be of any type, so we marshal it to JSON for a readable output
	if h.Metadata != nil {
		// should be a nil
	} else {
		result += "Metadata: nil\n"
	}

	result += fmt.Sprintf("Locality: {Region: %s, Zone: %s, SubZone: %s}\n", h.Locality.Region, h.Locality.Zone, h.Locality.SubZone)

	return result
}