package main

import (
	"GO/service"
	"fmt"
	"net/http"
	"os"
	"time"
)

func HandelTestReqeust(w http.ResponseWriter, r *http.Request) {

	fmt.Println("requests was made")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("all good"))

}

func main() {

	port_number := os.Getenv("PORT_NUM")
	fmt.Println("server starting at port ", port_number, "well if you see this it is newly compiled")
	minioClinet, err := service.NewS3ClintStore()
	if err != nil {
		fmt.Println("could't parse the port number from env, exiting")
	}
	err, stat := service.RegistNodeHandler(port_number)

	if stat != true {
		fmt.Println("first atttempt to regist fails with the error ", err)
		// now we need a retry logic that extends to upto 5 tries before killing node

		j := 1

		for i := 0; i < 5; i++ {

			err, stat = service.RegistNodeHandler(port_number)

			if stat == true {

				break

			}

			fmt.Println(i+1, " atttempt to regist failed with the error ", err)
			time.Sleep(time.Duration(5*j) * time.Second)
			j *= 2

		}

	}

	if stat != true {
		fmt.Println("could't register node exiting with err ", err)
		return
	}

	fmt.Println("node registered, starting service")

	if err != nil {
		fmt.Println("fatal error while setting up s3_Clinet", err)
		return
	}

	http.HandleFunc("/stream/node/test2", HandelTestReqeust)
	http.HandleFunc("/stream/node/prepare", minioClinet.DownloadFileFromMinIO)

	err = http.ListenAndServe(":"+port_number, nil) // should probably move the port num to env
	if err != nil {
		fmt.Println("there was an error with attachign the server to the port")

		return

	}
}
