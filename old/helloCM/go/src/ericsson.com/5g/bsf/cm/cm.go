// vim:ts=4:sw=4
/*
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 15, 2018
 *     Author: eedsvs
 */
// Configuration Mgmt Provider for BSF
package cm

const (
	Schema string = "ericsson.com-5g-bsf-schema-0.0.1-2"
)

// The BSF DT
type BsfConf struct {
	Meta 	BsfMetaInfo	`json:"metaInfo"`
	Node 	BsfMainConf	`json:"mainConf"`
}

// Versioning and other meta info that we might need
type BsfMetaInfo struct {
	Schema 	string 	`json:"schema"`
	Config	string	`json:"config"`
	Touch	int 	`json:"touch"`
}

// BsfMainConf defines BSF's toplevel data such as host name and hello message
type BsfMainConf struct {
	EP		EPType
	NRFs	[]RemoteEpType
}

type RemoteEpType struct {
	EP 				EPType
	ReconnectPolicy	string
}

type EPType struct {
	Hostname	string	`json:"hostname"`
	IP 			string 	`json:"ip"`
	Domain		string	`json:"domain"`
}

// BsfConfProvider is the CM interface from BSF perspective, i.e. providing all config data fucntions needed by BSF
type BsfConfProvider interface {
	Get() (*BsfConf, error)

	// Push and switch to new DT
	Put(newConf *BsfConf) error

	// Blank-out CM Mediator's DB entirely
	DeleteAll() error

	// returns the structure of this BsfConf as JSON schema
	// AsJsonSchema() *[]byte
}

var DefaultBsfConf BsfConf = BsfConf {
	Meta: BsfMetaInfo{Schema: Schema, Config: "bsf-default"},
	Node: BsfMainConf{
		EP:		EPType{
			Hostname:	"",
			IP:			"",
			Domain:		""},
		NRFs:	[]RemoteEpType{RemoteEpType{
			EP:	EPType{
				Hostname:	"",
				IP:			"",
				Domain:		""},
			ReconnectPolicy: ""}}}}

var ExampleBsfConf1 BsfConf = BsfConf {
	Meta: BsfMetaInfo{Schema: Schema, Config: "bsf-example-1"},
	Node: BsfMainConf{
		EP:		EPType{
			Hostname:	"bsf-01",
			IP:			"10.78.17.1",
			Domain:		"ericsson.com"},
		NRFs:	[]RemoteEpType{RemoteEpType{
			EP:	EPType{
				Hostname:	"nrf-01",
				IP:			"10.78.18.1",
				Domain:		"ericsson.com"},
			ReconnectPolicy: "strict"},	RemoteEpType{
			EP:	EPType{
				Hostname:	"nrf-02",
				IP:			"10.78.18.2",
				Domain:		"ericsson.com"},
			ReconnectPolicy: "lazy"}}}}
