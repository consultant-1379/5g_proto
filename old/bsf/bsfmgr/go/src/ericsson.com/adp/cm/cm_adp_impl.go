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

import (
 	"fmt"
    "log"
    "encoding/json"
    "net/http"
    "github.com/alecthomas/jsonschema"
    "ericsson.com/tools/rest"
)

const (
	cmAdpImpBaseUrl = "http://eric-cm-mediator:5003/cm/api/v1.1"
)

// The bsfConfProviderCmImpl "class", implements cm.BsfConfProvider
type bsfConfProviderCmImpl struct {
	Title 	string		`json:"title"`
	Data 	*BsfConf 	`json:"data"`
}

// "constructor"
func NewBsfConfProviderCmImpl() (*bsfConfProviderCmImpl, error) {
	b := bsfConfProviderCmImpl{Schema,&DefaultBsfConf}

	err := b.init()
	if (err != nil) {
		return nil, err
	}

	return &b, nil
}

// initializator, constructor helper
func (b bsfConfProviderCmImpl) init() error {
	log.Printf("bsfConfProviderCmImpl.init()")

	// check whether schema exists...
	log.Printf("check whether schema exists...\n")
	resp, _, err := rest.Action{
		cmAdpImpBaseUrl,
		"checking existence of schema", http.MethodGet, "schemas", Schema, nil,
		nil,
		[]int{http.StatusOK, http.StatusNotFound},
		[]int{}}.Do()
	if err != nil {
		return err
	}

	// ...create one if not
	if resp.StatusCode == http.StatusNotFound {
		err = b.createNewSchema()

		if err != nil {
			return err
		}
	}

	// check whether config of that schema exists...
	log.Printf("check whether config exists...\n")
	resp, _, err = rest.Action{
		cmAdpImpBaseUrl,
		"checking existence of config", http.MethodGet, "configurations", Schema, nil, 
		nil,
		[]int{http.StatusOK, http.StatusNotFound},
		[]int{http.StatusBadRequest}}.Do()
	if err != nil {
		return err
	}

	// ...create one if not
	if resp.StatusCode == http.StatusNotFound {
		err = create(&ExampleBsfConf1)	// TODO: &DefaultBsfConf in production

		if err != nil {
			return err
		}
	}

	return b.updateSelf()
}

func (b bsfConfProviderCmImpl) updateSelf() error {
	// read latest config from CM Mediator
	_, body, err := rest.Action{
		cmAdpImpBaseUrl,
		"reading config", http.MethodGet, "configurations", Schema, nil,
		nil,
		[]int{http.StatusOK},
		[]int{http.StatusBadRequest, http.StatusNotFound}}.Do()
	if err != nil {
		return err
	}

	var updatedConfig bsfConfProviderCmImpl
	err = json.Unmarshal(*body, &updatedConfig)
	if err != nil {
    	return fmt.Errorf("unmarshaling response %v", err)
	}

	b.Title = updatedConfig.Title
	b.Data = updatedConfig.Data

	return nil
}

type nameTitleGetResp struct {
	Name 	string		`json:"name"`
	Title 	string		`json:"title"`
}

// TODO
func (b bsfConfProviderCmImpl) DeleteAll() error {
	// get list of all configs
	_, body, err := rest.Action{
		cmAdpImpBaseUrl,
		"getting list if all configs", http.MethodGet, "configurations", "", nil,
		nil,
		[]int{http.StatusOK},
		[]int{}}.Do()
	if err != nil {
		return err
	}

	var configs []nameTitleGetResp
	err = json.Unmarshal(*body, &configs)
	if err != nil {
    	return fmt.Errorf("unmarshaling response %v", err)
	}

	for _, config := range configs {
		err = delete(&config)
		if err != nil {
			return err
		}
	}

	return nil
}

func delete(config *nameTitleGetResp) error {
	// delete single config, ignoreing "not found"
	_, _, err := rest.Action{
		cmAdpImpBaseUrl,
		"deleting config", http.MethodDelete, "configurations", config.Name, nil, nil,
		[]int{http.StatusOK, http.StatusNotFound},
		[]int{}}.Do()
	if err != nil {
		return err
	}

	return nil
}

type configPostReq struct {
	Name 	*string		`json:"name"`
	Title 	*string		`json:"title"`
	Data 	*BsfConf 	`json:"data"`
}

