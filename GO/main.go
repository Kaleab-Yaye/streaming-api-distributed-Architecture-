package main

import (
	"fmt"
	"net/http"
)

func HandelTestReqeust(w http.ResponseWriter, r *http.Request) {

	fmt.Println("requests was made")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("all good"))

}

func main() {
	fmt.Println("server starting at port 3002")
	http.HandleFunc("/test", HandelTestReqeust)
	err := http.ListenAndServe(":3002", nil)
	if err != nil {
		fmt.Println("there was an error with attachign the server to the port")

		return

	}
}
