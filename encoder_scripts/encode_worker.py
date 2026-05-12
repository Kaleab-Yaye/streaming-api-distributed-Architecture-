from pathlib import Path
from raw_commands import get_command
from make_request import *
from s3_handler import *
from upload_to_s3_after_ziping import *
import os
import requests
import time
import shlex
import subprocess
import shutil

class Const:
  # here are the location variables and what they are meant to point to
  # new_work.vid_location: is the vid Key in s3 raw uploads, and will aslo be the the
  # ...key for the encoded version
  #temp_download_location: is the directory where vid is download from s3 storage
  #temp_encode_location: is a directory where encoded videos are saved before getting zipped
  #temp_ziped_location: is dir where the zipped and encoded vid file is stored before getting upload
  # vid_loaction: is orphan variable and can be removed but i wont

 time_to_sleep = 30


def extract_env()->tuple[str, str]:
  
  return os.getenv("RAW_VID"), os.getenv("PROC_ED")


def delete_file(location: str)->int:
 to_be_deleted = Path(location)
 to_be_deleted_info = Path(location + ".info")
 print (f" the lcoaiton to be deleted is {location}")

 try:
  to_be_deleted.unlink(missing_ok = True)
  to_be_deleted_info.unlink(missing_ok= True)

 except Exception as ex:
  print(f" unexpexted error while delting a file ex>>>>>>:: {ex}")
  return 1
 
 print("deleted the file")
 return 0 


def run_command(command_num:int, command: list[str])->tuple[int, str]:
 if(command_num== 1 or command_num == 4):
  runer = subprocess.run(
        command,
        capture_output= True,
        text= True,
    )
  return runer.returncode, runer.stdout.strip()
 
 elif(command_num == 2 or command_num ==3):
  runer = subprocess.run(
        command,
        capture_output= True,
        text= True,
    )
  return runer.returncode, None

if __name__ == "__main__":
 
#### const values
 temp_dowload_location = Path("/temp_download")
 temp_dowload_location.mkdir(parents=True, exist_ok=True)
 s3_clinet = S3ClinetHandler()
 temp_encode_location = Path("/temp_upload")
 temp_encode_location.mkdir(parents=True, exist_ok=True)
 temp_ziped_location = Path("/temp_ziped")
 temp_ziped_location.mkdir(parents=True, exist_ok=True)

 time.sleep(15)

 while True:
  time.sleep(15)
  print( " making a reqeust")
  new_work = get_new_job()
  print(f"::: got a response with {new_work.status_code}")
  if (new_work.status_code == 204):
   time.sleep(Const.time_to_sleep)
   continue
  elif (new_work.status_code != 200):
   print("::::::::::::::::::::::iligal server reponse:::::::::::::::::::::::::::::::")


  
  print(":::::: got vide locaito of::::vid and id ", new_work.vid_location, new_work.vid_id)
  vid_loaction = str(temp_dowload_location/new_work.vid_location)
  vid_id = new_work.vid_id

  vid_store_location = extract_env()[0]

  # download vid chunk 
  downloaded  = s3_clinet.download_object("raw-upload", new_work.vid_location, str(temp_dowload_location/new_work.vid_location)  )
  if(not downloaded):
   print("couldnt download the requested vid skiping work")
   #add the reqeust to the central server to tell it we could't download it so that 
   #
   #it doens keep pushing the same work over and over again
   #
   #
   #for now i am going to continue
   #
   continue

  # so the command every thing should wrok with as inteded nothing should change there the vid raw locaiton should be where the donloaded file is located
  vid_raw_location = str(vid_loaction)
  encoded_video_sotore_location_path = temp_encode_location/new_work.vid_location
  encoded_video_sotore_location_path.mkdir(parents=True, exist_ok=True)

  
  encoded_video_sotore_location = str(encoded_video_sotore_location_path)

  print(f":::::: got vid raw location at ::::::::::::: {vid_raw_location}")
  
  prob_check_command = get_command(1, vid_raw_location, vid_loaction)
  decode_check_command = get_command(2, vid_raw_location, vid_loaction )
  encode_check_command = get_command(3,vid_raw_location,  encoded_video_sotore_location) 
  get_vid_length_check_command =get_command(4,vid_raw_location, vid_loaction )

  #log
  print("::::::::::::::::::running prob check::::::::::::::::::::::")


  prob_check_result = run_command(1, prob_check_command)

  #log
  print(f"::::::::::::::::::out of prob check with the result {prob_check_result[1]} and status code { prob_check_result[0]}::::::::::::::::::::::")

  if(prob_check_result[0] != 0 or int( prob_check_result[1])<=1):
   issueReport = "holder"
   delete_result = delete_file(vid_raw_location)
   s3_object_delete_result = s3_clinet.delete_object("raw-upload", new_work.vid_location)

   if((delete_result==1)):
    if(not s3_object_delete_result):
    ### i will add video state to the Enum when a file could't be delted from s3 storage but for now we will know it bassed on log
     issueReport = IssueReport(vid_id, True, False, False, False, False)
    else:
     issueReport = IssueReport(vid_id, True, False, False, False, True)
      
   else:
    if(not s3_object_delete_result):
     issueReport = IssueReport(vid_id, True, False, False, True, False)
    else:
     issueReport = IssueReport(vid_id, True, False, False, True, True)
     

   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   reportResult = report_issue(issueReport)

   #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   print(f"::::::::::::::server responded with the status code{reportResult}:::::")

   if(reportResult != 200):
    print("server handling issue responded with iligal status code")
   time.sleep(Const.time_to_sleep)
   continue


