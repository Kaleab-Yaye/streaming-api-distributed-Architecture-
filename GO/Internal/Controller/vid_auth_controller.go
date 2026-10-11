package Controller

import (
	"GO/Internal/service"
	"log"
	"net/http"
	"strconv"
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
	currentFrameString := r.Header.Get("current-frame")

	if accessToken == "" || rawUri == "" || currentFrameString == "" {
		log.Println("there was error reading a header value")
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	currentFrame, err := strconv.ParseFloat(currentFrameString, 64)
	if err != nil {
		log.Println("a current frame could't not be convert in to a float value")
		w.WriteHeader(http.StatusBadRequest)

	}
	w.WriteHeader(vidAuthController.vidAuthService.WatchAuthHandler(rawUri, accessToken, currentFrame))

}
