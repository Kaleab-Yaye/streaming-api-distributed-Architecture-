package service

import (
	"GO/utils"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"path/filepath"

	"GO/DTOs"

	"errors"
	"strconv"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type S3ClintStore struct {
	MinioClint *minio.Client
}

func NewS3ClintStore() (*S3ClintStore, error) {
	// all this shit will have to be/will be an enviroment variable just testing stuff out
	// it is now
	endpoint := os.Getenv("END_POINT")
	accessKeyID := os.Getenv("ACCESS_kEY")
	secretAccessKey := os.Getenv("SECRET_ACCESS_KEY")
	rawBoolValueFromEnv := os.Getenv("SSL_USE")
	useSSL, err := strconv.ParseBool(rawBoolValueFromEnv)

	if endpoint == "" || accessKeyID == "" || secretAccessKey == "" || rawBoolValueFromEnv == "" {

		return nil, errors.New("could't extract one or more env variables to setup the clinet")
	}

	if err != nil {
		fmt.Println(" there was an error getting the bool value from the env")
		return nil, err

	}

	MinioClint, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKeyID, secretAccessKey, ""),
		Secure: useSSL,
	})

	if err != nil {
		fmt.Print("there was an error with creating the s3 Cinet")
		return nil, err
	}

	return &S3ClintStore{
		MinioClint: MinioClint,
	}, nil

}
func (s *S3ClintStore) DownloadFileFromMinIO(w http.ResponseWriter, r *http.Request) {
	var downloadRequest DTOs.DownloadFile
	if r.Method != http.MethodPost {
		w.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	err := json.NewDecoder(r.Body).Decode(&downloadRequest)
	if err != nil {
		fmt.Println("there was an error decoding the reqeust")
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("the json format didn't get recognised"))
		return

	}
	rawDownloadFolder := "Raw_Download"
	downloaderName := downloadRequest.VidId
	rawDownloadFolderPath := filepath.Join(".", rawDownloadFolder)
	err = os.MkdirAll(rawDownloadFolderPath, os.ModePerm)
	downloadedFilePath := filepath.Join(".", rawDownloadFolder, downloaderName)

	if err != nil {
		fmt.Println("there was an error creating the download folder")
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	err = s.MinioClint.FGetObject(context.Background(), downloadRequest.Bucket, downloadRequest.VidId+".zip", downloadedFilePath, minio.GetObjectOptions{})
	if err != nil {
		fmt.Println("there was an error in downlaoding the file", err)
		w.WriteHeader(http.StatusInternalServerError)
		return

	}

	// we gotta unzip it

	result := utils.UnzipHandler(downloadRequest.VidId, downloadedFilePath, filepath.Join(".", "unzipped"))

	if result != true {
		w.WriteHeader(http.StatusInternalServerError)
		return

	}

	err = os.RemoveAll(downloadedFilePath)
	if err != nil {
		fmt.Println("there was an error removing the raw downloaded file")
		w.WriteHeader(http.StatusInternalServerError)
		return

	}

	fmt.Println("all done")
	w.WriteHeader(http.StatusOK)
	return

}