#log
  print(":::: runing decode check:::::::::::::::::::")
  
  

  decode_check_result = run_command (2, decode_check_command)

  #log

  print("::: out of decode check with status code", decode_check_result[0])



  if(decode_check_result[0] != 0):
   issueReport = "holder"
   delete_result = delete_file(vid_raw_location)
   s3_object_delete_result = s3_clinet.delete_object("raw-upload", new_work.vid_location )
   if(delete_result==1 ):
     if(not s3_object_delete_result):
      issueReport = IssueReport(vid_id, False, True, False, False, False)
     else:
      issueReport = IssueReport(vid_id, False, True, False, False, True)
    

    

     
     
  
   else:
     if(not s3_object_delete_result):
      issueReport = IssueReport(vid_id, False, True, False, True, False)
     else:
      issueReport = IssueReport(vid_id, False, True, False, True, True)
      #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   reportResult = report_issue(issueReport)
   #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   print("::::::::::::::server responded with the status code}:::::", reportResult)

   if(reportResult != 200):
    print("server handling issue responded with iligal status code")
   time.sleep(Const.time_to_sleep)
   continue


  #log
  print("before going to encode check making vid length identification")
  vid_length_result = run_command(4 , get_vid_length_check_command)
  print(f" the length check is out with the status code of :: {vid_length_result[0]}the length of vid is::: {vid_length_result[1]}")
  print(":::: runing encode check:::::::::::::::::::")
  vid_length = float(vid_length_result[1])
  print(f"the length of the vid is {vid_length}")
  encode_check_result = run_command (3, encode_check_command)

  print("::: out of encode check with status code", encode_check_result[0])


  if (encode_check_result[0]!=0):
   ## will add the delting step here for now we will just say false
   issueReport = "holder"
   delete_result = delete_file(vid_raw_location)
   s3_object_delete_result = s3_clinet.delete_object("raw-upload", new_work.vid_location)
   if(delete_result==1 ):
     if(s3_object_delete_result): # 
      issueReport = IssueReport(vid_id, False, False, True, False, True)
     else:
       issueReport = IssueReport(vid_id, False, False, True, False, False)


   else:
      if(s3_object_delete_result):
       issueReport = IssueReport(vid_id, False, False, True, True, True)
      else:
       issueReport = IssueReport(vid_id, False, False, True, True, False)
       


   #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   reportResult = report_issue(issueReport)
   #log
   print(f"::::::::::::::server responded with the status code{ reportResult}:::::")

   if(reportResult != 200):
    print("server handling issue responded with iligal status code")
   time.sleep(Const.time_to_sleep)
   continue 

  jobDone = "holder"


## job done setup

  print("trying to zip and upload encnoded file")
  
  delete_result = delete_file(vid_raw_location)
  zip_destination= str(temp_ziped_location/new_work.vid_location) + ".zip"
  encoded_and_ziped_key = new_work.vid_location+".zip"
  encoded_bucket = "encoded"

  zip_and_upload_result = zip_and_upload(encoded_video_sotore_location, zip_destination, encoded_and_ziped_key, encoded_bucket )
  print(f"out of ziping and uplaoding with the result zipng:{zip_and_upload_result[0]}, uploading:{zip_and_upload_result[1]}")
  s3_object_delete_result = False

  if(zip_and_upload_result[1]):
   s3_object_delete_result = s3_clinet.delete_object("raw-upload", new_work.vid_location)

  if(delete_result==1):
    if(not s3_object_delete_result):
     jobDone = JobDone(vid_id, False, new_work.vid_location, vid_length, s3_object_delete_result, zip_and_upload_result[0], zip_and_upload_result[1])
  else:
    jobDone = JobDone(vid_id, True, new_work.vid_location, vid_length, s3_object_delete_result, zip_and_upload_result[0], zip_and_upload_result[1] ) 

  


  print(f"::::::::::::reporting jobe done with {jobDone.length}:::::::::::::::::::::::::::::")

  jobeDoneResponseResult = successJob(jobDone)
  print(f":::::::::::: jobe done report from server stuatus code:::::::::::::::::::::::::::::{jobeDoneResponseResult}", )

  if(jobeDoneResponseResult != 200):
   print(f"server responsded with the status code {jobeDoneResponseResult} ")
 
