package Controller

import (
	"GO/DTOs"
	"GO/Internal/service"
	"encoding/json"
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

func (vidAuthController *VidAuthController) doYouHaveWatchIdCachedHandler(r *http.Request, w http.ResponseWriter) {

	watchId := r.URL.Query().Get("watchId")

	currentFrameVar := vidAuthController.vidAuthService.DoYouHaveWatchInCacheHandler(watchId)

	w.WriteHeader(http.StatusOK)

	err := json.NewEncoder(w).Encode(DTOs.Current_Frame{CurrentFrame: currentFrameVar})

	if err != nil {
		http.Error(w, "failed to encode jason value", http.StatusInternalServerError)
	}

}
