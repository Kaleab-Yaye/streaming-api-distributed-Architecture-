package utils

import (
	"archive/zip"
	"fmt"
	"io"
	"os"
	"path/filepath"
)

func UnzipHandler(zipName, zipPath, zipDestination string) bool {
	// i should first create the destination path and folder

	destinationPath := filepath.Join(zipDestination, zipName)
	err := os.MkdirAll(destinationPath, os.ModePerm)
	if err != nil {
		fmt.Println(" could carate the directory that is>>", destinationPath)
		return false
	}

	opendZip, err := zip.OpenReader(zipPath)
	if err != nil {
		fmt.Println("can't open ziped file for the reason>>>", err)
		return false

	}
	defer opendZip.Close()

	for _, file := range opendZip.File {
		openedFile, err := file.Open()
		if err != nil {

			fmt.Println("there was an error opening a file in the archive>>", err)
			return false
		}
		defer openedFile.Close()

		if file.FileInfo().IsDir() {
			err = os.MkdirAll(filepath.Join(destinationPath, file.Name), os.ModePerm)
			if err != nil {
				fmt.Println(" there was an error creating a dire >>>", err)
			}

			continue
		}

		destFilePath := filepath.Join(destinationPath, file.Name)

		newCreatedFileInTheDestination, err := os.Create(destFilePath)
		if err != nil {
			fmt.Println("there was an error creating a file in the destination", err)
			return false
		}

		// copying the file bits in the ziped file to the newnly created file\

		_, err = io.Copy(newCreatedFileInTheDestination, openedFile)

		if err != nil {
			fmt.Println("there was an error copying a file content >>", err)
			return true
		}

	}

	return true

}
