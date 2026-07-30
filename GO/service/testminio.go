package service

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"path/filepath"

	"GO/DTOs"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type S3ClintStore struct {
	MinioClint *minio.Client
}

func NewS3ClintStore() *S3ClintStore {
	// all this shit will have to be/will be an enviroment variable just testing stuff out
	endpoint := "localhost:9000"
	accessKeyID := "ktadmin"
	secretAccessKey := "12345678"
	useSSL := false

	MinioClint, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKeyID, secretAccessKey, ""),
		Secure: useSSL,
	})

	if err != nil {
		fmt.Print("there was an error with creating the s3 Cinet")
	}

	return &S3ClintStore{
		MinioClint: MinioClint,
	}

}
func (s *S3ClintStore) DownloadFileFromMinIO(w http.ResponseWriter, r *http.Request) {
	var downloadRequest DTOs.DownloadFile
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

	err = s.MinioClint.FGetObject(context.Background(), downloadRequest.Bucket, downloadRequest.VidId, downloadedFilePath, minio.GetObjectOptions{})
	if err != nil {
		fmt.Println("there was an error in downlaoding the file", err)
		w.WriteHeader(http.StatusInternalServerError)
		return

	}

	fmt.Println("all done")
	w.WriteHeader(http.StatusOK)
	return

}
