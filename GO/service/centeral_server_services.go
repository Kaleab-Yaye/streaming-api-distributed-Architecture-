package service

import (
	"GO/DTOs"
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"time"
)

func RegistNodeHandler(port_num string) (error, bool) {

	request_body := DTOs.Regist_node{
		Ip_addr:     "localhost", // must be in a way that it dynamically looks up the machins ip addr
		Port_number: port_num,
	}

	end_point := "/api/v1/encoder/regist/new"

	mersheld_reqeust_body, err := json.Marshal(request_body)

	if err != nil {
		return err, false
	}

	request, err := http.NewRequest("POST", os.Getenv("CENTRAL_SERVER")+end_point, bytes.NewReader(mersheld_reqeust_body))

	if err != nil {
		fmt.Println("there was an error in setingup a reqeust")
		return err, false
	}

	request.Header.Add("Content-Type", "application/json")

	node_clinet := &http.Client{
		Timeout: 10 * time.Second,
	}

	res, err := node_clinet.Do(request)

	if err != nil {

		fmt.Println("there was an erro making a reqeust")
		return err, false

	}

	defer res.Body.Close()

	if res.StatusCode != http.StatusOK {
		fmt.Println("central server is not letting this node to registor, or can't be reached, the Status code is ", res.StatusCode)
		return nil, false
	}

	return nil, true

	//add the central server's ip addr in env
	// the jason format might not be the same with the cent DTO so...
	// and also how many times should we retry if the central server is not reachable?

}

func HandelHealthCheck(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
}
