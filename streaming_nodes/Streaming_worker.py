from fastapi import FastAPI, Response, status
from s3_handler import *
from pathlib import Path
from unzip_hls import *

servelet = FastAPI(); 
s3 = S3ClinetHandler()
download_location = Path("/downloaded")
download_location.mkdir(parents=True, exist_ok=True)
stream_location =  Path ("/stream/store")
stream_location.mkdir(parents= True, exist_ok= True)
@servelet.post("/download/new_vid/{vid_location}")
def download_vid(vid_location: str):
 downloaded = s3.download_object("encoded",vid_location+".zip","/downloaded/"+vid_location+".zip")
 if(not downloaded):
    return Response (status_code=status.HTTP_404_NOT_FOUND)
 finished_stream_location = stream_location/vid_location
 finished_stream_location.mkdir(parents=True, exist_ok=True)
 unziped = unzip_hls("/downloaded/"+vid_location+".zip", finished_stream_location)
 if(not unziped):
    return Response (status_code=status.HTTP_424_FAILED_DEPENDENCY)
 return Response(status_code=status.HTTP_200_OK )


            
            
            
            

