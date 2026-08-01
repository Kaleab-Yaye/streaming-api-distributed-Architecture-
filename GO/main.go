package main

import (
	"GO/service"
	"fmt"
	"net/http"
)

func HandelTestReqeust(w http.ResponseWriter, r *http.Request) {

	fmt.Println("requests was made")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("all good"))

}

func main() {
	fmt.Println("server starting at port 3002, well if you see this it is newly compiled")
	minioClinet, err := service.NewS3ClintStore()
	if err != nil {
		fmt.Println("fatal error while setting up s3_Clinet", err)
		return
	}

	http.HandleFunc("/stream/node/test2", HandelTestReqeust)
	http.HandleFunc("/stream/node/prepare", minioClinet.DownloadFileFromMinIO)

	err = http.ListenAndServe(":3002", nil)
	if err != nil {
		fmt.Println("there was an error with attachign the server to the port")

		return

	}
}
