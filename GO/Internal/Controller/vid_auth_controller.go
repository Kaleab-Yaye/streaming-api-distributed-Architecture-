package Controller

import (
	"GO/Internal/service"
	"log"
	"net/http"
)

type VidAuthController struct {
	vidAuthService *service.VidAuthService
}

func NewVidAuthController(vidAuthService *service.VidAuthService) *VidAuthController {
	return &VidAuthController{
		vidAuthService: vidAuthService,
	}

}

func (vidAuthController *VidAuthController) vidAuthHandler(r *http.Request, w http.ResponseWriter) {
	accessToken := r.Header.Get("Access-Token")
	rawUri := r.Header.Get("request_uri")

	if accessToken == "" || rawUri == "" {
		log.Println("there was error readin a header value")
	}
	w.WriteHeader(vidAuthController.vidAuthService.WatchAuthHandler(rawUri, accessToken))

}