func create(newConf *BsfConf) error {
	var req configPostReq

	req.Name = &newConf.Meta.Schema
	req.Title = &newConf.Meta.Config
	req.Data = newConf

	reqJson, err := json.MarshalIndent(req, "", "  ")
	if err != nil {
	    panic("fatal internal JSON marchaling error")
	}

	_, _, err = rest.Action{
		cmAdpImpBaseUrl,
		"creating config", http.MethodPost, "configurations", "", nil,
		&reqJson, 
		[]int{http.StatusCreated, http.StatusConflict}, // conflict = already there = OK
		[]int{http.StatusBadRequest, http.StatusNotFound}}.Do()

	return err
}

type configPutReq struct {
	Title 	*string		`json:"title"`
	Data 	*BsfConf 	`json:"data"`
}

func (b bsfConfProviderCmImpl) Put(newConf *BsfConf) error {
	var req configPutReq

	req.Title = &newConf.Meta.Config
	req.Data = newConf

	reqJson, err := json.MarshalIndent(req, "", "  ")
	if err != nil {
	    panic("fatal internal JSON marchaling error")
	}

	_, _, err = rest.Action{
		cmAdpImpBaseUrl,
		"updating config", http.MethodPut, "configurations", Schema, nil, 
		&reqJson,
		[]int{http.StatusOK},
		[]int{http.StatusBadRequest, http.StatusNotFound, http.StatusConflict}}.Do()

	return err
}

func (b bsfConfProviderCmImpl) Get() (*BsfConf, error) {
	err := b.updateSelf()
	if err != nil {
		return nil, nil
	}

	return b.Data, nil
}

func (b bsfConfProviderCmImpl) SwitchTo(configName *string) error {
	// TODO
	return nil
}

// create potentially new schema at ADP CM Mediator GS (exitent schema would be upgraded)
func (b bsfConfProviderCmImpl) createNewSchema() error {
	log.Printf("createNewSchema()...\n")

	json := b.AsJsonSchema()

	_, _, err := rest.Action{
		cmAdpImpBaseUrl,
		"creating schema", http.MethodPut, "schemas", Schema, nil, 
		json,
		[]int{http.StatusOK, http.StatusCreated},
		[]int{http.StatusConflict}}.Do()

	return err
}

// convenience func automatically creating a bsfConfProviderCmImpl object
func AsJsonSchema() *[]byte {
	b := bsfConfProviderCmImpl{}
	return b.AsJsonSchema()
}

// To satisfy CM Mediator expected JSON value in which the actual JSON schema is embedded in
type cmMediatorSchemaWrapper struct {
	Title		string				`json:"title"`
	JsonSchema 	*jsonschema.Schema	`json:"jsonSchema"`
}

// returns JSON schema description as expected by ADP CM Mediator GS
func (b bsfConfProviderCmImpl) AsJsonSchema() *[]byte {
	r := jsonschema.Reflector{}
	s := r.Reflect(&BsfConf{})

	sw := cmMediatorSchemaWrapper{Schema, s}

	json, err := json.MarshalIndent(sw, "", "  ")
	if err != nil {
	    panic("fatal internal JSON marchaling error")
	}
	return &json
}

// returns JSON value of this config
func (b bsfConfProviderCmImpl) AsJsonValue() *[]byte {
	// cw := cmMediatorConfigWrapper{b.Meta.ConfigName, &b}

	json, err := json.MarshalIndent(b, "", "  ")
	if err != nil {
	    panic("fatal internal JSON marchaling error")
	}
	return &json
}

// delete old schema definition from ADP CM Mediator GS (non-existence ignored)
func (b bsfConfProviderCmImpl) deleteOldSchemaIfExists() error {
	_, _, err := rest.Action{
		cmAdpImpBaseUrl,
		"deleting schema", http.MethodDelete, "schemas", Schema, nil, 
		nil,
		[]int{http.StatusOK, http.StatusNotFound},
		[]int{http.StatusBadRequest, http.StatusConflict}}.Do()

	return err
}

// create potentially new schema at ADP CM Mediator GS (exitent schema would be upgraded)
func (b bsfConfProviderCmImpl) updateSchema() error {
	json := b.AsJsonSchema()

	_, _, err := rest.Action{
		cmAdpImpBaseUrl,
		"creating schema", http.MethodPut, "schemas", Schema, nil, 
		json,
		[]int{http.StatusOK, http.StatusCreated},
		[]int{http.StatusConflict}}.Do()

	return err
}

