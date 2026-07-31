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

		if file.FileInfo().IsDir() {
			err = os.MkdirAll(filepath.Join(destinationPath, file.Name), os.ModePerm)
			if err != nil {
				fmt.Println(" there was an error creating a dire >>>", err)

			}

			continue
		}

		destFilePath := filepath.Join(destinationPath, file.Name)
		destFilePathParentFolders := filepath.Dir(destFilePath)
		err = os.MkdirAll(destFilePathParentFolders, os.ModePerm) // i ran to an issue before becouse of how the zip file is organized
		/*
				it looked more like this, and was passing the is.dir() test
			master.m3u8
			stream_360/segment_000.ts
			stream_360/playlist.m3u8
			stream_720/segment_000.ts
			stream_720/playlist.m3u8
			stream_480/segment_000.ts
			stream_480/playlist.m3u8

		*/

		if err != nil {
			fmt.Println("error occured while creating the parent folder chain for the file >>>", err)
		}

		newCreatedFileInTheDestination, err := os.Create(destFilePath)
		if err != nil {
			fmt.Println("there was an error creating a file in the destination", err)
			return false
		}

		// copying the file bits in the ziped file to the newnly created file\

		_, err = io.Copy(newCreatedFileInTheDestination, openedFile)

		if err != nil {
			fmt.Println("there was an error copying a file content >>", err)
			return false
		}

		// had to close the resourses manually as the discriptors were stucking up. deffering for big files would fuck the whole thing up

		newCreatedFileInTheDestination.Close()
		openedFile.Close()

	}


	return true

}
